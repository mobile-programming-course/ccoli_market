package com.example.ccoli_market

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class MessageActivity:AppCompatActivity() {
    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var chatRoomUid: String? = null
    private var destinationUid: String? = null
    private var uid: String? = null
    private var recyclerView: RecyclerView? = null
    private lateinit var sendbtn: Button
    private var articleModelId: String? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting_room)

        val editText = findViewById<EditText>(R.id.et_message)
        //메세지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()
        val userEmail = intent.getStringExtra("userEmail")
        val title = intent.getStringExtra("title")
        val price = intent.getStringExtra("price")
        val imageUrl = intent.getStringExtra("imageUrl")
        articleModelId = intent.getStringExtra("articleModelId")
        chatRoomUid = intent.getStringExtra("chatRoomUid")

        findViewById<ImageButton>(R.id.chatBackButton).setOnClickListener {
            onBackPressed()
        }

        // 받아온 데이터를 각각의 TextView나 ImageView에 설정
        findViewById<TextView>(R.id.nickname).text = userEmail
        findViewById<TextView>(R.id.detailTitle).text = title
        findViewById<TextView>(R.id.price).text = price
        Picasso.get().load(imageUrl).into(findViewById<ImageView>(R.id.detailImage))
        sendbtn = findViewById<Button>(R.id.btn_send_message)
        destinationUid = intent.getStringExtra("destinationUid")
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.recyclerView)

        sendbtn.setOnClickListener {
            val comment = ChatModel.Comment(uid, editText.text.toString(), curTime)

            if (chatRoomUid == null) {
                val chatModel = ChatModel()
                chatModel.users[uid.toString()] = true
                chatModel.users[destinationUid.toString()] = true
                chatModel.articleModelId = articleModelId

                // 채팅방의 고유 아이디 생성
                val chatRoomRef = fireDatabase.child("chatrooms").push()
                chatRoomUid = chatRoomRef.key
                chatModel.chatRoomUid = chatRoomUid  // 채팅방의 chatRoomUid를 설정

                chatRoomRef.setValue(chatModel).addOnSuccessListener {
                    // 메시지 보내기
                    chatRoomRef.child("comments").push().setValue(comment).addOnSuccessListener {
                        editText.text = null
                        initRecyclerViewAdapter()  // 새로운 메시지를 추가한 후 RecyclerView를 업데이트합니다.
                    }
                }
            } else {
                fireDatabase.child("chatrooms").child(chatRoomUid!!)
                    .child("comments").push().setValue(comment).addOnSuccessListener {
                        editText.text = null
                        initRecyclerViewAdapter()  // 새로운 메시지를 추가한 후 RecyclerView를 업데이트합니다.
                    }
            }
        }

        checkChatRoom()
    }
    private fun checkChatRoom() {
        fireDatabase.child("chatrooms").orderByChild("users/$uid").equalTo(true)

        //fireDatabase.child("chatrooms").orderByChild("articleModelId").equalTo(articleModelId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) {
                        val chatModel = item.getValue<ChatModel>()
                        if (chatModel?.users!!.containsKey(uid) && chatModel.users.containsKey(destinationUid) && chatModel.articleModelId == articleModelId) {
                            chatRoomUid = item.key
                            sendbtn.isEnabled = true
                            initRecyclerViewAdapter()
                            break  // 일치하는 첫 번째 채팅방을 찾으면 루프를 종료합니다.
                        }
                    }
                }
            })
    }
    private fun initRecyclerViewAdapter() {
        val recyclerViewAdapter = RecyclerViewAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
        recyclerView?.adapter = recyclerViewAdapter
    }
    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>() {
        private val comments = ArrayList<ChatModel.Comment>()
        private var friend : User? = null
        val nickname=findViewById<TextView>(R.id.nickname)
        init{
            fireDatabase.child("users").child(destinationUid.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {//users/(보낸사람uid)
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    friend = snapshot.getValue<User>()
                    nickname.text = friend?.name
                    getMessageList()
                }
            })
        }

        fun getMessageList() {
            fireDatabase.child("chatrooms")
                .orderByChild("articleModelId").equalTo(articleModelId) // articleModelId를 이용한 필터링
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        comments.clear()
                        for (data in snapshot.children) {
                            val chatModel = data.getValue<ChatModel>()
                            if (chatModel?.chatRoomUid == chatRoomUid) { // chatRoomUid가 일치하는 데이터만 선택
                                for (commentSnapshot in data.child("comments").children) {
                                    val comment = commentSnapshot.getValue<ChatModel.Comment>()
                                    comment?.let {
                                        comments.add(it)
                                    }
                                }
                            }
                        }
                        notifyDataSetChanged()
                        recyclerView?.scrollToPosition(comments.size - 1)
                    }
                })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return MessageViewHolder(view)
        }
        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.tvmessage.textSize = 15F
            holder.tvmessage.text = comments[position].message
            holder.textView_time.text = comments[position].time
            if(comments[position].uid.equals(uid)){ // 본인 채팅
                holder.tvmessage.setBackgroundResource(R.drawable.rightbubble)
                holder.tvname.visibility = View.INVISIBLE
                holder.layoutdestination.visibility = View.INVISIBLE
                holder.layoutmain.gravity = Gravity.RIGHT
                val params = holder.layoutmain.layoutParams as ViewGroup.MarginLayoutParams
                val marginInDp = 25 // 오른쪽마진 (원하는 크기로 조절)
                val marginInPx = (marginInDp * holder.itemView.context.resources.displayMetrics.density).toInt()
                params.rightMargin = marginInPx
            }else{ // 상대방 채팅
//                Glide.with(holder.itemView.context)
//                    .load(friend?.profileImageUrl)
//                    .apply(RequestOptions().circleCrop())
//                    .into(holder.imageView_profile)
                holder.tvname.text = friend?.name
                //holder.tvname.text = friend?.name
                holder.layoutdestination.visibility = View.VISIBLE
                holder.tvname.visibility = View.VISIBLE
                holder.tvmessage.setBackgroundResource(R.drawable.leftbubble)
                holder.layoutmain.gravity = Gravity.LEFT
            }
        }

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvmessage:TextView=view.findViewById(R.id.messageItem_textView_message)
            val tvname: TextView = view.findViewById(R.id.messageItem_textview_name)
            //val imageView_profile: ImageView = view.findViewById(R.id.profileImage)
            val layoutdestination: LinearLayout = view.findViewById(R.id.messageItem_layout_destination)
            val layoutmain: LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time : TextView = view.findViewById(R.id.messageItem_textView_time)
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }
}