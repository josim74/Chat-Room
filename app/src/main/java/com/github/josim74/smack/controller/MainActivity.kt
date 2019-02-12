package com.github.josim74.smack.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import com.github.josim74.smack.R
import com.github.josim74.smack.adapters.MessageAdapter
import com.github.josim74.smack.model.Channel
import com.github.josim74.smack.model.Message
import com.github.josim74.smack.services.AuthService
import com.github.josim74.smack.services.MessageService
import com.github.josim74.smack.services.UserDataService
import com.github.josim74.smack.utils.BROADCAST_USER_DATA_CHANGE
import com.github.josim74.smack.utils.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity(){
    val socket = IO.socket(SOCKET_URL)
    lateinit var channleAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null

    private fun setAdapters() {
        channleAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channleAdapter

        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        setAdapters()

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGE))

        channel_list.setOnItemClickListener { parent, view, position, id ->
            selectedChannel = MessageService.channels[position]
            drawer_layout.closeDrawer(Gravity.START)
            updateWithChannel()
        }

        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this){}
        }

    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        socket.disconnect()
        super.onDestroy()
    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn) {
                userNameNaveHeader.text = UserDataService.name
                userEmailNaveHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginBtnNaveHeader.text = "Logout"

                MessageService.getChannels(){complete ->
                    if (complete) {
                        if (MessageService.channels.count() > 0) {
                            selectedChannel = MessageService.channels[0]
                            channleAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                    }
                }
            }
        }
    }

    fun updateWithChannel() {
        mainChannelName.text = "#${selectedChannel?.name}"

        if (selectedChannel != null) {
            MessageService.getMessage(selectedChannel!!.id) { complete ->
                if (complete) {
                    for (message in MessageService.messages) {
                        messageAdapter.notifyDataSetChanged()
                        if (messageAdapter.itemCount > 0) {
                            messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                        }
                    }
                }
            }

        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginBtnNaveClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            //logout..
            UserDataService.logout()

            channleAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()

            userNameNaveHeader.text = ""
            userEmailNaveHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNaveHeader.text = "Login"
            mainChannelName.text = "Please log in"
        }else{
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

    }

    fun addChannelClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)
            builder.setView(dialogView).setPositiveButton("Add"){dialog, which ->
                val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                val descriptionTextField = dialogView.findViewById<EditText>(R.id.addChannelDescText)

                val channelName = nameTextField.text.toString()
                val channelDescription = descriptionTextField.text.toString()
                //create channel...
                socket.emit("newChannel", channelName, channelDescription)
                hideKeyboard()
            }.setNegativeButton("Cancel"){dialog, which ->
                hideKeyboard()
            }.show()
        }
    }

    fun sendMessageClicked(view: View) {
        if (App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage", messageTextField.text.toString(), userId, channelId, UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)
            messageTextField.text.clear()
            hideKeyboard()
        }
    }

    private val onNewChannel = Emitter.Listener {args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName, channelDescription, channelId)
                MessageService.channels.add(newChannel)

                channleAdapter.notifyDataSetChanged()
            }
        }
        
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelId = args[1] as String
                if (channelId == selectedChannel?.id) {
                    val msgBOdy = args[0] as String
                    val userName = args[2] as String
                    val userAvatar = args[3] as String
                    val avatarColor = args[4] as String
                    val id = args[5] as String
                    val timeStamp = args[6] as String

                    val newMessage = Message(msgBOdy, userName, channelId, userAvatar, avatarColor, id, timeStamp)
                    MessageService.messages.add(newMessage)

                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount-1)
                }
            }
        }
    }


    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
