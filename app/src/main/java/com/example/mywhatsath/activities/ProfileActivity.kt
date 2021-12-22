package com.example.mywhatsath.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.set
import androidx.core.text.toSpannable
import com.bumptech.glide.Glide
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ActivityProfileBinding
import com.example.mywhatsath.utils.LinearGradientSpan
import com.example.mywhatsath.utils.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init main toolbar
        setSupportActionBar(binding.mainToolbar)
        binding.mainToolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        // set linear gradient for tv
        getGradientTextView()

        //init auth&db
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()

        //userId to show profile of the other user
        val receiverId = intent.getStringExtra("userId")

        //load the profile
        loadUserProfile(receiverId)

        // show edit button if the user of profile is the current user
        checkCurrentUser(receiverId)

        //edit button click
        binding.editProfileBtn.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ProfileEditActivity::class.java))
        }
    }

    private fun getGradientTextView() {
        val sloganText = binding.profileTv.text.toString()
        val startColor = ContextCompat.getColor(this, R.color.start)
        val endColor = ContextCompat.getColor(this, R.color.end)
        val spannable = sloganText.toSpannable()
        spannable[0..sloganText.length] = LinearGradientSpan(sloganText, sloganText, startColor, endColor)
        binding.profileTv.text = spannable
    }

    // inflate menu to toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // set menu functions
    override fun onOptionsItemSelected(item: MenuItem) = when(item?.itemId) {
        R.id.homeBtn -> {
            startActivity(Intent(this@ProfileActivity, DashboardUserActivity::class.java))
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

    private fun checkCurrentUser(receiverId: String?) {
        if(receiverId.isNullOrEmpty()){
            binding.editProfileBtn.visibility = View.VISIBLE
        }else{
            binding.editProfileBtn.visibility = View.INVISIBLE
        }

    }

    private fun loadUserProfile(receiverId: String?) {
        val ref = fbDbRef.getReference("Users")

        if(receiverId.isNullOrEmpty()){
            ref.child(fbAuth.uid!!)
                .addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get user profile
                        val profileImage = "${snapshot.child("profileImage").value}"
                        val name = "${snapshot.child("name").value}"
                        val email = "${snapshot.child("email").value}"
                        val sex = "${snapshot.child("sex").value}"
                        val regDate = "${snapshot.child("regDate").value}"
                        val sport = "${snapshot.child("sport").value}"
                        val level = "${snapshot.child("level").value}"
                        val aboutMe = "${snapshot.child("aboutMe").value}"

                        //convert regdate
                        val formattedRegDate = MyApplication.formatRegDate(regDate.toLong())

                        //set data
                        if(profileImage.isEmpty() || profileImage == ""){
                            binding.profileIv.setImageResource(R.drawable.ic_baseline_person_24)
                        }else{
                            try{
                                Glide.with(this@ProfileActivity)
                                    .load(profileImage)
                                    .into(binding.profileIv)
                            } catch(e: Exception){
                                Log.d("PROFILE_TAG", "Failed to load the profile image. Error: ${e.message}")
                            }
                        }

                        binding.nameTv.text = name
                        binding.emailTv.text = email

                        if(sex.lowercase() == R.string.male.toString().lowercase()){
                            binding.sexIv.setImageResource(R.drawable.ic_man)
                        }else{
                            binding.sexIv.setImageResource(R.drawable.ic_woman)
                        }

                        binding.regDateTv.text = formattedRegDate
                        binding.sportTv.text = sport
                        binding.levelTv.text = level

                        if(aboutMe.isEmpty() || aboutMe == ""){
                            binding.aboutMeTv.text = "Update your information"
                        }else{
                            binding.aboutMeTv.text = aboutMe
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }else{
            ref.child(receiverId!!)
                .addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get user profile
                        val profileImage = "${snapshot.child("profileImage").value}"
                        val name = "${snapshot.child("name").value}"
                        val email = "${snapshot.child("email").value}"
                        val sex = "${snapshot.child("sex").value}"
                        val regDate = "${snapshot.child("regDate").value}"
                        val sport = "${snapshot.child("sport").value}"
                        val level = "${snapshot.child("level").value}"
                        val aboutMe = "${snapshot.child("aboutMe").value}"

                        //convert regdate
                        val formattedRegDate = MyApplication.formatRegDate(regDate.toLong())

                        //set data
                        if(profileImage.isEmpty() || profileImage == ""){
                            binding.profileIv.setImageResource(R.drawable.ic_baseline_person_24)
                        }else{
                            try{
                                Glide.with(this@ProfileActivity)
                                    .load(profileImage)
                                    .into(binding.profileIv)
                            } catch(e: Exception){
                                Log.d("PROFILE_TAG", "Failed to load the profile image. Error: ${e.message}")
                            }
                        }

                        binding.nameTv.text = name
                        binding.emailTv.text = email

                        if(sex == R.string.male.toString()){
                            binding.sexIv.setImageResource(R.drawable.ic_man)
                        }else{
                            binding.sexIv.setImageResource(R.drawable.ic_woman)
                        }

                        binding.regDateTv.text = formattedRegDate
                        binding.sportTv.text = sport
                        binding.levelTv.text = level

                        if(aboutMe.isEmpty() || aboutMe == ""){
                            binding.aboutMeTv.text = ""
                        }else{
                            binding.aboutMeTv.text = aboutMe
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
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