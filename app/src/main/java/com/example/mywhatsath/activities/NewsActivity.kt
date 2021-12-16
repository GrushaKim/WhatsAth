package com.example.mywhatsath.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mywhatsath.databinding.ActivityNewsBinding
import com.example.mywhatsath.utils.retrofit.NewsResponse
import com.example.mywhatsath.utils.retrofit.RetrofitClient
import com.example.mywhatsath.utils.retrofit.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding
    private lateinit var retrofit: Retrofit
    private lateinit var supplService: RetrofitService

    /*private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsList: ArrayList<ModelData>
*/
    companion object{
        const val TAG = "NEWS_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init retrofit & load service interface
        retrofit = RetrofitClient.getInstance()
        supplService = retrofit.create(RetrofitService::class.java)


        binding.clickBtn.setOnClickListener { 
            getHeadlines(supplService, "a0b2b41a173e1a2641db7afe59bc972f", "sports")
        }
        /*newsList = ArrayList()
        newsAdapter = NewsAdapter(this, newsList)

        binding.newsRecyclerView.apply{
            layoutManager = LinearLayoutManager(this@NewsActivity)
            adapter = newsAdapter
        }*/

    }

    private fun getHeadlines(service: RetrofitService, access_key: String, categories: String) {
        service.getHeadlines(access_key, categories).enqueue(object: Callback<NewsResponse>{
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                Log.d(TAG, "onResponse: successfully loaded. ${response.body()?.data.toString()}")
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: failed to load. Error: ${t.message}")
            }

        })
        }
    }

