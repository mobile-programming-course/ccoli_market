package com.example.ccoli_market

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ccoli_market.databinding.ActivityEditItemBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class EditItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditItemBinding
    private lateinit var articleDB: DatabaseReference
    private lateinit var articleModelId: String
    private var originalImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 기존의 이미지 URL을 가져와서 저장
        originalImageUrl = intent.getStringExtra("imageUrl")
        val imageUrl = intent.getStringExtra("imageUrl")

        // 기존의 이미지를 표시
        Picasso.get().load(imageUrl).into(binding.editImage)
        // Firebase 데이터베이스 레퍼런스 설정
        articleDB = FirebaseDatabase.getInstance().getReference("Articles")

        // Intent에서 ArticleModelId 가져오기
        articleModelId = intent.getStringExtra("articleModelId").toString()

        // 기존의 게시글 내용을 불러와 EditText에 표시
        loadArticleDetails(articleModelId)
        binding.radiogroup.check(R.id.radioButton)
        // 수정 버튼 클릭 시 동작
        binding.editButton.setOnClickListener {
            // EditText에서 수정된 내용 가져오기
            val editedTitle = binding.editTitle.text.toString()
            val editedPrice = binding.editPrice.text.toString()
            val editedContent = binding.editContent.text.toString()
            val checkedRadioButtonId = binding.radiogroup.checkedRadioButtonId
            val editedStatus = when (checkedRadioButtonId) {
                R.id.radioButton -> "판매중"
                R.id.radioButton2 -> "판매완료"
                else -> "판매중" // 선택된 라디오 버튼이 없을 경우 기본값 설정
            }
            // 수정된 내용으로 데이터베이스 업데이트
            updateArticle(articleModelId, editedTitle, editedPrice, editedContent, editStatus = editedStatus)

            // 수정 결과를 DetailActivity로 돌려주기
            val resultIntent = Intent()
            resultIntent.putExtra("articleModelId", articleModelId)
            resultIntent.putExtra("editedTitle", editedTitle)
            resultIntent.putExtra("editedPrice", editedPrice)
            resultIntent.putExtra("editedContent", editedContent)
            resultIntent.putExtra("editedStatus",editedStatus)
            setResult(RESULT_OK, resultIntent)

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // 화면 종료
            finish()
        }
    }

    private fun loadArticleDetails(articleModelId: String) {
        // Firebase에서 기존 게시글의 내용 불러오기
        // 이 부분은 실제 데이터베이스의 구조에 따라 수정이 필요할 수 있습니다.
        articleDB.child(articleModelId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val title = snapshot.child("title").getValue(String::class.java)
                val price = snapshot.child("price").getValue(String::class.java)
                val content = snapshot.child("content").getValue(String::class.java)
                // 가져온 내용을 EditText에 표시
                binding.editTitle.setText(title)
                binding.editPrice.setText(price)
                binding.editContent.setText(content)
            } else {
                Toast.makeText(this, "게시글이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "데이터를 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            Log.e("LoadDataError", "Error getting data", exception)
            finish()
        }
    }


    private fun updateArticle(
        articleModelId: String,
        editedTitle: String,
        editedPrice: String,
        editedContent: String,
        editStatus:String
    ) {
        // Firebase 데이터베이스에서 해당 게시글 업데이트
        // 이 부분은 실제 데이터베이스의 구조에 따라 수정이 필요할 수 있습니다.
        val updateData = mapOf(
            "title" to editedTitle,
            "price" to editedPrice,
            "content" to editedContent,
            "status" to editStatus
            // 필요한 경우 다른 필드도 추가할 수 있습니다.
        )

        articleDB.child(articleModelId).updateChildren(updateData)
    }
}
