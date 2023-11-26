package com.example.ccoli_market

import android.content.Intent
import android.os.Bundle
import android.view.View
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

    private val articleList = mutableListOf<ArticleModel>()
    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)

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

        articleList.clear()

        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        userDB = Firebase.database.reference.child(DB_USERS)
        articleAdapter = ArticleAdapter(onItemClicked = { articleModel->
            if (auth.currentUser != null){
                // todo 로그인을 한 상태

                if(auth.currentUser!!.uid != articleModel.sellerId){

                    val chatRoom = ChatListItem(
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
                        .setValue(chatRoom)

                    val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                        putExtra("sellerId", articleModel.sellerId)
                        putExtra("title", articleModel.title)
                        putExtra("price", articleModel.price)
                        putExtra("imageUrl", articleModel.imageUrl)
                        putExtra("content", articleModel.content)
                    }

                    startActivity(intent)

                    //Snackbar.make(view,"채팅방이 생성되었습니다. 채팅 탭에서 확인해주세요.", Snackbar.LENGTH_LONG).show()

                } /*else {
                    // todo 내가 올린 아이템일 때
                    Snackbar.make(view,"내가 올린 아이템 입니다.", Snackbar.LENGTH_LONG).show()

                }*/

            } else {
                // todo 로그인을 안한 상태
                Snackbar.make(view,"로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()

            }

        } )

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        fragmentHomeBinding.floatingButton.setOnClickListener{

            if(auth.currentUser != null){
                val intent = Intent(requireContext(), AddItemActivity::class.java)
                startActivity(intent)
            }else{
                Snackbar.make(view,"로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            }
        }
        articleDB.addChildEventListener(listener)

    }

    override fun onResume() {
        super.onResume()
        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        articleDB.removeEventListener(listener)
    }
}