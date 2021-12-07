package com.example.mywhatsath

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mywhatsath.adapters.MessageAdapter
import com.example.mywhatsath.databinding.ActivityChatBinding
import com.example.mywhatsath.models.ModelMessage
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    // fb vars
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    //image uri
    private var imageUri: Uri? = null

    //progress dialog
    private lateinit var pDialog: ProgressDialog

    companion object{
        const val TAG = "CHAT_TAG"
    }

    // messagelist recyclerview & adapter
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<ModelMessage>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth & progress dialog
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()

        // extract info from DashboardUserActivity
        val senderId = fbAuth.currentUser!!.uid
        val receiverId = intent.getStringExtra("userId")

        // set recyclerView
        chatRecyclerView = binding.chatRecyclerView
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter


        // load name&profileImage of recipient
        loadToolbarInfo(receiverId)

        // load previous messages
        loadMessages(senderId, receiverId)

        // check if imageUri exists
        checkImageUri(imageUri)

        // back button click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // block click
        binding.blockBtn.setOnClickListener {
            blockUser(receiverId)
        }

        // pick image from gallery
        binding.uploadImgBtn.setOnClickListener {
            pickImageFromGallery()
        }

        // cancel to upload image
        binding.cancelUploadBtn.setOnClickListener{
            imageUri = null
            checkImageUri(imageUri)
        }

        // send message click
        binding.sendMsgBtn.setOnClickListener {
            if (imageUri == null) {
                sendMsg(senderId, receiverId, "")
            } else {
                uploadImage(senderId, receiverId)
            }
        }
    }

    private fun blockUser(receiverId: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Block this user?")
            .setCancelable(false)
            .setPositiveButton("Block"){ dialog, id ->
                val ref = fbDbRef.getReference("Users")
                val hashMap = HashMap<String, Any>()
                hashMap.put(receiverId.toString(), receiverId.toString())
                ref.child(fbAuth.uid!!).child("blocked")
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        Log.d(TAG, "blockUser: successfully blocked the user")
                    }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "blockUser: failed to block the user")
                    }
            }
            .setNegativeButton("Cancel"){ dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun loadMessages(senderId: String, receiverId: String?) {
        val ref = fbDbRef.getReference("Chat")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()

                // get all messages
                for(ds: DataSnapshot in snapshot.children){
                    val chat = ds.getValue(ModelMessage::class.java)

                    if(chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)
                    ){
                        messageList.add(chat)
                    }
                }
                messageAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun sendMsg(senderId: String, receiverId: String?, uploadedImgUrl: String){

        // set data
        val hashMap: HashMap<String, Any?> = HashMap()
        val message = binding.msgBoxEt.text.toString()
        val timestamp = System.currentTimeMillis()

        if(message.isNullOrEmpty() && uploadedImgUrl == ""){
            Toast.makeText(this, "Please type your message or attach an image", Toast.LENGTH_SHORT).show()
        }else{
            hashMap["senderId"] = senderId
            hashMap["receiverId"] = receiverId
            hashMap["message"] = message
            hashMap["image"] = uploadedImgUrl
            hashMap["timestamp"] = timestamp

            val ref = fbDbRef.getReference()
            ref.child("Chat")
                .push()
                .setValue(hashMap)
                .addOnSuccessListener {
                    Log.d(TAG, "sendMessage: This message has successfully been sent")
                    imageUri = null
                    checkImageUri(imageUri)
                    binding.msgBoxEt.setText("")

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send a message", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "sendMessage: Failed to send a message. Error: ${e.message}")
                    imageUri = null
                    checkImageUri(imageUri)
                    binding.msgBoxEt.setText("")

                }
        }


    }



    private fun loadToolbarInfo(receiverId: String?) {

        val ref = fbDbRef.getReference("Users")
        ref.child(receiverId!!).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val profileImage = "${snapshot.child("profileImage").value}"
                val name = "${snapshot.child("name").value}"

                // set toolbar info
                binding.nameTv.text = name
                if(profileImage.isNullOrBlank()){
                    binding.profileIv.setImageResource(R.drawable.ic_baseline_person_24)
                }else{
                    try{
                        Glide.with(this@ChatActivity)
                            .load(profileImage)
                            .into(binding.profileIv)
                    } catch(e: Exception){
                        Log.d(TAG, "onDataChange: failed to load user profileImage")
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // change upload img icon color
    private fun checkImageUri(imageUri: Uri?) {
        if(imageUri == null){
            binding.uploadImgBtn.setImageResource(R.drawable.ic_baseline_insert_photo_24_gray)
            binding.cancelUploadBtn.visibility = View.INVISIBLE
        }else{
            binding.uploadImgBtn.setImageResource(R.drawable.ic_baseline_insert_photo_24)
            binding.cancelUploadBtn.visibility = View.VISIBLE
        }
    }

    private fun uploadImage(senderId: String, receiverId: String?) {

        // image path, name
        val filePathAndName = "ChatImages/$senderId"
        //storage
        val ref = FirebaseStorage.getInstance().getReference(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                //get uri of uploaded image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadedImgUrl = "${uriTask.result}"

                sendMsg(senderId, receiverId, uploadedImgUrl)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image. Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        galleryActivityResultLauncher.launch(intent)
    }


    // to handle gallery intent result
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                checkImageUri(imageUri)
            }else{
                Toast.makeText(this, "Cancelled to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    )



}

