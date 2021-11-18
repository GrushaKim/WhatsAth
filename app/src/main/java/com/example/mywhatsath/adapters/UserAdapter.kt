package com.example.mywhatsath.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mywhatsath.databinding.ItemDashboardUserBinding
import com.example.mywhatsath.models.ModelUser
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter: RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private val context: Context
    var userList: ArrayList<ModelUser>

    private lateinit var binding: ItemDashboardUserBinding

    // init
    constructor(context: Context, userList: ArrayList<ModelUser>) : super() {
        this.context = context
        this.userList = userList
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
        holder.nameTv.text = currentUser.name

//        Glide.with(context).load(currentUser.profileImage).into(holder.profileImageIv)

    }


    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameTv: TextView = binding.nameTv
        val dummyTv: TextView = binding.dummyTv
        val profileImageIv: CircleImageView = binding.profileImageIv
    }


}