package com.example.mywhatsath.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mywhatsath.activities.ChatActivity
import com.example.mywhatsath.activities.ProfileActivity
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ItemDashboardUserBinding
import com.example.mywhatsath.models.ModelMessage
import com.example.mywhatsath.models.ModelUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter: RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private val context: Context
    var userList: ArrayList<ModelUser>
    var latestMessage: String = ""
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    private lateinit var binding: ItemDashboardUserBinding

    // init
    constructor(context: Context, userList: ArrayList<ModelUser>) : super() {
        this.context = context
        this.userList = userList
    }

    // delete specific chat by swipe
    fun deleteItem(index: Int, receiverId: String){
        val ref = FirebaseDatabase.getInstance().getReference("Chats")

        userList.removeAt(index)
        notifyDataSetChanged()

        ref.child(fbAuth.uid!!)
            .child("$receiverId")
            .removeValue()
            .addOnSuccessListener {
                Log.d("UserAdapter_TAG", "deleteItem: successfully deleted messages ")
                Toast.makeText(context, "All messages deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d("UserAdapter_TAG", "deleteItem: failed to delete messages. Error: ${e.message} ")
            }

    }

    fun blockItem(i: Int){

        userList.removeAt(i)
        notifyDataSetChanged()
    }

    // inflate item_dashboard_user.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        binding = ItemDashboardUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return UserViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        val profileImage = currentUser.profileImage

        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()
        latestMessage = String()

        val ref = fbDbRef.getReference("Latest-Message/${fbAuth.uid}/${currentUser.uid}")

        // set the data
        holder.nameTv.text = currentUser.name

        // get the latest message of each chat

            // check if each chat has the latest message
            ref.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val modelMessage = snapshot.getValue(ModelMessage::class.java)
                        latestMessage = modelMessage!!.message!!.toString()
                        holder.dummyTv.text = latestMessage
                        Log.d("UserAdapter_TAG", "onDataChange: latestMessage of user - ${fbAuth.uid} to the user - ${currentUser.uid}is $latestMessage")
                    }else{
                        latestMessage = ""
                        holder.dummyTv.text = latestMessage
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        if(profileImage.isNullOrBlank()){
            holder.profileImageIv.setImageResource(R.drawable.ic_baseline_person_24)
        }else{
            try{
                Glide.with(context)
                    .load(profileImage)
                    .into(holder.profileImageIv)
            } catch(e: Exception){
                Log.d("UserAdapter_TAG", "onBindViewHolder: Failed to load profileImage")
            }
        }


        // move to individual chat
        holder.infoLl.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("userId", currentUser.uid)
            intent.putExtra("userName", currentUser.name)
            intent.putExtra("userProfileImage", currentUser.profileImage)

            context.startActivity(intent)
        }

        holder.profileImageIv.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            intent.putExtra("userId", currentUser.uid)

            context.startActivity(intent)
        }


    }

    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameTv: TextView = binding.nameTv
        val dummyTv: TextView = binding.dummyTv
        val infoLl: LinearLayout = binding.infoLl
        val profileImageIv: CircleImageView = binding.profileImageIv


    }


}