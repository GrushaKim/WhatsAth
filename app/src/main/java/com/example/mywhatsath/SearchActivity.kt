package com.example.mywhatsath

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.mywhatsath.adapters.MessageAdapter
import com.example.mywhatsath.adapters.SearchAdapter
import com.example.mywhatsath.databinding.ActivitySearchBinding
import com.example.mywhatsath.models.ModelMessage
import com.example.mywhatsath.models.ModelSport
import com.example.mywhatsath.models.ModelUser
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.protobuf.Value

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    // firebase auth, db
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    companion object {
        const val TAG = "SEARCH_TAG"
    }

    // adapter
    private lateinit var searchList: ArrayList<ModelUser>
    private lateinit var categoryList: ArrayList<ModelSport>
    private lateinit var userList: ArrayList<ModelUser>
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchRecyclerView: RecyclerView

    // viewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // init
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()


        // inflate fragments
        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)





//        loadAllUsers()

        // set search conditions
//        setSportsFilterChips()
//        setLevelsFilterChips()



       /* binding.levelsCg.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)
            val level = chip?.text.toString().trim()

            Toast.makeText(this, level, Toast.LENGTH_SHORT).show()

            try {
                searchAdapter.filter!!.filter(level)
                Log.d(TAG, "onCreate: $level")

            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }*/
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager) {
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )
        userList = ArrayList()

        // load all sport categories from db
        val ref = fbDbRef.getReference("Users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                val modelAll = ModelUser("01", "All", "", "", "", "", "", "", "", 0)
                val modelMostPopularUsers =
                    ModelUser("01", "Most Popular", "", "", "", "", "", "", "", 0)

                userList.add(modelAll)
                userList.add(modelMostPopularUsers)

                viewPagerAdapter.addFragment(
                    SearchFragment.newInstance(
                        "${modelAll.uid}",
                        "${modelAll.name}"
                    ), modelAll.name!!
                )
                viewPagerAdapter.addFragment(
                    SearchFragment.newInstance(
                        "${modelMostPopularUsers.uid}",
                        "${modelMostPopularUsers.name}"
                    ), modelAll.name!!
                )

                viewPagerAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        // inflate adapter to viewPager
        viewPager.adapter = viewPagerAdapter
    }

    /* // create chips
     entryChip()

     // select sport chips
     choiceSportChips()*/


/*    private fun setSportsFilterChips() {
        binding.sportsCg.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)
            val keyword = chip?.text.toString()

            if(chip?.isChecked == true){
                binding.searchEt.setText(keyword)
            }
        }
    }*/
/*
    private fun setLevelsFilterChips() {
        binding.levelsCg.setOnCheckedChangeListener { group, checkedId ->
            val chip: Chip? = group.findViewById(checkedId)
            }
        }
*/


/*    private fun loadAllUsers() {
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

    }
}*/

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context) :
        FragmentPagerAdapter(fm, behavior) {
        private val fragmentsList: ArrayList<SearchFragment> = ArrayList()
        private val fragmentCategoryList: ArrayList<String> = ArrayList()
        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentCategoryList[position]
        }

        fun addFragment(fragment: SearchFragment, category: String) {
            fragmentsList.add(fragment)
            fragmentCategoryList.add(category)
        }

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