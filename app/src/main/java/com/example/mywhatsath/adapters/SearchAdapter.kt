package com.example.mywhatsath.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mywhatsath.ChatActivity
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ItemSearchListBinding
import com.example.mywhatsath.models.ModelUser
import com.example.mywhatsath.utils.SearchFilter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SearchAdapter: RecyclerView.Adapter<SearchAdapter.HolderSearch>, Filterable {

    private var context: Context
    var searchList: ArrayList<ModelUser>
    var filterList: ArrayList<ModelUser>

    private lateinit var binding: ItemSearchListBinding
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

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
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()
        val hashMap: HashMap<String, Any?> = HashMap()

        val model = searchList[position]
        val uid = model.uid

        hashMap["uid"] = uid

        // set to holder
        holder.nameTv.text = model.name
        holder.emailTv.text = model.email
        holder.sportTv.text = model.sport
        holder.levelTv.text = model.level
        holder.heartsCntTv.text = model.heartsCnt.toString()

        if(model.profileImage.isNullOrBlank()){
            holder.profileIv.setImageResource(R.drawable.ic_baseline_person_24)
        }else{
            try{
                Glide.with(context)
                    .load(model.profileImage)
                    .into(holder.profileIv)
            } catch(e: Exception){
                Log.d("SearchAdapter_TAG", "onBindViewHolder: Failed to load profileImage")
            }
        }

        if(model.gender!!.lowercase() == R.string.male.toString().lowercase()){
            holder.genderIv.setImageResource(R.drawable.ic_man)
        }else{
            holder.genderIv.setImageResource(R.drawable.ic_woman)
        }

        // confirm if the current user want to contact the selected user
        holder.profileIv.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage("Add ${model.name} to your chatlist?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    // add uid to followings&followed
                    val ref = fbDbRef.getReference("Users")
                    ref.child(fbAuth.uid!!).child("followed")
                        .child(uid!!)
                        .setValue(hashMap)
                        .addOnSuccessListener {
                            Log.d("SEARCHADAPTER_TAG", "onBindViewHolder: successfully added to followed")

                            val hashMap2: HashMap<String, Any?> = HashMap()
                            hashMap2["uid"] = fbAuth.uid!!

                            ref.child(uid!!).child("followings")
                                .child(fbAuth.uid!!)
                                .setValue(hashMap2)
                                .addOnSuccessListener {
                                    Log.d("SEARCHADAPTER_TAG", "onBindViewHolder: successfully added to followings")
                                }
                                .addOnFailureListener { e ->
                                    Log.d("SEARCHADAPTER_TAG", "onBindViewHolder: failed to add to followings. Error: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to contact", Toast.LENGTH_SHORT).show()
                            Log.d("SEARCHADAPTER_TAG", "onBindViewHolder: failed to add to followed. Error: ${e.message}")
                        }

                    // move to chat function
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("userId", uid)
                    context.startActivity(intent)
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()


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