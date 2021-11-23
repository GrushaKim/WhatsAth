package com.example.mywhatsath

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mywhatsath.databinding.ActivityProfileEditBinding
import com.example.mywhatsath.utils.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    //image uri
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth&db
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()

        //load the profile
        loadUserProfile()

    }

    private fun loadUserProfile() {
        val ref = fbDbRef.getReference("Users")
        ref.child(fbAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user profile
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val name = "${snapshot.child("name").value}"
                    val email = "${snapshot.child("email").value}"
                    val sex = "${snapshot.child("sex").value}"
                    val regDate = "${snapshot.child("regDate").value}"
                    val sport = "${snapshot.child("sport").value}"
                    val level = "${snapshot.child("level").value}"
                    // val aboutMe = "${snapshot.child("aboutMe").value}"

                    //convert regdate
                    val formattedRegDate = MyApplication.formatRegDate(regDate.toLong())

                    //set data
                    binding.nameEt.hint = name
                    binding.emailTv.text = email
                    if(sex == "male"){
                        binding.sexIv.setImageResource(R.drawable.ic_man)
                    }else{
                        binding.sexIv.setImageResource(R.drawable.ic_woman)
                    }
                    binding.regDateTv.text = formattedRegDate
                    binding.sportTv.text = sport
                    binding.levelTv.text = level
                    //binding.aboutMeTv.text = aboutMe

                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}