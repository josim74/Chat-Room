package com.github.josim74.smack.services

import android.graphics.Color
import com.github.josim74.smack.controller.App
import java.util.*

object UserDataService {
    var id = ""
    var avatarColor = ""
    var avatarName = ""
    var email = ""
    var name = ""

    fun returnAvatarColor(components: String): Int{
        val striptColor = components.replace("[", "").replace(",", "").replace("]", "")
        var r = 0
        var g = 0
        var b = 0

        val scanner = Scanner(striptColor)
        if (scanner.hasNext()) {
            r = (scanner.nextDouble() * 255).toInt()
            g = (scanner.nextDouble() * 255).toInt()
            b = (scanner.nextDouble() * 255).toInt()
        }

        return Color.rgb(r, g, b)
    }

    fun logout() {
        id = ""
        avatarColor = ""
        avatarName = ""
        email = ""
        name = ""

        App.prefs.authToken = ""
        App.prefs.userEmail = ""
        App.prefs.isLoggedIn = false

        MessageService.cleareMessages()
        MessageService.cleareChannels()
    }
}