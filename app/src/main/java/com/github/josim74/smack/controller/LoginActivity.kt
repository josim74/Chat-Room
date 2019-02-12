package com.github.josim74.smack.controller

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.github.josim74.smack.R
import com.github.josim74.smack.services.AuthService
import com.github.josim74.smack.utils.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginProgressBar.visibility = View.INVISIBLE
    }

    fun loginLoginBtnClicked(view: View) {
        hideKeyboard()
        enableProgressBar(true)
        val email = loginEmailText.text.toString()
        val password = loginPasswordText.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            AuthService.loginUSer(email, password) {loginSuccess ->
                if (loginSuccess) {
                    AuthService.findUserByEmail(this) {findUserSuccess ->
                        if (findUserSuccess) {
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
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        }
    }

    fun loginCreateUserClicked(view: View) {
        val crateUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(crateUserIntent)
        finish()
    }


    fun errorToast() {
        Toast.makeText(this, "Something went wrong, Please try again", Toast.LENGTH_LONG).show()
        enableProgressBar(false)
    }

    fun enableProgressBar(enable: Boolean) {
        if (enable) {
            loginProgressBar.visibility = View.VISIBLE
        }else{
            loginProgressBar.visibility = View.INVISIBLE
        }
        loginLoginBtn.isEnabled = !enable
        loginCreateUserBtn.isEnabled = !enable
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
