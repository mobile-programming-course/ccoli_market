package com.example.ccoli_market.recyclerview

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.ccoli_market.R
import com.example.ccoli_market.MessageActivity

// 4.아이템을 유지/관리하는 Adapter
class ChattingRoomItemRecyclerViewAdapter(var context: Context) : //화면에 데이터를 붙이기 위해 context가 필요함
    RecyclerView.Adapter<ChattingRoomItemRecyclerViewAdapter.ViewHolder>() { //리사이클러뷰 어댑터를 상속, Generic 값으로 innerClass인 ViewHolder를 넣어줘야함
    private var chattingRoomItems: List<ChattingRoomItem> = emptyList()

    fun setChattingRoomItem(items: List<ChattingRoomItem>) {
        this.chattingRoomItems = items
        notifyDataSetChanged()
    }

//    val predefinedColors = listOf(
//        Color.parseColor("#FFC107"), // Amber
//        Color.parseColor("#FF5722"), // Deep Orange
//        Color.parseColor("#4CAF50"), // Green
//        Color.parseColor("#03A9F4")  // Light Blue
//    )


    //(2) ViewHolder패턴 => View를 Holder에 넣어두었다가 재사용을 하기 위함
    //=> itemView는 onCreateViewHolder에서 전달받은 아이템 뷰의 레이아웃에 해당
    //=> onBindViewHolder에서 view에 groups의 값을 할당함
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var chatItemImage : ImageView // 채팅 상대 아이콘 이미지
        var chatUserName : TextView // 상대방 이름
        var lastChat : TextView // 마지막 채팅 내용
        var cardView : CardView // 카드 뷰

        init { //innerClass의 생성자에 해당 => 뷰의 레이아웃 가져오기 => 화면에 붙이기 위한 하나의 뷰를 만드는 과정에 해당
            chatItemImage = itemView.findViewById(R.id.iv_chat_user)
            chatUserName = itemView.findViewById(R.id.tv_id)
            lastChat = itemView.findViewById(R.id.tv_last_chat)
            cardView = itemView.findViewById(R.id.cardview_chat_user)

        }
    }

    //아이템 뷰의 레이아웃을 가져와서 화면에 붙임 (1)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        //화면에 뷰를 붙이기 위해 inflater가 필요
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //아이템 뷰 레이아웃 가져오기
        val view = inflater.inflate(R.layout.item_chatting_room, parent, false)

        return ViewHolder(view)
    }


    //(3)
    //itemView에 Array<ChattingRoomItem>의 값을 할당함
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser: ChattingRoomItem = chattingRoomItems[position]
//        holder.chatItemImage
        holder.chatUserName.text = currentUser.chatUserName
        holder.lastChat.text = currentUser.lastChat

//        holder.cardView.setCardBackgroundColor(predefinedColors[currentUser.chatItemImage])

        // 아이템 클릭시 이벤트
        // 채팅방으로 이동하는 부분
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("chatRoom", chattingRoomItems[position]) // 채팅방 정보 전달
            context.startActivity(intent)
        }
    }

    //리사이클러뷰의 아이템의 개수가 총 몇개인지를 리턴
    override fun getItemCount(): Int {
        return chattingRoomItems.size
    }
}