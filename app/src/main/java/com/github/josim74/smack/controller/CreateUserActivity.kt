package com.github.josim74.smack.controller

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.github.josim74.smack.R
import com.github.josim74.smack.services.AuthService
import com.github.josim74.smack.services.UserDataService
import com.github.josim74.smack.utils.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {
    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        createProgressBar.visibility = View.INVISIBLE
    }

    fun generateUserAvater(view: View) {
        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        if (color == 0) {
            userAvatar = "light$avatar"
        }else{
            userAvatar = "dark$avatar"
        }

        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarImageView.setImageResource(resourceId)

    }

    fun generateColorClicked(view: View) {
        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r,g,b))

        val savedR = r.toDouble()/255
        val savedG = g.toDouble()/255
        val savedB = b.toDouble()/255

        avatarColor = "[$savedR, $savedG, $savedB]"

        println(avatarColor)
    }

    fun createUserClicked(view: View) {
        enableProgressBar(true)
        var userName = createUserNameText.text.toString()
        var email = createEmailText.text.toString()
        var password = createPasswordText.text.toString()
        if (userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            AuthService.registerUser(email,password){registerSuccess ->
                if (registerSuccess) {
                    AuthService.loginUSer(email, password ){loginSuccess ->
                        if (loginSuccess) {
                            Log.d("AUTH_TOKEN", "user auth:: "+App.prefs.authToken)
                            AuthService.createUser(userName, email, userAvatar, avatarColor ){userCreateSuccess ->
                                if (userCreateSuccess) {
                                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                                    enableProgressBar(false)
                                    finish()
                                }else{
                                    errorToast()
                                }

                            }

                        }else{
                            errorToast()
                        }
                    }
                }else{
                    errorToast()
                }
            }
        }else{
            Toast.makeText(this, "Make sure username, email and password is not empty", Toast.LENGTH_LONG).show()
            enableProgressBar(false)
        }
    }

    fun errorToast() {
        Toast.makeText(this, "Something went wrong, Please try again", Toast.LENGTH_LONG).show()
        enableProgressBar(false)
    }

    fun enableProgressBar(enable: Boolean) {
        if (enable) {
            createProgressBar.visibility = View.VISIBLE
        }else{
            createProgressBar.visibility = View.INVISIBLE
        }
        createUserBtn.isEnabled = !enable
        createAvatarImageView.isEnabled = !enable
        backgroundColorBtn.isEnabled = !enable
    }
}
