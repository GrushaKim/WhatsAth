package com.example.mywhatsath.utils.retrofit

import com.example.mywhatsath.R
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("v1/news")
    fun getHeadlines(
        @Query("access_key") access_key: String,
        @Query("categories") categories: String
    ): Call<NewsResponse>

}