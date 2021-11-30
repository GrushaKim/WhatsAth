package com.example.mywhatsath

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.mywhatsath.databinding.ActivitySearchBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    // firebase auth, db
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.chip1.setOnClickListener{
            Toast.makeText(this, binding.chip1.text, Toast.LENGTH_SHORT).show()
        }

        // create chips
        entryChip()

    }

    private fun entryChip() {
        binding.searchEt.setOnKeyListener { view, keyCode, keyEvent ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP){

                binding.apply{
                    val keyword = searchEt.text.toString().trim()
                    createChips(keyword)
                    searchEt.text.clear()
                }

                return@setOnKeyListener true
            }

            false
        }
    }

    private fun createChips(keyword: String) {
        val chip = Chip(this)
        chip.apply{
            text = keyword
            chipIcon = ContextCompat.getDrawable(
                this@SearchActivity,
                R.drawable.ic_launcher_background
            )
            isChipIconVisible = false
            isCloseIconVisible = true
            isClickable = true
            isCheckable = true
            // set add+remove a single chip
            binding.apply{
                chipEntryGroup.addView(chip as View)
                chip.setOnCloseIconClickListener {
                    chipEntryGroup.removeView(chip as View)
                }
            }
        }
    }
}