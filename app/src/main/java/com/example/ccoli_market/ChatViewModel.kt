package com.example.ccoli_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ccoli_market.recyclerview.ChattingRoomItem

class ChatViewModel : ViewModel() {
    val chattingRoomItemsLiveData: MutableLiveData<List<ChattingRoomItem>> = MutableLiveData(listOf())

    fun addChatRoom(image: Int, name: String, lastMessage: String, userId: String) {
        val newChatRoom = ChattingRoomItem(image, name, lastMessage, userId)

        val currentChatRooms = chattingRoomItemsLiveData.value.orEmpty().toMutableList()
        currentChatRooms.add(newChatRoom)

        chattingRoomItemsLiveData.value = currentChatRooms
    }
}
