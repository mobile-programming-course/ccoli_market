package com.example.ccoli_market

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

private lateinit var auth: FirebaseAuth
lateinit var database: DatabaseReference

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_page)
        auth = Firebase.auth
        database = Firebase.database.reference

        //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        val email = findViewById<EditText>(R.id.sign_up_email).text
        val password = findViewById<EditText>(R.id.sign_up_password).text
        val name = findViewById<EditText>(R.id.editTextName).text
        val button = findViewById<Button>(R.id.sign_up_page_btn)
        var profileCheck = false

        val intent = Intent(this, LoginActivity::class.java)

        button.setOnClickListener {
            if (email.isEmpty() && password.isEmpty() && name.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 제대로 입력해주세요.", Toast.LENGTH_SHORT).show()
                Log.d("Email", "$email, $password")
            } else {
                auth.createUserWithEmailAndPassword(email.toString(), password.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = Firebase.auth.currentUser
                            val userId = user?.uid
                            val userIdSt = userId.toString()

                            val friend = User(name.toString(),email.toString(),  userIdSt)
                            database.child("users").child(userId.toString()).setValue(friend)

                            Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "$userId")
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            reload();
        }
    }
    private fun reload() {
    }
    companion object {
        private const val TAG = "EmailPassword"
    }
}