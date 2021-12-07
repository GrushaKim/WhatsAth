package com.example.mywhatsath

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mywhatsath.adapters.UserAdapter
import com.example.mywhatsath.databinding.ActivityDashboardUserBinding
import com.example.mywhatsath.models.ModelUser
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding

    // firebase auth, db
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    companion object{
        const val TAG = "DASHBOARD_USER_TAG"
    }
    // userlist recyclerview & adapter
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<ModelUser>
    private lateinit var followedList: ArrayList<Any>
    private lateinit var userAdapter: UserAdapter

    var latestMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth&db
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()
        checkUser()

        // init arraylist for holder
        userList = ArrayList()
        followedList = ArrayList()
        userAdapter = UserAdapter(this, userList)

        // init recyclerview
        userRecyclerView = binding.userRecyclerView
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = userAdapter

        /*setItemTouchHelper()*/

        // load chatlist
        loadChatlist()

        // bottom drawer
        BottomSheetBehavior.from(binding.bottomDrawerSheet).apply {
            peekHeight = 180
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }


        // <Drawer menu>
        binding.logoutBtn.setOnClickListener {
            fbAuth.signOut()
            checkUser()
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this@DashboardUserActivity, ProfileActivity::class.java))
        }

        binding.searchBtn.setOnClickListener {
            startActivity(Intent(this@DashboardUserActivity, SearchActivity::class.java))
        }

        binding.chatBtn.setOnClickListener {
            this.recreate()
        }



    }

   /* private fun setItemTouchHelper() {
        ItemTouchHelper(object: ItemTouchHelper.Callback(){
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = 0 // no drag
                val swipeFlags = ItemTouchHelper.LEFT
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if(4 == direction){
                    viewHolder.itemId
                    Log.d(TAG, "onSwiped: ${viewHolder.itemId}")
                }
            }
        }).apply {
            attachToRecyclerView(userRecyclerView)
        }
    }*/

    private fun loadChatlist() {
        val ref = fbDbRef.getReference("Users")

        ref.child(fbAuth.uid!!)
            .child("followed")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()

                    for(ds in snapshot.children){
                        val followedUser = ds.child("uid").getValue(String::class.java)

                        ref.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {

                                for(pds in snapshot.children){
                                    val currentUser = pds.getValue(ModelUser::class.java)
                                    if(currentUser!!.uid == followedUser)
                                        userList.add(currentUser!!)
                                }

                                userAdapter.notifyDataSetChanged()
                            }
                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        /* val ref = fbDbRef.getReference("Users")
         ref.child(fbAuth.uid!!).child("followed").addListenerForSingleValueEvent(object: ValueEventListener{
             override fun onDataChange(snapshot: DataSnapshot) {
 //                followedList.clear()
                 for(ds in snapshot.children) {
                     val followedUser =  ds.child("uid").value.toString()
 //                    followedList.add(followedUser!!)
                     Log.d(TAG, "onDataChange: $followedUser")
                     ref.child(followedUser!!).addListenerForSingleValueEvent(object: ValueEventListener{
                         override fun onDataChange(snapshot: DataSnapshot) {
                             userList.clear()
                             for(ds in snapshot.children){
                                 val currentUser = ds.getValue(ModelUser::class.java)
                                 userList.add(currentUser!!)
                             }
                             userAdapter.notifyDataSetChanged()
                         }
                         override fun onCancelled(error: DatabaseError) {
                         }
                 })
             }
             }
             override fun onCancelled(error: DatabaseError) {
             }
         })*/

        /*ref.child(fbAuth.uid!!).child("followed").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear a previous list
                userList.clear()
                // get the data
                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(ModelUser::class.java)
                        userList.add(currentUser!!)
                }
                userAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })*/
    }



    private fun checkUser() {
        // get current user
        val fbUser = fbAuth.currentUser
        if(fbUser == null){
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }else{
            val email = fbUser.email
        }
    }
}