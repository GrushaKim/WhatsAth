package com.example.mywhatsath.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.mywhatsath.R
import com.example.mywhatsath.R.menu.main_toolbar_menu
import com.example.mywhatsath.databinding.ActivitySearchBinding
import com.example.mywhatsath.models.ModelSport
import com.facebook.appevents.suggestedevents.ViewOnClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    // firebase auth, db
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    companion object {
        const val TAG = "SEARCH_TAG"
    }

    // adapter
    private lateinit var categoryList: ArrayList<ModelSport>

    // viewPager
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init main toolbar
        setSupportActionBar(binding.mainToolbar)
        binding.mainToolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        // init auth&db
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()


        // inflate fragments
        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

    }

    // inflate menu to toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(main_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // set menu functions
    override fun onOptionsItemSelected(item: MenuItem) = when(item?.itemId) {
            R.id.homeBtn -> {
                startActivity(Intent(this@SearchActivity, DashboardUserActivity::class.java))
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

    private fun checkUser() {
        // get current user
        val fbUser = fbAuth.currentUser
        if (fbUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}