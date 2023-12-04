package com.example.ccoli_market
class ChatModel(
    var users: HashMap<String, Boolean> = HashMap(),
    val comments: HashMap<String, Comment> = HashMap(),
    var title: String? = null,
    var price: String? = null,
    var imageUrl: String? = null,
    var articleModelId: String? = null,  // 상품 고유 아이디 추가
    var chatRoomUid: String? = null
) {
    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null)
}
