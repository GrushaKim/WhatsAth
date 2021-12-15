package com.example.mywhatsath.models

open class ModelNewsApiRes {
    var status: String? = null
    var totalResults: Int? = 0
    var articles: ArrayList<ModelArticle>? = null

    constructor(){}
    constructor(status: String?, totalResults: Int?, articles: ArrayList<ModelArticle>?) {
        this.status = status
        this.totalResults = totalResults
        this.articles = articles
    }

}