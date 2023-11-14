package com.example.ccoli_market

data class ChatItem (
    val time: String,
    val senderId: String,
    val message: String
){
    constructor():this("","","")
}