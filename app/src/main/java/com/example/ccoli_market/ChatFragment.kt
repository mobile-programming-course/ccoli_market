package com.example.ccoli_market

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ccoli_market.databinding.FragmentChatBinding
import com.example.ccoli_market.recyclerview.ChattingRoomItem
import com.example.ccoli_market.recyclerview.ChattingRoomItemRecyclerViewAdapter
import com.example.ccoli_market.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //recycler view layout
    private lateinit var recyclerViewChattingItem : RecyclerView

    //recycler view adapter
    private lateinit var recyclerViewChattingItemAdapter : ChattingRoomItemRecyclerViewAdapter

    //Firebase database reference
    private lateinit var database: DatabaseReference
    //Firebase Authentication
    private lateinit var auth: FirebaseAuth

    //Firebase Event Listener
    private lateinit var childEventListener: ChildEventListener

    //HomeFragment ViewModel
    private val chatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        //Firebase init
        database = Firebase.database.reference
        auth = Firebase.auth

        recyclerViewChattingItem = binding.recyclerviewChattingRoom

        addChildFirebaseListener() //리스너 부착

        // LiveData Observer 설정
        chatViewModel.chattingRoomItemsLiveData.observe(viewLifecycleOwner, Observer { items ->
            recyclerViewChattingItemAdapter.setChattingRoomItem(items)
        })

        setAdapter() //어댑터 붙이기
        //chatViewModel.addChatRoom(R.drawable.colli_icon2,"Chat Room 1", "Last Message", "User123")

        return binding.root
    }

    private fun addChildFirebaseListener() {
        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("firebase","call onChildAdded")
                val newUser = snapshot.getValue(User::class.java)
                newUser?.let { user ->
                    if (auth.currentUser?.uid != user.uId) {
                        val randInt = Random.nextInt(0, 4)

                        // 리스트에 동일한 사용자가 있는지 확인
                        val isAlreadyAdded = chatViewModel.chattingRoomItemsLiveData.value?.any { it.userId == user.uId } == true
                        if (!isAlreadyAdded) {
                            val newChatRoomItem = ChattingRoomItem(randInt, user.name, user.email, user.uId) // userId 추가
                            val updatedList = chatViewModel.chattingRoomItemsLiveData.value.orEmpty().toMutableList()
                            updatedList.add(newChatRoomItem)
                            chatViewModel.chattingRoomItemsLiveData.value = updatedList
                        }
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // 데이터 변경시 처리 로직
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // 데이터 삭제시 처리 로직
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }

        database.child("user").addChildEventListener(childEventListener)
    }



    //리사이클러뷰에 리사이클러뷰 어댑터 부착
    private fun setAdapter(){
        recyclerViewChattingItem.layoutManager = LinearLayoutManager(this.context)
        recyclerViewChattingItemAdapter = activity?.let { ChattingRoomItemRecyclerViewAdapter(it) }!!
        recyclerViewChattingItem.adapter = recyclerViewChattingItemAdapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        //리스너 삭제
        database.child("user").removeEventListener(childEventListener)
    }
}
