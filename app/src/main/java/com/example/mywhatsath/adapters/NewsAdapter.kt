package com.example.mywhatsath.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mywhatsath.databinding.ItemNewsHeadlineBinding
import com.example.mywhatsath.models.ModelArticle
import com.squareup.picasso.Picasso

class NewsAdapter: RecyclerView.Adapter<NewsAdapter.HolderNews> {
    private var context: Context
    var headlineList: ArrayList<ModelArticle>

    private lateinit var binding: ItemNewsHeadlineBinding

    constructor(context: Context, headlineList: ArrayList<ModelArticle>) : super() {
        this.context = context
        this.headlineList = headlineList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderNews {
        binding = ItemNewsHeadlineBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderNews(binding.root)
    }

    override fun onBindViewHolder(holder: HolderNews, position: Int) {
        val model = headlineList[position]

        holder.titleTv.text = model.title
        holder.agentTv.text = model.source?.name

        // check if the loaded article has a thumbnail image
        if(model.urlToImage != null){
            Picasso.get()
                .load(model.urlToImage)
                .into(holder.headlineIv)
        }

    }

    override fun getItemCount(): Int {
        return headlineList.size
    }

    inner class HolderNews(itemView: View): RecyclerView.ViewHolder(itemView) {
        var titleTv = binding.titleTv
        var agentTv = binding.agentTv
        var headlineIv = binding.headlineIv
    }

}