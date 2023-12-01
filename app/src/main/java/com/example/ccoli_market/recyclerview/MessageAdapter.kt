package com.example.ccoli_market.recyclerview


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ccoli_market.Message
import com.example.ccoli_market.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


// 1. inner Class ViewHolder클래스 작성 - RecyclerView.ViewHolder 상속
// 2. outer Class Adpater 작성 - Adapter<ViewHolder>()클래스 상속
// => 해당 클래스의 경우 사용할 뷰 홀더 클래스가 2개이므로, 일반화관계인 ViewHolder를 Generic으로 넘김
// 3. Implementation method

class MessageAdapter(private val messageList: ArrayList<Message>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 사용할 뷰 홀더가 2개이므로, 타입을 정하기 위해
    // 메세지에 따라 서로 다른 뷰 홀더를 사용해야함
    val SEND: Int = 1
    val RECEIVE: Int = 2


    // 보낸 쪽
    class SendViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        // 보낸 메시지 텍스트뷰 객체 구현
        val sendMessage: TextView = itemView.findViewById(R.id.send_message)
    }

    // 받은 쪽
    class ReceiveViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        // 받은 메시지 텍스트뷰 객체 구현
        val receiveMessage: TextView = itemView.findViewById(R.id.receive_message)
    }


    // Set Type of Recyclerview Item's View
    // 리턴된 타입을 바탕으로 사용할 뷰홀더 결정
    override fun getItemViewType(position: Int): Int {
        //현재 메시지
        var currentMessage = messageList[position]

        //현재 메시지의 id를 확인하여 나의 uid와 같다면 SEND 리턴
        return if(Firebase.auth.currentUser?.uid == currentMessage.sendId)
            SEND
        else
            RECEIVE

    }


    // Implementation of RecyclerView Adapter

    // 뷰 생성 => 뷰 홀더의 layout으로 화면에 붙일 뷰를 생성하여 리턴
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //getItemViewType 에서 리턴된 값은 viewType에 존재
        return if(viewType == SEND){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_send_message, parent, false)
            SendViewHolder(view) // SendViewHolder로 View 생성
        } else{
            //
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receive_message, parent, false)
            ReceiveViewHolder(view) //ReciveViewHolder로 View 생성
        }
    }

    // return count of recyclerview list items
    // 여기서 리턴된만큼 전체 과정이 반복됨
    override fun getItemCount(): Int {
        return messageList.size
    }

    // 데이터 연결
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]

        //보내는 메시지라면
        if(holder.javaClass == SendViewHolder::class.java){
            //DownCasting
            val viewHolder = holder as SendViewHolder
            viewHolder.sendMessage.text = currentMessage.message
        } else { //받는 메시지라면
            //DownCasting
            val viewHolder = holder as ReceiveViewHolder
            viewHolder.receiveMessage.text = currentMessage.message
        }
    }

}