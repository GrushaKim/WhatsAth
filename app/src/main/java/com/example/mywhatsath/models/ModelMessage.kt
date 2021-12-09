package com.example.mywhatsath.models

class ModelMessage {
    var id: String? = null
    var message: String? = null
    var receiverId: String? = null
    var senderId: String? = null
    var timestamp: Long? = 0
    var imageUrl: String? = null

    constructor(){}
    constructor(
        id: String?,
        message: String?,
        receiverId: String?,
        senderId: String?,
        timestamp: Long?,
        imageUrl: String?
    ) {
        this.id = id
        this.message = message
        this.receiverId = receiverId
        this.senderId = senderId
        this.timestamp = timestamp
        this.imageUrl = imageUrl
    }


}