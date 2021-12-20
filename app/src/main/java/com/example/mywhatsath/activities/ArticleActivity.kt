package com.example.mywhatsath.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ActivityArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.util.*

class ArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleBinding
    private lateinit var fbAuth: FirebaseAuth

    companion object{
        const val TAG = "ARTICLE_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init main toolbar
        setSupportActionBar(binding.mainToolbar)
        binding.mainToolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        //init auth
        fbAuth = FirebaseAuth.getInstance()

        //extract info from HeadlineActivity
        val title = intent.getStringExtra("title")
        val image = intent.getStringExtra("image")
        val author = intent.getStringExtra("author")
        val source = intent.getStringExtra("source")
        val description = intent.getStringExtra("description")

        //load article detail
        loadArticle(title, image, author, source, description)
    }

    private fun loadArticle(
        title: String?,
        image: String?,
        author: String?,
        source: String?,
        description: String?
    ) {

        var desc: String = Html
            .fromHtml(description, Html.FROM_HTML_MODE_COMPACT)
            .toString()

        binding.articleTitleTv.text = title

        //check image
        if(image.isNullOrEmpty()){
            binding.articleIv.visibility = View.GONE
        }else{
            binding.articleIv.visibility = View.VISIBLE
            Picasso.get()
                .load(image)
                .into(binding.articleIv)
        }

        binding.articleSourceTv.text = source
        binding.articleAuthorTv.text = author
        binding.articleDescTv.text = desc
    }


    // inflate menu to toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // set menu functions
    override fun onOptionsItemSelected(item: MenuItem) = when(item?.itemId) {
        R.id.homeBtn -> {
            startActivity(Intent(this@ArticleActivity, DashboardUserActivity::class.java))
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

    private fun checkUser() {
        // get current user
        val fbUser = fbAuth.currentUser
        if (fbUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}