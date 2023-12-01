package com.example.ccoli_market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_page)

        // Check if user is already signed in
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            // User is already signed in
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.sign_in_btn)?.setOnClickListener {
            val userEmail = findViewById<EditText>(R.id.sign_in_email)?.text.toString()
            val password = findViewById<EditText>(R.id.sign_in_password)?.text.toString()
            doLogin(userEmail, password)
        }
        findViewById<Button>(R.id.sign_up_btn)?.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
    private fun doLogin(userEmail: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    // Sign in success, update UI with the signed-in user's information
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "로그인에 실패하셨습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
