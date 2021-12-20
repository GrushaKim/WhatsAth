package com.example.mywhatsath.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mywhatsath.R
import com.example.mywhatsath.adapters.NewsAdapter
import com.example.mywhatsath.databinding.ActivityHeadlineBinding
import com.example.mywhatsath.utils.retrofit.*
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class HeadlineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHeadlineBinding
    private lateinit var fbAuth: FirebaseAuth

    // retrofit var
    private lateinit var retrofit: Retrofit
    private lateinit var supplService: RetrofitService

    // recyclerView var
    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsList: ArrayList<Data>
    private lateinit var newsAdapter: NewsAdapter

    /*private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsList: ArrayList<ModelData>
*/
    companion object{
        const val TAG = "HEADLINE_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeadlineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init main toolbar
        setSupportActionBar(binding.mainToolbar)
        binding.mainToolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        // init auth&db
        fbAuth = FirebaseAuth.getInstance()

        // init retrofit & load service interface
        retrofit = RetrofitClient.getInstance()
        supplService = retrofit.create(RetrofitService::class.java)

        // load latest headlines
        getHeadlines(supplService, AuthKey.NEWS_API_KEY)

        // init recyclerview
        newsRecyclerView = binding.newsRecyclerView
        newsList = ArrayList()
        newsAdapter = NewsAdapter(this, newsList)

        newsRecyclerView.layoutManager = LinearLayoutManager(this)
        newsRecyclerView.adapter = newsAdapter

    }

    // inflate menu to toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // set menu functions
    override fun onOptionsItemSelected(item: MenuItem) = when(item?.itemId) {
        R.id.homeBtn -> {
            startActivity(Intent(this@HeadlineActivity, DashboardUserActivity::class.java))
            true
        }
        R.id.logoutBtn -> {
            fbAuth.signOut()
            checkUser()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun getHeadlines(service: RetrofitService, newsApiKey: String) {
        service.getHeadlines(newsApiKey).enqueue(object: Callback<NewsResponse>{
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

    private fun checkUser() {
        // get current user
        val fbUser = fbAuth.currentUser
        if (fbUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

