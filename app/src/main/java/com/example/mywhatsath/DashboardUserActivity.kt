package com.example.mywhatsath

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mywhatsath.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding

    private lateinit var fbAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth
        fbAuth = FirebaseAuth.getInstance()
        checkUser()


        // logout button click
        binding.logoutTv.setOnClickListener {
            fbAuth.signOut()
            checkUser()
        }

    }

    private fun checkUser() {
        // get current user
        val fbUser = fbAuth.currentUser
        if(fbUser == null){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }else{
            val email = fbUser.email
            binding.titleTv.text = email
        }
    }
}