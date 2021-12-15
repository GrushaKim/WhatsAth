package com.example.mywhatsath.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mywhatsath.databinding.ActivityNewsBinding

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding

    companion object{
        const val TAG = "NEWS_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}