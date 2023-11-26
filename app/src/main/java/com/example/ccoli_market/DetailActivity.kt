package com.example.ccoli_market

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ccoli_market.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class DetailActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private lateinit var binding: ActivityDetailBinding
    private var isLiked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_detail)
        binding = ActivityDetailBinding.bind(findViewById(R.id.constLayout))

        val sellerId = intent.getStringExtra("sellerId")
        val title = intent.getStringExtra("title")
        val price = intent.getStringExtra("price")
        val content = intent.getStringExtra("content")
        val imageUrl = intent.getStringExtra("imageUrl")

        Picasso.get().load(imageUrl).into(binding.detailImage)

        binding.nickname.text = sellerId
        binding.detailTitle.text = title
        binding.detailContent.text = content
        binding.price.text = price
        binding.detailLikeIcon.setImageResource(if (isLiked) R.drawable.love_filled else R.drawable.love_empty)

        if (auth.currentUser != null && auth.currentUser!!.uid == sellerId) {
            // 판매자가 자신의 상품일 경우
            showToast("내가 올린 상품입니다.")
            disableButton(findViewById<Button>(R.id.messagebutton))
        } else {
            binding.detailLikeIcon.setOnClickListener {
                // 상품에 대한 다른 동작 수행
                if (!isLiked) {
                    binding.detailLikeIcon.setImageResource(R.drawable.love_filled)
                    showToast("관심 목록에 추가되었습니다.")
                    isLiked = true
                } else {
                    binding.detailLikeIcon.setImageResource(R.drawable.love_empty)
                    isLiked = false
                }
            }

            findViewById<Button>(R.id.messagebutton).setOnClickListener {
                // 판매자가 자신의 상품이 아닌 경우에만 동작
                if (auth.currentUser != null && auth.currentUser!!.uid != sellerId) {
                    val chatIntent = Intent(this, ChattingRoomActivity::class.java)
                    startActivity(chatIntent)
                } else {
                    showToast("내가 올린 상품입니다.")
                }
            }
        }
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun disableButton(button: Button) {
        button.isEnabled = false
        button.isClickable = false
        button.alpha = 0.5f
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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