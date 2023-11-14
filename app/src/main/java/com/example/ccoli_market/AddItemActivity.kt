package com.example.ccoli_market

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddItemActivity :AppCompatActivity(){
    private lateinit var imageView:ImageView
    private val root:DatabaseReference=FirebaseDatabase.getInstance().getReference("Image")
    private val reference:StorageReference=FirebaseStorage.getInstance().getReference()
    private var imageUri: Uri?=null
    private val activityResult=registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result:ActivityResult->
        if(result.resultCode== Activity.RESULT_OK&&result.data!=null){
            imageUri=result.data!!.data
            imageView.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_item)
        val uploadbtn=findViewById<Button>(R.id.uploadbtn)
        imageView=findViewById(R.id.imageView)
        imageView.setOnClickListener {
            val galleryIntent= Intent().apply{
                action=Intent.ACTION_GET_CONTENT
                type="image/"
            }
            activityResult.launch(galleryIntent)
        }
        uploadbtn.setOnClickListener {
            imageUri?.let { uri->
                uploadToFirebase(uri)
            }?:run{
                Toast.makeText(this@AddItemActivity, "사진을 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun uploadToFirebase(uri: Uri){
        val fileRef: StorageReference = reference.child("${System.currentTimeMillis()}.${getFileExtension(uri)}")
        fileRef.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    // 이미지 모델에 담기
                    val model = Model(downloadUri.toString())

                    // 키로 아이디 생성
                    val modelId: String = root.push().key!!

                    // 데이터 넣기
                    root.child(modelId).setValue(model)

                    Toast.makeText(this@AddItemActivity, "업로드 성공", Toast.LENGTH_SHORT).show()

                    imageView.setImageResource(R.drawable.ic_camera)
                }
            }

    }
    // 파일 타입 가져오기
    private fun getFileExtension(uri: Uri): String? {
        val cr: ContentResolver = contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri))

    }
}
