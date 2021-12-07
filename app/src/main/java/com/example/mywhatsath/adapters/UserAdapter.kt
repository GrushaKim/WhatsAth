package com.example.mywhatsath.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mywhatsath.ChatActivity
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ItemDashboardUserBinding
import com.example.mywhatsath.models.ModelUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter: RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private val context: Context
    var userList: ArrayList<ModelUser>
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    private lateinit var binding: ItemDashboardUserBinding

    // init
    constructor(context: Context, userList: ArrayList<ModelUser>) : super() {
        this.context = context
        this.userList = userList
    }

    // delete specific chat
    fun deleteItem(viewHolder: RecyclerView.ViewHolder){
        val hashMap: HashMap<String, Any?> = HashMap()
        /* list.removeAt(viewHolder.adapterPosition)
        * notifyItemRemoved(viewHolder.adapterPosition)*/
        /*userList.removeAt(i)
        notifyDataSetChanged()*/
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

        val ref = fbDbRef.getReference("Chat")




        // set the data
        holder.nameTv.text = currentUser.name
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

            context.startActivity(intent)
        }


    }






    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameTv: TextView = binding.nameTv
        val dummyTv: TextView = binding.dummyTv
        val infoLl: LinearLayout = binding.infoLl
        val profileImageIv: CircleImageView = binding.profileImageIv
        val moreBtn: ImageView = binding.moreBtn

        init{
            moreBtn.setOnClickListener { popupMenus(it) }
        }

        private fun popupMenus(itemView: View){
            val position = userList[adapterPosition]
            val popupMenus = PopupMenu(context, itemView)
            popupMenus.inflate(R.menu.menu)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    // add to favorite
                    R.id.hearts -> {
                        Toast.makeText(context, "userId is ${position.uid} ", Toast.LENGTH_SHORT).show()
                        true
                    }
                    // show profile
                    R.id.profile -> {

                        true
                    }
                    // block the user
                    R.id.block -> {

                        true
                    }
                    else -> true
                }
            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu,true)
        }
    }


}