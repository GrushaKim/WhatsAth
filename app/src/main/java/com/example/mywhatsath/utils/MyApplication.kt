package com.example.mywhatsath.utils

import android.app.Application
import android.text.format.DateFormat
import java.util.*

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }
    companion object{
        fun formatTimeStamp(timestamp: Long):String{
            val cal = Calendar.getInstance(Locale.KOREA)
            cal.timeInMillis = timestamp
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }
    }
}