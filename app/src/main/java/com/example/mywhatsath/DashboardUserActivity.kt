package com.example.mywhatsath

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mywhatsath.adapters.UserAdapter
import com.example.mywhatsath.databinding.ActivityDashboardUserBinding
import com.example.mywhatsath.models.ModelUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding

    // firebase auth, db
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: DatabaseReference
    // userlist recyclerview & adapter
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<ModelUser>
    private lateinit var userAdapter: UserAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth&db
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance().getReference()
        checkUser()

        // init arraylist for holder
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList)

        // init recyclerview
        userRecyclerView = binding.userRecyclerView
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = userAdapter

        fbDbRef.child("Users").addValueEventListener(object: ValueEventListener{
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
        })


/*        // logout button click
        binding.logoutTv.setOnClickListener {
            fbAuth.signOut()
            checkUser()
        }*/

    }

    // inflate navigation menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    // logout
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.logout){
            fbAuth.signOut()
            checkUser()
            return true
        }

        return true
    }

    private fun checkUser() {
        // get current user
        val fbUser = fbAuth.currentUser
        if(fbUser == null){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }else{
            val email = fbUser.email
//            binding.titleTv.text = email
        }
    }
}