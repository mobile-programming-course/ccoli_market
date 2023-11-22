package com.example.ccoli_market

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ccoli_market.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.checkerframework.checker.nullness.qual.NonNull


class DetailActivity : AppCompatActivity() {

    private var mDatabase: DatabaseReference? = null
    private var detailImage: ImageView? = null
    private var nickname: TextView? = null
    private var address: TextView? = null
    private var mannerNumber: TextView? = null
    private var mannerEmoji: TextView? = null
    private var mannerText: TextView? = null
    private var detailTitle: TextView? = null
    private var detailContent: TextView? = null
    private var price: TextView? = null

    private lateinit var binding: ActivityDetailBinding
    private var isLiked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDatabase = FirebaseDatabase.getInstance().getReference().child("articles");

        // 뷰 초기화
        detailImage = findViewById(R.id.detailImage)
        nickname = findViewById(R.id.nickname)
        address = findViewById(R.id.address)
        mannerNumber = findViewById(R.id.mannerNumber)
        mannerEmoji = findViewById(R.id.mannerEmoji)
        mannerText = findViewById(R.id.mannerText)
        detailTitle = findViewById(R.id.detailTitle)
        detailContent = findViewById(R.id.detailContent)
        price = findViewById(R.id.price)

        val articleId = intent.getStringExtra("articleId")
        if (articleId != null) {
            mDatabase?.child(articleId)?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val article = snapshot.getValue(ArticleModel::class.java)

                        // 가져온 데이터를 뷰에 설정
                        if (article != null) {
                            //Glide.with(this@DetailActivity).load(article.imageUrl).into(detailImage!!)
                            nickname!!.text = article.title // 변경
                            address!!.text = article.price // 변경
                            mannerNumber!!.text = article.createdAt.toString() // 변경
                            mannerEmoji!!.text = "" // 여기에 표시할 내용을 넣으세요
                            mannerText!!.text = "" // 여기에 표시할 내용을 넣으세요
                            detailTitle!!.text = article.title
                            detailContent!!.text = article.content // 변경
                            price!!.text = article.price
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // 데이터 가져오기 실패
                }
            })
        }

        val receivedItem = intent.getParcelableExtra<MyItem>("myItem")
        receivedItem?.let {
/*            binding.detailImage.setImageResource(it.listImage)
            binding.nickname.text = it.nickname
            binding.address.text = it.listAddress
            binding.detailTitle.text = it.listTitle
            binding.detailContent.text = it.detailContent
            binding.price.text = it.listPrice*/
            //isLiked = it.isLiked == true
            binding.detailLikeIcon.setImageResource(if (isLiked) {R.drawable.love_filled} else {R.drawable.love_empty})
            binding.backButton.setOnClickListener {
                exit()
            }
            binding.detailLikeIcon.setOnClickListener {
                if(!isLiked){
                    binding.detailLikeIcon.setImageResource(R.drawable.love_filled)
                    Snackbar.make(binding.constLayout, "관심 목록에 추가되었습니다.", Snackbar.LENGTH_SHORT).show()
                    isLiked = true
                }else {
                    binding.detailLikeIcon.setImageResource(R.drawable.love_empty)
                    isLiked = false
                }
            }
        }
/*        findViewById<Button>(R.id.button).setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }*/
    }
    private fun exit() {
        val likePosition = intent.getIntExtra("likePosition", 0)
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("likePosition", likePosition)
            putExtra("isLiked", isLiked)
        }
        setResult(RESULT_OK, intent)
        if (!isFinishing) finish()
    }
    override fun onBackPressed() {
        exit()
    }
}