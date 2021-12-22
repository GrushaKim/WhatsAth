package com.example.mywhatsath.utils.retrofit

object Constants {
    private val GOOGLE_API_URL = "https://maps.googleapis.com/"
    val googleApiService: GoogleAPIService
        get()=GoogleAPIRetrofitClient.getClient(GOOGLE_API_URL).create(GoogleAPIService::class.java)
}