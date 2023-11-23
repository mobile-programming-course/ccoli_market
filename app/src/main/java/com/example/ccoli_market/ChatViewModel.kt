package com.example.ccoli_market

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ccoli_market.recyclerview.ChattingRoomItem

class ChatViewModel : ViewModel() {
    val chattingRoomItemsLiveData: MutableLiveData<List<ChattingRoomItem>> = MutableLiveData()
}