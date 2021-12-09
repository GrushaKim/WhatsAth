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
import com.example.mywhatsath.models.ModelMessage
import com.example.mywhatsath.models.ModelUser
import com.example.mywhatsath.utils.SwipeToDeleteCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.protobuf.Value

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
    private lateinit var userAdapter: UserAdapter


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
        userAdapter = UserAdapter(this, userList)

        // init recyclerview with swipe to delete a specific chat
        userRecyclerView = binding.userRecyclerView.apply{
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@DashboardUserActivity)
            val swipeDelete = object: SwipeToDeleteCallback(this@DashboardUserActivity){
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val arrPosition = viewHolder.absoluteAdapterPosition
                    val receiverId = userList[arrPosition].uid.toString()

                    userAdapter.deleteItem(viewHolder.adapterPosition, receiverId)
                }
            }

            val touchHelper = ItemTouchHelper(swipeDelete)
            touchHelper.attachToRecyclerView(this)
        }


        /*setItemTouchHelper()*/

        // load chatlist
        loadChatlist()

        // load userList
        loadUserList()


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

        binding.helpBtn.setOnClickListener {
            startActivity(Intent(this@DashboardUserActivity, CategoryAddTempActivity::class.java))
        }

        binding.chatBtn.setOnClickListener {
            this.recreate()
        }




    }

    private fun loadUserList() {
        val ref = fbDbRef.getReference("Users")


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
        val chatRef = fbDbRef.getReference("Chats")
        val userRef = fbDbRef.getReference("Users")

        chatRef.child(fbAuth.uid!!)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    userList.clear()

                    for(ds in snapshot.children){
                        Log.d(TAG, "onDataChange: successfully get the chat key - ${ds.key}")
                        val chatKey = ds.key!!

                        // check if uid matches
                        userRef.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for(pds in snapshot.children){
                                    val chatUser = pds.getValue(ModelUser::class.java)
                                    if(chatUser!!.uid == chatKey)
                                        userList.add(chatUser!!)
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