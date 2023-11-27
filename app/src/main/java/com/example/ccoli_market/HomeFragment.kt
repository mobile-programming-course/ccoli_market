package com.example.ccoli_market

import android.app.Activity
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
                    putExtra("articleModelId", articleModel.articleModelId)
                    putExtra("sellerId", articleModel.sellerId)
                    putExtra("title", articleModel.title)
                    putExtra("price", articleModel.price)
                    putExtra("imageUrl", articleModel.imageUrl)
                    putExtra("content", articleModel.content)
                    putExtra("status",articleModel.status)
                }

                startActivity(intent)

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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == DetailActivity.EDIT_ITEM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // 수정된 데이터를 받아서 화면 갱신 등의 작업을 수행
            val editedArticleModelId = data?.getStringExtra("articleModelId")
            val editedTitle = data?.getStringExtra("editedTitle")
            val editedPrice = data?.getStringExtra("editedPrice")
            val editedContent = data?.getStringExtra("editedContent")
            val editedStatus= data?.getStringExtra("editedStatus")
            // 여기에서 받은 데이터를 사용하여 화면을 갱신하는 작업을 수행
            if (editedArticleModelId != null && editedTitle != null && editedPrice != null && editedContent != null&&editedStatus!=null) {
                // 수정된 아이템을 찾아서 업데이트
                val index = articleList.indexOfFirst { it.articleModelId == editedArticleModelId }
                if (index != -1) {
                    val editedArticle = ArticleModel(
                        articleModelId = editedArticleModelId,
                        sellerId = auth.currentUser?.uid ?: "",
                        title = editedTitle,
                        createdAt = System.currentTimeMillis(),
                        price = editedPrice,
                        imageUrl = "", // TODO: 이미지 URL에 대한 처리가 필요함
                        content = editedContent,
                        status=editedStatus
                    )
                    articleList[index] = editedArticle
                    articleAdapter.notifyItemChanged(index)
                }
            }
        }
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