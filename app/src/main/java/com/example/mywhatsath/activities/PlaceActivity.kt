package com.example.mywhatsath.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.text.set
import androidx.core.text.toSpannable
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ActivityPlaceBinding
import com.example.mywhatsath.models.PlaceDatabase
import com.example.mywhatsath.utils.LinearGradientSpan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PlaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceBinding
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRf: FirebaseDatabase
    private lateinit var roomDb: PlaceDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomDb = PlaceDatabase.getInstance(applicationContext)!!
        getPlaceList()

        //init main toolbar
        setSupportActionBar(binding.mainToolbar)
        binding.mainToolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        // set gradient linear for title tv
        getGradientTextView()

        //
        binding.mapBtn.setOnClickListener {
            startActivity(Intent(this@PlaceActivity, MapsScreenActivity::class.java))
        }
    }

    private fun getPlaceList() {
        var placeList = "Place List \n"
        CoroutineScope(Dispatchers.Main).launch{
            val places = CoroutineScope(Dispatchers.IO).async {
                roomDb.placeDao().getAll()
            }.await()

            for(place in places){
                placeList += "${place.name} , ${place.id}, ${place.latitude}, ${place.longitude}, ${place.address} \n"
            }

            binding.infoTv.text = placeList
        }
    }

    private fun getGradientTextView() {
        val sloganText = binding.sloganTv.text.toString()
        val startColor = ContextCompat.getColor(this, R.color.start)
        val endColor = ContextCompat.getColor(this, R.color.end)
        val spannable = sloganText.toSpannable()
        spannable[0..sloganText.length] = LinearGradientSpan(sloganText, sloganText, startColor, endColor)
        binding.sloganTv.text = spannable
    }

    // inflate menu to toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // set menu functions
    override fun onOptionsItemSelected(item: MenuItem) = when(item?.itemId) {
        R.id.homeBtn -> {
            startActivity(Intent(this@PlaceActivity, DashboardUserActivity::class.java))
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