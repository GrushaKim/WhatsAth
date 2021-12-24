package com.example.mywhatsath.utils.retrofit

import com.example.mywhatsath.R
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface RetrofitService {

    @GET(Constants.NEWS_HEADLINE_URL)
    fun getHeadlines(
        @Query("access_key") access_key: String,
    ): Call<NewsResponse>

    @GET
    fun getNearbyGyms(@Url url:String): Call<MyPlace>

}