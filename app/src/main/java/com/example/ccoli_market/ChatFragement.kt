package com.example.ccoli_market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment() {
    companion object{
        fun newInstance() : ChatFragment {
            return ChatFragment()
        }
    }
    private val fireDatabase = FirebaseDatabase.getInstance().reference
    //메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    //프레그먼트를 포함하고 있는 액티비티에 붙었을 때
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    //뷰가 생성되었을 때
    //프레그먼트와 레이아웃을 연결시켜주는 부분
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview_chatting_room)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()

        return view
    }
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        private val chatModel = ArrayList<ChatModel>()
        private var uid : String? = null
        private val destinationUsers : ArrayList<String> = arrayListOf()

        init {
            uid = Firebase.auth.currentUser?.uid.toString()
            println(uid)
            fireDatabase.child("chatrooms").orderByChild("users/$uid").equalTo(true).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatModel.clear()
                    destinationUsers.clear()  // 리스트 초기화
                    for(data in snapshot.children){
                        println(data)
                        val model = data.getValue<ChatModel>()!!
                        model.chatRoomUid = data.key
                        model?.let {
                            fetchArticleData(it)
                            chatModel.add(it)
                        }
                        for (user in model.users.keys) {
                            if (!user.equals(uid)) {
                                destinationUsers.add(user)
                            }
                        }
                    }
                    notifyDataSetChanged()
                }
            })

        }
        private fun fetchArticleData(chatModel: ChatModel) {
            fireDatabase.child("Articles").child(chatModel.articleModelId.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(articleSnapshot: DataSnapshot) {
                        val article = articleSnapshot.getValue<ArticleModel>()
                        article?.let {
                            chatModel.title = it.title
                            chatModel.price = it.price
                            chatModel.imageUrl = it.imageUrl
                        }
                        notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // 에러 처리
                        Log.e("Firebase Error", "Failed to retrieve article data: ${error.message}")
                    }
                })
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chatting_room, parent, false))
        }
        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            //            val imageView: ImageView = itemView.findViewById(R.id.cardview_chat_user)
            val titletv : TextView = itemView.findViewById(R.id.tv_id)
            val lastMessagetv : TextView = itemView.findViewById(R.id.tv_last_chat)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            var destinationUid: String? = null
            //채팅방에 있는 유저 모두 체크
            for (user in chatModel[position].users.keys) {
                if (!user.equals(uid)) {
                    destinationUid = user
                    destinationUsers.add(destinationUid)
                }
            }

            fireDatabase.child("users").child("$destinationUid").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    val friend = snapshot.getValue<User>()
                    holder.titletv.text = friend?.name
                }
            })
            //메세지 내림차순 정렬 후 마지막 메세지의 키값을 가져
            val commentMap = TreeMap<String, ChatModel.Comment>(reverseOrder())
            commentMap.putAll(chatModel[position].comments)
            val lastMessageKey = commentMap.keys.toTypedArray()[0]
            holder.lastMessagetv.text = chatModel[position].comments[lastMessageKey]?.message

            //채팅창 선택 시 이동
            holder.itemView.setOnClickListener {
                val intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("destinationUid", destinationUsers[position])
                intent.putExtra("title", chatModel[position].title)
                intent.putExtra("price", chatModel[position].price)
                intent.putExtra("imageUrl", chatModel[position].imageUrl)
                intent.putExtra("chatRoomUid", chatModel[position].chatRoomUid)
                intent.putExtra("articleModelId", chatModel[position].articleModelId) // 이 부분 추가
                context?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return chatModel.size
        }
    }
}