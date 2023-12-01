package com.example.ccoli_market
class ChatModel(
    val users: HashMap<String, Boolean> = HashMap(),
    val comments: HashMap<String, Comment> = HashMap(),
    val title: String? = null,
    val price: String? = null,
    val imageUrl: String? = null,
    var articleModelId: String? = null,  // 상품 고유 아이디 추가
    var chatRoomUid: String? = null
) {
    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null)
}
