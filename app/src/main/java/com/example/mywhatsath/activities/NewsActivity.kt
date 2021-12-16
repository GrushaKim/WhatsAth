package com.example.mywhatsath.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mywhatsath.adapters.NewsAdapter
import com.example.mywhatsath.databinding.ActivityNewsBinding
import com.example.mywhatsath.utils.retrofit.Data
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

    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsList: ArrayList<Data>
    private lateinit var newsAdapter: NewsAdapter

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

        // load latest headlines
        getHeadlines(supplService)

        // init recyclerview
        newsRecyclerView = binding.newsRecyclerView
        newsList = ArrayList()
        newsAdapter = NewsAdapter(this, newsList)

        newsRecyclerView.layoutManager = LinearLayoutManager(this)
        newsRecyclerView.adapter = newsAdapter

    }

    private fun getHeadlines(service: RetrofitService) {
        service.getHeadlines().enqueue(object: Callback<NewsResponse>{
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if(response.isSuccessful){
                    Log.d(TAG, "onResponse: successfully loaded headlines from mediastack API. ${response.body().toString()}")
                    newsList.clear()

                    val titleData = response.body()!!.data

                    for(item in titleData!!){
                        newsList.add(item)
                    }
                    newsAdapter.notifyDataSetChanged()
                }

            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: failed to load. Error: ${t.message}")
            }

        })
        }
    }

