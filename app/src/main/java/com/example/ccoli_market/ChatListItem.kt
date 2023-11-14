package com.example.ccoli_market

data class ChatListItem(
    val buyerId: String,
    val sellerId : String,
    val itemTitle: String,
    val key: Long
){

    constructor(): this("","","",0)
}