package com.example.mywhatsath.utils.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface GoogleAPIService {
    @GET
    fun getNearbyGyms(@Url url:String): Call<MyPlace>
}