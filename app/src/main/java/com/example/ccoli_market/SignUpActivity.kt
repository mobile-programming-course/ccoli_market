package com.example.ccoli_market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_page)
        database = Firebase.database.reference
        auth = Firebase.auth

        val checkBox = findViewById<CheckBox>(R.id.sign_up_checkBox)
        val signUpButton = findViewById<Button>(R.id.sign_up_page_btn)
        val backBtn = findViewById<ImageButton>(R.id.backBtn)

        backBtnListener(backBtn)

        // 초기값 설정
        var isChecked = false

        // 체크박스 상태 변경 리스너
        checkBox.setOnCheckedChangeListener { _, isCheckedNow ->
            isChecked = isCheckedNow
            updateButtonListener(signUpButton, isChecked)
        }

        // 초기 버튼 리스너 설정
        updateButtonListener(signUpButton, isChecked)
    }

    private fun backBtnListener(backBtn: ImageButton) {
        backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun updateButtonListener(button: Button, isChecked: Boolean) {
        button.setOnClickListener {
            if (isChecked) {
                signUp()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                showToast(this, "정보제공 동의해주세요.")
            }
        }
    }

    private fun signUp() {
        val email = findViewById<EditText>(R.id.sign_up_email).text.toString()
        val password = findViewById<EditText>(R.id.sign_up_password).text.toString()
        val name=findViewById<EditText>(R.id.editTextName).text.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast(this, "회원가입에 성공하였습니다.")
//                    val user = auth.currentUser
                    val uId = Firebase.auth.currentUser?.uid!! //현재 사용자의 uid
                    addUser(name,email, uId)
                }
                else{
                    showToast(this, "회원가입에 실패하였습니다.")
                }
            }
        showToast(this, "회원가입에 성공하였습니다.")
        val uId = Firebase.auth.currentUser?.uid!!
        addUser(name,email, uId)
    }
    private fun addUser(name:String,email: String,uId:String){
        val user = User(name = name, email = email,uId=uId)
        database.child("userInfo").child(uId).setValue(user)
//        database.child("$uId").child("email").setValue(User(email))

//        database.child("userInfo").child(uId).setValue(User(name,email,uId))

    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}