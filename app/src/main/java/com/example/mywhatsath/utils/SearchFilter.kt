package com.example.mywhatsath.utils

import android.widget.Filter
import com.example.mywhatsath.adapters.SearchAdapter
import com.example.mywhatsath.models.ModelUser

class SearchFilter: Filter {
    val filterList: ArrayList<ModelUser>
    var searchAdapter: SearchAdapter

    constructor(filterList: ArrayList<ModelUser>, searchAdapter: SearchAdapter) : super() {
        this.filterList = filterList
        this.searchAdapter = searchAdapter
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if(constraint != null && constraint.isNotEmpty()){
            val filteredModels: ArrayList<ModelUser> = ArrayList()
            // validate data
            for(i in filterList.indices){
                if(filterList[i].email!!.lowercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            // return results
            results.count = filteredModels.size
            results.values = filteredModels
        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        // apply changes
        searchAdapter.searchList = results.values as ArrayList<ModelUser>
        searchAdapter.notifyDataSetChanged()

    }
}