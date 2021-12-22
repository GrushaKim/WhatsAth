package com.example.mywhatsath.utils.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GoogleAPIRetrofitClient {
    private var retrofit: Retrofit ?= null

    fun getClient(baseUrl1: String): Retrofit{
        if(retrofit == null){
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl1)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}