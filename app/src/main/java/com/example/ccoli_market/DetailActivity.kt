package com.example.ccoli_market

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.ccoli_market.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var isLiked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_detail)
        binding = ActivityDetailBinding.bind(findViewById(R.id.constLayout))

        val sellerId = intent.getStringExtra("sellerId")
        val title = intent.getStringExtra("title")
        val price = intent.getStringExtra("price")
        val uri = intent.getStringExtra("uri")
        val content = intent.getStringExtra("content")

        Log.d("DetailActivity", "sellerId: $sellerId, title: $title, price: $price, uri: $uri, content: $content")

        //binding.detailImage.setImageResource(/* 이미지 리소스 ID 또는 URI */)
        binding.nickname.text = sellerId
        binding.detailTitle.text = title
        binding.detailContent.text = content
        binding.price.text = price
        binding.detailLikeIcon.setImageResource(if (isLiked) R.drawable.love_filled else R.drawable.love_empty)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.detailLikeIcon.setOnClickListener {
            if (!isLiked) {
                binding.detailLikeIcon.setImageResource(R.drawable.love_filled)
                Snackbar.make(binding.constLayout, "관심 목록에 추가되었습니다.", Snackbar.LENGTH_SHORT).show()
                isLiked = true
            } else {
                binding.detailLikeIcon.setImageResource(R.drawable.love_empty)
                isLiked = false
            }
        }
        findViewById<Button>(R.id.messagebutton).setOnClickListener {
            val chatIntent = Intent(this, ChattingRoomActivity::class.java)
            startActivity(chatIntent)
        }
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        binding.backButton.setOnClickListener {
            // product_list.xml 파일의 액티비티로 이동
            val intent = Intent(this, HomeFragment::class.java)
            startActivity(intent)
        }
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