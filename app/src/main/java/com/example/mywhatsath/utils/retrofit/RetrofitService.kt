package com.example.mywhatsath.utils.retrofit

import com.example.mywhatsath.R
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {

    @GET("v1/news?access_key=a0b2b41a173e1a2641db7afe59bc972f&categories=health,sports&languages=en&sort=published_desc")
    fun getHeadlines(): Call<NewsResponse>

}