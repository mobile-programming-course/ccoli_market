package com.example.ccoli_market

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ccoli_market.DBKey.Companion.CHILD_CHAT
import com.example.ccoli_market.DBKey.Companion.DB_ARTICLES
import com.example.ccoli_market.DBKey.Companion.DB_USERS
import com.example.ccoli_market.databinding.FragmentHomeBinding
//import com.example.ccoli_market.chatlist.ChatListItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database

import com.google.firebase.ktx.Firebase

//product_list.xml 다루는 kt파일
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding: FragmentHomeBinding? = null

    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB: DatabaseReference

    private var statusFilter: String = ""
    private val defaultValue = ""
    private var isDataInitialized = false

    private val spinnerItems = listOf("검색 필터", "전체", "판매중", "판매완료")

    private val articleList = mutableListOf<ArticleModel>()

    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            if (!isDataInitialized) {
                // 한 번만 초기화 수행
                articleList.clear()
                isDataInitialized = true
            }

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            // 중복 저장 방지를 위한 체크
            if (!articleList.contains(articleModel)) {
                articleList.add(articleModel)
                articleAdapter.submitList(articleList)
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
    }

    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        val spinner = fragmentHomeBinding.spinner
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerItems)
        spinner.adapter = spinnerAdapter

        spinner.setSelection(spinnerItems.indexOf("검색 필터"))

        // Spinner 아이템 선택 리스너 설정
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 선택된 아이템에 따라 필터링 수행
                statusFilter = spinnerItems[position]
                filterData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinner.setSelection(spinnerItems.indexOf(defaultValue))

        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        userDB = Firebase.database.reference.child(DB_USERS)
        articleAdapter = ArticleAdapter(onItemClicked = { articleModel ->
            if (auth.currentUser != null) {
                // todo 로그인을 한 상태

                /*                val chatRoom = ChatListItem(
                                    buyerId = auth.currentUser!!.uid,
                                    sellerId = articleModel.sellerId,
                                    itemTitle = articleModel.title,
                                    key = System.currentTimeMillis()
                                )

                                userDB.child(auth.currentUser!!.uid)
                                    .child(CHILD_CHAT)
                                    .push()
                                    .setValue(chatRoom)

                                userDB.child(articleModel.sellerId)
                                    .child(CHILD_CHAT)
                                    .push()
                                    .setValue(chatRoom)*/

                val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                    putExtra("articleModelId", articleModel.articleModelId)
                    putExtra("sellerId", articleModel.sellerId)
                    putExtra("userEmail", articleModel.userEmail)
                    putExtra("title", articleModel.title)
                    putExtra("price", articleModel.price)
                    putExtra("imageUrl", articleModel.imageUrl)
                    putExtra("content", articleModel.content)
                    putExtra("status", articleModel.status)
                }

                startActivity(intent)

            } else {
                // todo 로그인을 안한 상태
                Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            }
        })

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        fragmentHomeBinding.floatingButton.setOnClickListener {
            if (auth.currentUser != null) {
                val intent = Intent(requireContext(), AddItemActivity::class.java)
                startActivity(intent)
            } else {
                Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            }
        }

        // Listener 추가
        articleDB.addChildEventListener(listener)
    }

    private fun filterData() {
        // 선택된 아이템에 따라 데이터를 필터링하여 새로운 리스트 생성
        val filteredList = when (statusFilter) {
            "검색 필터" -> articleList.filter { it.status == "" }
            "판매중" -> articleList.filter { it.status == "판매중" }
            "판매완료" -> articleList.filter { it.status == "판매완료" }
            "전체" -> articleList
            else -> articleList // 기본적으로 전체 반환
        }

        // 필터링된 리스트를 어댑터에 전달하여 업데이트
        articleAdapter.submitList(filteredList)
        // 어댑터에게 데이터가 변경되었음을 알림
        articleAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        // 데이터베이스 리스너 다시 연결
        articleDB.addChildEventListener(listener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 리스트 초기화
        articleList.clear()
        // 이벤트 리스너 제거
        articleDB.removeEventListener(listener)
    }
}