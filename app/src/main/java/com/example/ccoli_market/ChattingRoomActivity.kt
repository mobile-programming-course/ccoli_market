package com.example.ccoli_market

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ccoli_market.recyclerview.MessageAdapter
import com.example.ccoli_market.databinding.ActivityChattingRoomBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database


//채팅방
class ChattingRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChattingRoomBinding

    //채팅방 관련 정보들
    private lateinit var receiverName : String
    private lateinit var receivedUid : String

    //Firebase
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference

    //chatting room
    private lateinit var receiverRoom: String //받는쪽 대화방
    private lateinit var senderRoom: String //보내는쪽 대화방

    //대화 목록
    private lateinit var messageList: ArrayList<Message>

    //채팅방 어댑터
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageRecyclerViewAdapter : MessageAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firebase init
        auth = Firebase.auth
        database = Firebase.database.reference

        // 정보 담기
        //receiverName = intent.getStringExtra("name").toString() //상대방 이름
        receiverName = intent.getStringExtra("name").toString() //상대방 이름
        receivedUid = intent.getStringExtra("uId").toString() //상대방 uId

        // 접속자 UID
        val senderUid = auth.currentUser?.uid

        // 보내는쪽 대화방 ID 설정
        senderRoom = receivedUid + senderUid
        // 받는쪽 대화방 ID 설정
        receiverRoom = senderUid + receivedUid
        //=> 위 값을 활용하여 채팅을 보내면 2개의 방에 각각 데이터 삽입


        binding.nickname.text = receiverName //상대방 이름 상단에 띄우기

        //뒤로가기 버튼 구현
        binding.backButton.setOnClickListener {
            finish() //엑티비티 종료
        }

        //메시지 전송 버튼
        binding.btnSendMessage.setOnClickListener {
            val message = binding.etMessage.text.toString()
            val messageObject = Message(message,senderUid) //메시지 객체 생성

            //데이터 저장 : 보내는쪽 대화방
            //chats -> senderRoom -> messages
            database.child("chats").child(senderRoom).child("messages").push()
                .setValue(messageObject).addOnSuccessListener { // 보낸 메시지 DB에 삽입
                    //삽입 성공시
                    //데이터 저장 : 받는 쪽 대화방
                    database.child("chats").child(receiverRoom).child("remessages").push()
                        .setValue(messageObject)
                }
            binding.etMessage.setText("") //초기화
        }

        //대화목록 초기화
        messageList = ArrayList()

        //채팅목록 리사이클러뷰 초기화
        messageRecyclerView = binding.recyclerView
        messageRecyclerViewAdapter = MessageAdapter(messageList) // 어댑터 생성
        messageRecyclerView.layoutManager = LinearLayoutManager(this) //레이아웃 설정
        messageRecyclerView.adapter = messageRecyclerViewAdapter // 어댑터 부착


        // 대화 목록 가져오기
        database.child("chats").child(senderRoom).child("messages")
            .addValueEventListener(object :ValueEventListener{
                // 메시지 변경시 호출됨
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear() //empty

                    for(postSnapshat in snapshot.children){
                        val message = postSnapshat.getValue(Message::class.java)
                        messageList.add(message!!) //대화 삽입
                    }
                    messageRecyclerViewAdapter.notifyDataSetChanged() // 데이터 변경여부 알리기
                }

                // 오류 발생시
                override fun onCancelled(error: DatabaseError) {
                    Log.d("firebae","error : chattingRoom error")
                }
            })


    }
}