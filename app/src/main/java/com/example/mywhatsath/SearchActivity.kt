package com.example.mywhatsath

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mywhatsath.adapters.MessageAdapter
import com.example.mywhatsath.adapters.SearchAdapter
import com.example.mywhatsath.databinding.ActivitySearchBinding
import com.example.mywhatsath.models.ModelMessage
import com.example.mywhatsath.models.ModelUser
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.protobuf.Value

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    // firebase auth, db
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    companion object{
        const val TAG = "SEARCH_TAG"
    }

    // adapter
    private lateinit var searchList: ArrayList<ModelUser>
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // init
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()


        // set recyclerView
        searchRecyclerView = binding.searchRecyclerView
        searchList = ArrayList()
        searchAdapter = SearchAdapter(this, searchList)

        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchAdapter



        loadAllUsers()

        // search
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try{
                    searchAdapter.filter!!.filter(s)
                }catch(e: Exception){
                    Log.d(TAG, "onTextChanged: failed to search users. Error: ${e.message}")
                }
            }
            override fun afterTextChanged(ed: Editable?) {
            }
        })


       /* // create chips
        entryChip()

        // select sport chips
        choiceSportChips()

        // filter chips
        filterChips()*/

    }

    private fun loadAllUsers() {
        val ref = fbDbRef.getReference("Users")

        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                searchList.clear()
                //get data
                for(ds in snapshot.children){
                    val currentUser = ds.getValue(ModelUser::class.java)

                    if(fbAuth.currentUser?.uid != currentUser?.uid){
                        searchList.add(currentUser!!)
                    }
                }
                searchAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

       /* fbDbRef.child("Users").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear a previous list
                userList.clear()
                // get the data
                for(postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(ModelUser::class.java)

                    // add all friends except the current user
                    if(fbAuth.currentUser?.uid != currentUser?.uid){
                        userList.add(currentUser!!)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })*/

    }

    /*private fun filterChips() {
        binding.filtersGroupChips.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)

            if(chip?.isChecked == true){
                Toast.makeText(this, chip.text, Toast.LENGTH_SHORT).show()
//                chip?.chipBackgroundColor = getColorStateList(
//                    R.color.primaryPaleYellow
//                )
            }
        }
    }*/

   /* private fun choiceSportChips() {
       binding.sportsChipGroup.setOnCheckedChangeListener { group, checkedId ->
           val chip: Chip? = group.findViewById(checkedId)

           chip?.let{
               Toast.makeText(this, it.text, Toast.LENGTH_SHORT).show()
           }
       }
    }*/

   /* private fun entryChip() {
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
    }*/

    /*private fun createChips(keyword: String) {
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
    }*/
}