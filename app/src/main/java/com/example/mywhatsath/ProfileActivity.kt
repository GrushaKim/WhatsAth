package com.example.mywhatsath

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.mywhatsath.databinding.ActivityProfileBinding
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

        //init auth&db
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()

        //load the profile
        loadUserProfile()

        //back button click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //edit button click
        binding.editProfileBtn.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ProfileEditActivity::class.java))
        }
    }

    private fun loadUserProfile() {
        val ref = fbDbRef.getReference("Users")
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

                    if(sex == R.string.male.toString()){
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
    }
}