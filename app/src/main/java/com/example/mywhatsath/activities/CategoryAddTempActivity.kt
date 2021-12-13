package com.example.mywhatsath.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.mywhatsath.databinding.ActivityCategoryAddTempBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddTempActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddTempBinding
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    private var sport = ""

    companion object{
        const val TAG = "CATEGORY_ADD_TEMP_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddTempBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init auth
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()

        //submit button click
        binding.submitBtn.setOnClickListener {
            addSportCategory()
        }
    }

    private fun addSportCategory() {
        sport = binding.nameEt.text.toString().trim()
        val hashMap = HashMap<String, Any>()
        val timestamp = System.currentTimeMillis()

        hashMap["sport"] = sport
        hashMap["id"] = "$timestamp"

        val ref = fbDbRef.getReference("Sports")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addSportCategory: successfully added sport category")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "addSportCategory: failed to add sport category. Error: ${e.message}")
            }
    }

}