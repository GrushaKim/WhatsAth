package com.example.mywhatsath.models

open class ModelSource {
    var id: String = ""
    var name: String = ""

    constructor(){}

    constructor(id: String, name: String) {
        this.id = id
        this.name = name
    }


}