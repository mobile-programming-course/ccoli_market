package com.example.ccoli_market

class Model (imageUrl: String?){
    private var imageUrl: String? = imageUrl
    fun Model(imageUrl: String?) {
        this.imageUrl = imageUrl
    }
    fun getImageUrl(): String? {
        return imageUrl
    }
    fun setImageUrl(imageUrl: String?) {
        this.imageUrl = imageUrl
    }
}