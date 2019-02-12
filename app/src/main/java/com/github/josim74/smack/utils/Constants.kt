package com.github.josim74.smack.utils

const val BASE_URL = "https://chattychatju.herokuapp.com/v1/"
//const val URL_REGISTER = "http://192.168.0.109:3005/v1/account/register"
const val URL_REGISTER = "$BASE_URL/account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_CREATE_USER = "${BASE_URL}user/add"
const val URL_GET_USER = "${BASE_URL}/user/byEmail/"
const val URL_GET_CHANNELS = "${BASE_URL}channel"
const val URL_GET_MESSAGES = "${BASE_URL}message/byChannel/"

const val SOCKET_URL = "https://chattychatju.herokuapp.com/"

//broadcast constants..
const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"