package com.example.mywhatsath

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mywhatsath.adapters.MessageAdapter
import com.example.mywhatsath.databinding.ActivityChatBinding
import com.example.mywhatsath.models.ModelMessage
import com.example.mywhatsath.utils.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    // fb vars
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    // messagelist recyclerview & adapter
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<ModelMessage>

    var receiverRoom: String? = null
    var senderRoom: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()

        // extract info from DashboardUserActivity
        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")

        // make serials for each chatroom
        val senderUid = fbAuth.currentUser?.uid
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        supportActionBar?.title = name

        // init recyclerview&arr
        chatRecyclerView = binding.chatRecyclerView
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        // add message data to recyclerview
        fbDbRef.getReference("Chats").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for(postSnapshot in snapshot.children){

                        val msg = postSnapshot.getValue(ModelMessage::class.java)
                        messageList.add(msg!!)

                    }
                    messageAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })

        // send message button click
        binding.sendMsgBtn.setOnClickListener {

            // add msg to DB
            val msg = binding.msgBoxEt.text.toString()
            val senderId = fbAuth.currentUser!!.uid
            val timestamp = System.currentTimeMillis()
            val hashMap: HashMap<String, Any?> = HashMap()

            hashMap["message"] = msg
            hashMap["senderId"] = senderId
            hashMap["timestamp"] = timestamp


            fbDbRef.getReference("Chats").child(senderRoom!!)
                .child("messages").push()
                .setValue(hashMap)
                .addOnSuccessListener {
                    fbDbRef.getReference("Chats").child(receiverRoom!!)
                        .child("messages").push()
                        .setValue(hashMap)
                }
            binding.msgBoxEt.setText("")
        }
    }
}