package com.example.mywhatsath.models

class ModelMessage {
    var message: String? = null
    var sender: String? = null

    constructor(){}

    constructor(message: String?, sender: String?) {
        this.message = message
        this.sender = sender
    }


}