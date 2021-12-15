package com.example.mywhatsath.models

open class ModelArticle {
    var source: ModelSource? = null
    var author: String? = ""
    var title: String? = ""
    var description: String? = ""
    var url: String? = ""
    var urlToImage: String? = ""
    var publishedAt: String? = ""
    var content: String? = ""

    constructor(){}
    constructor(
        source: ModelSource?,
        author: String?,
        title: String?,
        description: String?,
        url: String?,
        urlToImage: String?,
        publishedAt: String?,
        content: String?
    ) {
        this.source = source
        this.author = author
        this.title = title
        this.description = description
        this.url = url
        this.urlToImage = urlToImage
        this.publishedAt = publishedAt
        this.content = content
    }

}