package com.example.ccoli_market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_page)

        val checkBox = findViewById<CheckBox>(R.id.sign_up_checkBox)
        val signUpButton = findViewById<Button>(R.id.sign_up_page_btn)

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

    private fun updateButtonListener(button: Button, isChecked: Boolean) {
        button.setOnClickListener {
            if (isChecked) {
                signUp()
                showToast(this, "가입에 성공하였습니다.")
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

        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = Firebase.auth.currentUser
                }
            }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}