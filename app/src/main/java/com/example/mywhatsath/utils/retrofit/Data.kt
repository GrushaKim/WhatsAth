package com.example.mywhatsath.utils.retrofit


import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.util.*

data class Data(
    @SerializedName("author")
    val author: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("language")
    val language: String?,
    @SerializedName("published_at")
    val publishedAt: String?,
    @SerializedName("source")
    val source: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("url")
    val url: String?
)