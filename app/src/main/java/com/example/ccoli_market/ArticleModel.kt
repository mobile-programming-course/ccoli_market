package com.example.ccoli_market

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class ArticleModel(
    val articleModelId: String? = null, // ArticleModelId 추가
    val sellerId: String,
    val title: String,
    val createdAt: Long,
    val price: String,
    val imageUrl: String,
    val content: String,
    val status:String,
    val uri: String? = null

)
{
    constructor(): this("","","",0,"","","","")

    companion object {
        val Articles = mutableListOf<ArticleModel>()

        init {
            // Firebase Realtime Database에서 Articles 데이터를 가져옵니다.
            val database = FirebaseDatabase.getInstance()
            val articlesRef = database.getReference("articles")

            articlesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Articles를 초기화하고 데이터베이스에서 가져온 값을 추가합니다.
                    Articles.clear()
                    for (articleSnapshot in snapshot.children) {
                        val article = articleSnapshot.getValue(ArticleModel::class.java)
                        article?.let { Articles.add(it) }
                    }
                    // 가져온 데이터로 RecyclerView를 갱신하는 코드를 추가할 수 있습니다.
                    // adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // 데이터베이스에서 데이터를 가져오는 데 실패한 경우 처리할 내용을 여기에 추가합니다.
                }
            })
        }
    }
}