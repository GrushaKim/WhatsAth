package com.example.mywhatsath.utils.retrofit


import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("data")
    val `data`: List<Data>?
)