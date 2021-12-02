package com.example.mywhatsath.models

class ModelMessage {
    var message: String? = null
    var receiverId: String? = null
    var senderId: String? = null
    var timestamp: Long? = 0
    var image: String? = null

    constructor(){}
    constructor(
        message: String?,
        receiverId: String?,
        senderId: String?,
        timestamp: Long?,
        image: String?
    ) {
        this.message = message
        this.receiverId = receiverId
        this.senderId = senderId
        this.timestamp = timestamp
        this.image = image
    }


}