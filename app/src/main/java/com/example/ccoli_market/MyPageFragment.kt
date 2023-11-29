package com.example.ccoli_market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

import com.example.ccoli_market.R
//import com.example.ccoli_market.databinding.FragmentChatlistBinding

import com.example.ccoli_market.databinding.FragmentMypageBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MyPageFragment : Fragment(R.layout.fragment_mypage){
    private var binding: FragmentMypageBinding? = null
    val user= Firebase.auth.currentUser
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentMypageBinding = FragmentMypageBinding.bind(view)
        binding = fragmentMypageBinding
        user?.let {
            val email = it.email
            binding?.email?.setText(email)
        }
        binding?.logoutBtn?.setOnClickListener {
            signOut()
            Snackbar.make(view,"로그아웃되었습니다.", Snackbar.LENGTH_LONG).show()
            binding?.email?.setText("로그인하시오")
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }
    private fun signOut() {
        Firebase.auth.signOut()
    }
}