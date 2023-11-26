package com.example.ccoli_market

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ccoli_market.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DetailActivity : AppCompatActivity() {

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private lateinit var binding: ActivityDetailBinding
    private var isLiked = false

    private lateinit var sellerId : String
    private lateinit var ArticleModelId : String //판매 상품의 id
    private lateinit var currentUserid : String //현재 유저의 id
    private lateinit var Articleid : String //판매 상품의 id값

    private lateinit var articleDB : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_detail)
        binding = ActivityDetailBinding.bind(findViewById(R.id.constLayout))

        //Articleid = intent.getStringExtra("id")!!
        val sellerId = intent.getStringExtra("sellerId")
        val title = intent.getStringExtra("title")
        val price = intent.getStringExtra("price")
        val content = intent.getStringExtra("content")
        val imageUrl = intent.getStringExtra("imageUrl")

        Picasso.get().load(imageUrl).into(binding.detailImage)
        articleDB = FirebaseDatabase.getInstance().getReference("Articles")

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
        //현재 사용자 아이디
        currentUserid = FirebaseAuth.getInstance().currentUser?.uid!!

        val popup = findViewById<ImageView>(R.id.iv_item_setting)
        popup.setOnClickListener {
            //내가 작성한 글인 경우에만 팝업 띄우기
            showPopupMenu(it)
        }

        findViewById<Button>(R.id.messagebutton).setOnClickListener {
            val chatIntent = Intent(this, ChattingRoomActivity::class.java)
            startActivity(chatIntent)
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
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        //if(currentUserid == sellerId) {
            inflater.inflate(R.menu.popup_menu, popupMenu.menu)
        //} else {
            //inflater.inflate(R.menu.popup_menu2, popupMenu.menu)
        //}
        //메뉴 클릭시 동작 정의
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit -> {
                    // 수정 클릭시
                    val intent = Intent(this,AddItemActivity::class.java)
                    //게시글 아이디 넣어서 화면 이동
                    //intent.putExtra("ArticleModelId", ArticleModelId) //판매 아이템 아이디 넘겨주기
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.delete -> {
                    articleDB = FirebaseDatabase.getInstance().getReference("Articles")
                    articleDB.child(ArticleModelId).removeValue() //데이터 삭제
                    finish() //현재 엑티비티 종료
                    Toast.makeText(this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    true
                }
                R.id.report -> {
                    Toast.makeText(this, "신고가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
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