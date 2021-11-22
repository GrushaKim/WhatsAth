package com.example.mywhatsath.models

class ModelMessage {
    var message: String? = null
    var senderId: String? = null
    var timestamp: Long? = 0
    var msgImage: String? = null

    constructor(){}

    constructor(message: String?, senderId: String?, timestamp: Long?, msgImage: String?) {
        this.message = message
        this.senderId = senderId
        this.timestamp = timestamp
        this.msgImage = msgImage
    }


}