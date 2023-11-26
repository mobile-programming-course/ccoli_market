package com.example.ccoli_market
//사용자 이름, 이메일(아이디), 단과대학, UID
data class User(
    var colorId:Int = 0,
    var name: String = "",
    var email: String = "",
    var school: String = "",
    var uId: String = ""
)

//class User {
//    var name: String? = null
//    var profileImgUrl: String? = null
//    var uid: String? = null
//    var pushToken: String? = null
//}