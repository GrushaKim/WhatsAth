package com.example.mywhatsath

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mywhatsath.adapters.SearchAdapter
import com.example.mywhatsath.databinding.FragmentSearchBinding
import com.example.mywhatsath.models.ModelUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchList: ArrayList<ModelUser>
    private lateinit var searchAdapter: SearchAdapter

    private var uid = ""
    private var name = ""

    companion object{
        private const val TAG = "SEARCH_FRAGMENT_TAG"

        //get all data
        fun newInstance(uid: String, name: String): SearchFragment{
            val fragment = SearchFragment()
            val args = Bundle()
            args.putString("uid", uid)
            args.putString("name", name)
            fragment.arguments = args
            return fragment
        }
    }

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // get arguments from newInstance
        val args = arguments
        if(args != null){
            uid = args.getString("uid")!!
            name = args.getString("name")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(LayoutInflater.from(context), container, false)


        //load all tabs
        if(name == "All"){
            loadAllUsers()
        }else if(name == "Most Popular"){
            loadMostPopularUsers("heartsCnt")
        }/*else{
            loadCategorizedUsers()
        }*/

        //text search
        binding.searchEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try{
                    searchAdapter.filter!!.filter(cs)
                }catch(e: Exception){
                    Log.d(TAG, "onTextChanged: Search is unavailable. Error: ${e.message}")
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }


        })

        return binding.root
    }

    private fun loadAllUsers() {
        searchList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                searchList.clear()
                //get data to add
                for(ds in snapshot.children){
                    val currentUser = ds.getValue(ModelUser::class.java)
                        searchList.add(currentUser!!)
                }
                searchAdapter = SearchAdapter(context!!, searchList)
                binding.searchRecyclerView.adapter = searchAdapter
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        // search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    searchAdapter.filter!!.filter(s)
                } catch (e: Exception) {
                    Log.d(SearchActivity.TAG, "onTextChanged: failed to search users. Error: ${e.message}")
                }
            }

            override fun afterTextChanged(ed: Editable?) {
            }
        })
    }

    private fun loadMostPopularUsers(orderBy: String) {
        searchList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild(orderBy).limitToFirst(10)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    searchList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelUser::class.java)
                        searchList.add(model!!)
                    }
                    searchAdapter = SearchAdapter(context!!, searchList)
                    binding.searchRecyclerView.adapter = searchAdapter
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        // search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    searchAdapter.filter!!.filter(s)
                } catch (e: Exception) {
                    Log.d(SearchActivity.TAG, "onTextChanged: failed to search users. Error: ${e.message}")
                }
            }

            override fun afterTextChanged(ed: Editable?) {
            }
        })
    }


}
