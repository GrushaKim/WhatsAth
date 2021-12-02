package com.example.mywhatsath.models

class ModelUser{
    var name: String? = null
    var email: String? = null
    var uid: String? = null
    var profileImage: String? = null
    var gender: String? = null
    var level: String? = null
    var sport: String? = null
    var aboutMe: String? = null
    var sex: String? = null
    var hearts: Int? = 0

    constructor(){}
    constructor(
        name: String?,
        email: String?,
        uid: String?,
        profileImage: String?,
        gender: String?,
        level: String?,
        sport: String?,
        aboutMe: String?,
        sex: String?,
        hearts: Int?
    ) {
        this.name = name
        this.email = email
        this.uid = uid
        this.profileImage = profileImage
        this.gender = gender
        this.level = level
        this.sport = sport
        this.aboutMe = aboutMe
        this.sex = sex
        this.hearts = hearts
    }


}