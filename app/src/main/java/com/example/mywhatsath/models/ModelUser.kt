package com.example.mywhatsath.models

class ModelUser {
    var uid: String? = null
    var name: String? = null
    var email: String? = null
    var profileImage: String? = null
    var gender: String? = null
    var level: String? = null
    var sport: String? = null
    var aboutMe: String? = null
    var heartsCnt: Long? = null



    constructor(){}
    constructor(
        uid: String?,
        name: String?,
        email: String?,
        profileImage: String?,
        gender: String?,
        level: String?,
        sport: String?,
        aboutMe: String?,
        heartsCnt: Long?
    ) {
        this.uid = uid
        this.name = name
        this.email = email
        this.profileImage = profileImage
        this.gender = gender
        this.level = level
        this.sport = sport
        this.aboutMe = aboutMe
        this.heartsCnt = heartsCnt
    }
}