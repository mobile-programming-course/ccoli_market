//package com.example.ccoli_market
//
//import android.R
//import android.content.DialogInterface
//import android.content.Intent
//import android.os.Bundle
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.EditText
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.request.RequestOptions
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ServerValue
//import com.google.firebase.database.ValueEventListener
//import java.text.SimpleDateFormat
//
//
//class MessageActivity : AppCompatActivity() {
//    private var chatRoomUid: String? = null //채팅방 하나 id
//    private var myuid: String? = null //나의 id
//    private var destUid: String? = null //상대방 uid
//    private var recyclerView: RecyclerView? = null
//    private var button: Button? = null
//    private var editText: EditText? = null
//    private var firebaseDatabase: FirebaseDatabase? = null
//    private var destUser: User? = null
//    private val simpleDateFormat: SimpleDateFormat = SimpleDateFormat("yyy.MM.dd HH:mm")
//    override fun onBackPressed() {
//        val intent = Intent(this@MessageActivity, MainHome::class.java)
//        intent.flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                or Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//        overridePendingTransition(R.anim.in_left, R.anim.out_right)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_message)
//        init()
//        sendMsg()
//    }
//
//    private fun init() {
//        myuid = FirebaseAuth.getInstance().currentUser!!.uid
//        destUid = intent.getStringExtra("destUid") //채팅 상대
//        recyclerView = findViewById<View>(R.id.message_recyclerview) as RecyclerView
//        button = findViewById<View>(R.id.message_btn) as Button
//        editText = findViewById<View>(R.id.message_editText) as EditText
//        firebaseDatabase = FirebaseDatabase.getInstance()
//        if (editText!!.text.toString() == null) button!!.isEnabled = false else button!!.isEnabled =
//            true
//        checkChatRoom()
//    }
//
//    private fun sendMsg() {
//        button!!.setOnClickListener(object : DialogInterface.OnClickListener() {
//            fun onClick(v: View?) {
//                val chatModel = ChatModel()
//                chatModel.users.put(myuid, true)
//                chatModel.users.put(destUid, true)
//
//                //push() 데이터가 쌓이기 위해 채팅방 key가 생성
//                if (chatRoomUid == null) {
//                    Toast.makeText(this@MessageActivity, "채팅방 생성", Toast.LENGTH_SHORT).show()
//                    button!!.isEnabled = false
//                    firebaseDatabase!!.reference.child("chatrooms").push().setValue(chatModel)
//                        .addOnSuccessListener { checkChatRoom() }
//                } else {
//                    sendMsgToDataBase()
//                }
//            }
//        })
//    }
//
//    //작성한 메시지를 데이터베이스에 보낸다.
//    private fun sendMsgToDataBase() {
//        if (editText!!.text.toString() != "") {
//            val comment = ChatModel.Comment()
//            comment.uid = myuid
//            comment.message = editText!!.text.toString()
//            comment.timestamp = ServerValue.TIMESTAMP
//            firebaseDatabase!!.reference.child("chatrooms").child(chatRoomUid!!).child("comments")
//                .push().setValue(comment).addOnSuccessListener {
//                    editText!!.setText("")
//                }
//        }
//    }
//
//    private fun checkChatRoom() {
//        //자신 key == true 일때 chatModel 가져온다.
//        /* chatModel
//        public Map<String,Boolean> users = new HashMap<>(); //채팅방 유저
//        public Map<String, ChatModel.Comment> comments = new HashMap<>(); //채팅 메시지
//        */
//        firebaseDatabase!!.reference.child("chatrooms").orderByChild("users/$myuid").equalTo(true)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(@NonNull snapshot: DataSnapshot) {
//                    for (dataSnapshot in snapshot.children)  //나, 상대방 id 가져온다.
//                    {
//                        val chatModel = dataSnapshot.getValue(ChatModel::class.java)
//                        if (chatModel!!.users.containsKey(destUid)) {           //상대방 id 포함돼 있을때 채팅방 key 가져옴
//                            chatRoomUid = dataSnapshot.key
//                            button!!.isEnabled = true
//
//                            //동기화
//                            recyclerView!!.layoutManager = LinearLayoutManager(this@MessageActivity)
//                            recyclerView!!.adapter = RecyclerViewAdapter()
//
//                            //메시지 보내기
//                            sendMsgToDataBase()
//                        }
//                    }
//                }
//
//                override fun onCancelled(@NonNull error: DatabaseError) {}
//            })
//    }
//
//    //===============채팅 창===============//
//    internal inner class RecyclerViewAdapter :
//        RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
//        var comments: MutableList<ChatModel.Comment?>
//
//        init {
//            comments = ArrayList()
//            getDestUid()
//        }
//
//        //상대방 uid 하나(single) 읽기
//        private fun getDestUid() {
//            firebaseDatabase!!.reference.child("users").child(destUid!!)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(@NonNull snapshot: DataSnapshot) {
//                        destUser = snapshot.getValue(User::class.java)
//
//                        //채팅 내용 읽어들임
//                        messageList
//                    }
//
//                    override fun onCancelled(@NonNull error: DatabaseError) {}
//                })
//        }
//
//        private val messageList: Unit
//            //채팅 내용 읽어들임
//            private get() {
//                FirebaseDatabase.getInstance().reference.child("chatrooms").child(chatRoomUid!!)
//                    .child("comments").addValueEventListener(object : ValueEventListener {
//                        override fun onDataChange(@NonNull snapshot: DataSnapshot) {
//                            comments.clear()
//                            for (dataSnapshot in snapshot.children) {
//                                comments.add(
//                                    dataSnapshot.getValue(
//                                        ChatModel.Comment::class.java
//                                    )
//                                )
//                            }
//                            notifyDataSetChanged()
//                            recyclerView!!.scrollToPosition(comments.size - 1)
//                        }
//
//                        override fun onCancelled(@NonNull error: DatabaseError) {}
//                    })
//            }
//
//        @NonNull
//        override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
//            val view: View =
//                LayoutInflater.from(parent.context).inflate(R.layout.item_messagebox, parent, false)
//            return ViewHolder(view)
//        }
//
//        override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
//            val viewHolder = holder
//            if (comments[position]!!.uid == myuid) //나의 uid 이면
//            {
//                //나의 말풍선 오른쪽으로
//                viewHolder.textViewMsg.text = comments[position]!!.message
//                viewHolder.textViewMsg.setBackgroundResource(R.drawable.rightbubble)
//                viewHolder.linearLayoutDest.visibility = View.INVISIBLE //상대방 레이아웃
//                viewHolder.linearLayoutRoot.gravity = Gravity.RIGHT
//                viewHolder.linearLayoutTime.gravity = Gravity.RIGHT
//            } else {
//                //상대방 말풍선 왼쪽
//                Glide.with(holder.itemView.context)
//                    .load(destUser!!.profileImgUrl)
//                    .apply(RequestOptions().circleCrop())
//                    .into(holder.imageViewProfile)
//                viewHolder.textViewName.text = destUser!!.name
//                viewHolder.linearLayoutDest.visibility = View.VISIBLE
//                viewHolder.textViewMsg.setBackgroundResource(R.drawable.leftbubble)
//                viewHolder.textViewMsg.text = comments[position]!!.message
//                viewHolder.linearLayoutRoot.gravity = Gravity.LEFT
//                viewHolder.linearLayoutTime.gravity = Gravity.LEFT
//            }
//            viewHolder.textViewTimeStamp.text = getDateTime(position)
//        }
//
//        fun getDateTime(position: Int): String {
//            val unixTime = comments[position]!!.timestamp as Long
//            val date = Date(unixTime)
//            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"))
//            return simpleDateFormat.format(date)
//        }
//
//        override fun getItemCount(): Int {
//            return comments.size
//        }
//
//        private inner class ViewHolder(@NonNull itemView: View) :
//            RecyclerView.ViewHolder(itemView) {
//            var textViewMsg //메시지 내용
//                    : TextView
//            var textViewName: TextView
//            var textViewTimeStamp: TextView
//            var imageViewProfile: ImageView
//            var linearLayoutDest: LinearLayout
//            var linearLayoutRoot: LinearLayout
//            var linearLayoutTime: LinearLayout
//
//            init {
//                textViewMsg = itemView.findViewById(R.id.item_messagebox_textview_msg)
//                textViewName = itemView.findViewById(R.id.item_messagebox_TextView_name)
//                textViewTimeStamp = itemView.findViewById(R.id.item_messagebox_textview_timestamp)
//                imageViewProfile = itemView.findViewById(R.id.item_messagebox_ImageView_profile)
//                linearLayoutDest = itemView.findViewById(R.id.item_messagebox_LinearLayout)
//                linearLayoutRoot = itemView.findViewById(R.id.item_messagebox_root)
//                linearLayoutTime = itemView.findViewById(R.id.item_messagebox_layout_timestamp)
//            }
//        }
//    }
//}