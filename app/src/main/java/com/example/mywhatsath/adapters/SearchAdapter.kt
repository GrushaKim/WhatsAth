package com.example.mywhatsath.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mywhatsath.ChatActivity
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ItemSearchListBinding
import com.example.mywhatsath.models.ModelUser
import com.example.mywhatsath.utils.SearchFilter

class SearchAdapter: RecyclerView.Adapter<SearchAdapter.HolderSearch>, Filterable {

    private var context: Context
    var searchList: ArrayList<ModelUser>
    var filterList: ArrayList<ModelUser>

    private lateinit var binding: ItemSearchListBinding

    private var filter: SearchFilter? = null

    constructor(
        context: Context,
        searchList: ArrayList<ModelUser>
    ) : super() {
        this.context = context
        this.searchList = searchList
        this.filterList = searchList
    }


    // inflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderSearch {
        binding = ItemSearchListBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderSearch(binding.root)
    }

    // data getter+setter
    override fun onBindViewHolder(holder: HolderSearch, position: Int) {
        val model = searchList[position]
        val uid = model.uid
        val profileImage = model.profileImage
        val name = model.name
        val email = model.email
        val gender = model.gender
        val heartsCnt = model.hearts
        val sport = model.sport
        val level = model.level

        // set to holder
        holder.nameTv.text = name
        holder.emailTv.text = email
        if(profileImage.isNullOrBlank()){
            holder.profileIv.setImageResource(R.drawable.ic_baseline_person_24)
        }else{
            try{
                Glide.with(context)
                    .load(profileImage)
                    .into(holder.profileIv)
            } catch(e: Exception){
                Log.d("SearchAdapter_TAG", "onBindViewHolder: Failed to load profileImage")
            }
        }

        // move to chat
        holder.profileIv.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("userId", uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    // viewholder for search list items
    inner class HolderSearch(itemView: View): RecyclerView.ViewHolder(itemView) {
        var profileIv = binding.profileIv
        var nameTv = binding.nameTv
        var emailTv = binding.emailTv
        var genderTv = binding.genderTv
        var genderIv = binding.genderIv
        var heartsCntTv = binding.heartsCntTv
        var sportTv = binding.sportTv
        var levelTv = binding.levelTv
    }

    // filtering
    override fun getFilter(): Filter {
        if(filter == null){
            filter = SearchFilter(filterList, this)
        }
        return filter as SearchFilter
    }
}