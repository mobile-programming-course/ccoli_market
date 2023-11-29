package com.example.ccoli_market

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.ccoli_market.DBKey.Companion.DB_ARTICLES
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class AddItemActivity() : AppCompatActivity() {
    private var selectedUri: Uri? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private val articleDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_ARTICLES)
    }
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    findViewById<ImageView>(R.id.imageView)?.setImageURI(uri)
                    selectedUri = uri
                }
            } else {
                Toast.makeText(this,"사진을 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    private fun startContentProvider() {
        Log.d("AddItemFragment", "startContentProvider() called")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_item)

        findViewById<Button>(R.id.uploadbtn).setOnClickListener {
            startContentProvider()
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startContentProvider()
                }

                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1010
                    )
                }
            }
        }
        findViewById<Button>(R.id.addbutton).setOnClickListener {
            val title = findViewById<EditText>(R.id.et_title).text.toString()
            val price = findViewById<EditText>(R.id.et_price).text.toString()
            val content = findViewById<EditText>(R.id.et_content).text.toString()
            val sellerId = auth.currentUser?.uid.orEmpty()
            val userEmail = auth.currentUser?.email.orEmpty()
            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                uploadPhoto(
                    photoUri,
                    successHandler = { uri ->
                        uploadArticle(sellerId,userEmail, title, price, uri, content,"판매중")
                        finish()
                    },
                    errorHandler = {
                        Toast.makeText(this, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                uploadArticle(sellerId,userEmail, title, price, "",content,"판매중")
                finish()
            }

        }
    }
    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child("Add_item").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.reference.child("Add_item").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { downloadUrl ->
                            successHandler(downloadUrl.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }
    private fun uploadArticle(sellerId: String,userEmail:String, title: String, price: String, imageUrl: String, content:String,status:String) {
        val model = ArticleModel(null,sellerId,userEmail, title, System.currentTimeMillis(), "$price 원", imageUrl,content,status)
        //articleDB.push().setValue(model)
        val newRef = articleDB.push()
        newRef.setValue(model.copy(articleModelId = newRef.key))

/*        // 새로 추가된 게시글의 키를 가져와서 Intent에 담아 다음 액티비티로 전달
        val articleId = newRef.key
        val intent = Intent(this, HomeFragment::class.java)
        intent.putExtra("articleModelId", articleId)
        startActivity(intent)*/
    }


    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 가져오기 위해 필요합니다")
            .setPositiveButton("동의") { _, _ ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    1010
                )
            }
            .create()
            .show()
    }
}