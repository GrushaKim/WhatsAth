package com.example.mywhatsath.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.mywhatsath.adapters.SearchAdapter
import com.example.mywhatsath.databinding.ActivitySearchBinding
import com.example.mywhatsath.models.ModelSport
import com.example.mywhatsath.models.ModelUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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
        categoryList = ArrayList()

        // load all sport categories from db
        val ref = fbDbRef.getReference("Sports")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()

                val modelAll = ModelSport("01", "All")
                val modelPopular = ModelSport("01", "Popular")

                categoryList.add(modelAll)
                categoryList.add(modelPopular)

                viewPagerAdapter.addFragment(
                    SearchFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.sport}",
                    ), modelAll.sport!!
                )
                viewPagerAdapter.addFragment(
                    SearchFragment.newInstance(
                        "${modelPopular.id}",
                        "${modelPopular.sport}"
                    ), modelPopular.sport!!
                )

                viewPagerAdapter.notifyDataSetChanged()

                // load all sport categories from db
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelSport::class.java)
                        categoryList.add(model!!)
                        viewPagerAdapter.addFragment(
                            SearchFragment.newInstance(
                                "${model.id}",
                                "${model.sport}"
                            ), model.sport!!
                        )
                        viewPagerAdapter.notifyDataSetChanged()
                    }
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


}