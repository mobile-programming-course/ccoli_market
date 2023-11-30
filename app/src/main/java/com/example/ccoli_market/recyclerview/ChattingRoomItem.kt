package com.example.ccoli_market.recyclerview

import java.io.Serializable

// 판매 상품 아이템 DTO
data class ChattingRoomItem(
    var chatItemImage : Int, //채팅 방 이미지
    var chatUserName : String, //상대방 이름
    val lastChat : String, //마지막 채팅
    val userId: String // 상대방의 uId
): Serializable