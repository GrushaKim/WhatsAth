package com.example.mywhatsath

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.example.mywhatsath.adapters.SearchAdapter
import com.example.mywhatsath.databinding.ActivityCategoryAddTempBinding.bind
import com.example.mywhatsath.databinding.FragmentSearchBinding
import com.example.mywhatsath.models.ModelUser
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.nio.file.Files.find

class SearchFragment : Fragment {
    private lateinit var binding: FragmentSearchBinding

    companion object{
        private const val TAG = "SEARCH_FRAGMENT_TAG"

        //get all data
        fun newInstance(id: String, sport: String): SearchFragment{
            val fragment = SearchFragment()
            val args = Bundle()
            args.putString("id", id)
            args.putString("sport", sport)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var searchList: ArrayList<ModelUser>
    private lateinit var searchAdapter: SearchAdapter

    private var id = ""
    private var sport = ""


    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get arguments from newInstance
        val args = arguments
        if(args != null){
            id = args.getString("id")!!
            sport = args.getString("sport")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(LayoutInflater.from(context), container, false)


        //load all tabs
        if(sport == "All"){
            loadAllUsers()
        }else if(sport == "Popular"){
            loadPopularUsers("heartsCnt")
        }else{
            loadCategorizedUsers()
        }

        //text search
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try{
                    searchAdapter.filter!!.filter(cs)
                }catch(e: Exception){
                    Log.d(TAG, "onTextChanged: Search is unavailable. Error: ${e.message}")
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        //entry chips for recent text search
        choiceChip()

        //set keyword when the chip is clicked
        binding.choiceCg.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)
            val searchedKeyword = chip?.text.toString()

            binding.searchEt.setText(searchedKeyword)
        }

        return binding.root
    }


    // take the recent keyword from search
    private fun choiceChip() {
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

    // create chip with keyword
    private fun createChips(keyword: String) {

        val chip = Chip(context)
        chip.apply{
            text = keyword
            chipIcon = ContextCompat.getDrawable(
                context,
                R.drawable.ic_launcher_background
            )
            isChipIconVisible = false
            isCloseIconVisible = true
            isClickable = true
            isCheckable = true

            // set add+remove a single chip
            binding.apply{
                choiceCg.addView(chip as View)
                chip.setOnCloseIconClickListener {
                    choiceCg.removeView(chip as View)
                    binding.searchEt.text.clear()
                }
            }
        }
    }

    private fun loadCategorizedUsers() {
        searchList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild("sport").equalTo(sport)
            .addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                searchList.clear()
                //get data to add
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelUser::class.java)
                    searchList.add(model!!)
                }
                searchAdapter = SearchAdapter(context!!, searchList)
                binding.searchRecyclerView.adapter = searchAdapter
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadAllUsers() {
        searchList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                searchList.clear()
                //get data to add
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelUser::class.java)
                        searchList.add(model!!)
                }
                searchAdapter = SearchAdapter(context!!, searchList)
                binding.searchRecyclerView.adapter = searchAdapter
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadPopularUsers(orderBy: String) {
        searchList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild(orderBy).limitToLast(10)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    searchList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelUser::class.java)
                        searchList.add(model!!)
                    }
                    searchList.reverse()
                    searchAdapter = SearchAdapter(context!!, searchList)
                    binding.searchRecyclerView.adapter = searchAdapter
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }


}
