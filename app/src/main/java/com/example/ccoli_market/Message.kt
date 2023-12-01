package com.example.ccoli_market

// 메시지
data class Message(
    var message: String?,
    var sendId: String?
) {
    constructor() : this("", "")
}