package com.example.mywhatsath.utils.retrofit

import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitImpl {

    @GET("top-headlines")
    fun callHeadlines(
        @Query("country") country: String,
        @Query("category") category: String,
        @Query("q") query: String,
        @Query("apiKey") api_key: String
    ) : Call<JsonElement>

}