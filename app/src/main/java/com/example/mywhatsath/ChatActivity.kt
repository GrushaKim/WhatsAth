package com.example.mywhatsath

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mywhatsath.adapters.MessageAdapter
import com.example.mywhatsath.databinding.ActivityChatBinding
import com.example.mywhatsath.models.ModelMessage
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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

    var receiverRoom: String? = null
    var senderRoom: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth & progress dialog
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()
        pDialog = ProgressDialog(this)
        pDialog.setTitle("Please wait")
        pDialog.setCanceledOnTouchOutside(false)

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

        // check image attachment status
        checkImageUri()

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

        // upload image button click
        binding.uploadImgBtn.setOnClickListener {
            pickImageFromGallery()
        }

      binding.sendMsgBtn.setOnClickListener {

          if(imageUri==null){
              sendMessage("")
          }else{
              uploadImage()
          }
      }

    }

    private fun checkImageUri() {
        if(imageUri==null){
            binding.uploadImgBtn.setImageResource(R.drawable.ic_baseline_add_a_photo_gray24)
        }else{
            binding.uploadImgBtn.setImageResource(R.drawable.ic_baseline_add_a_photo_24)
        }
    }

    private fun uploadImage(){
        pDialog.setMessage("Uploading image")
        pDialog.show()

        // image path, name
        val filePathAndName = "ChatImages/"+fbAuth.uid
        //storage
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                pDialog.dismiss()

                //get uri of uploaded image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                sendMessage(uploadedImageUrl)
            }
            .addOnFailureListener { e ->
                pDialog.dismiss()
                Toast.makeText(this, "Failed to upload image. Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

        private fun sendMessage(uploadedImageUrl: String) {

            // add msg to DB
            val msg = binding.msgBoxEt.text.toString()
            val senderId = fbAuth.currentUser!!.uid
            val timestamp = System.currentTimeMillis()
            val hashMap: HashMap<String, Any?> = HashMap()

            hashMap["message"] = msg
            hashMap["senderId"] = senderId
            hashMap["timestamp"] = timestamp

            if(imageUri != null){
                hashMap["msgImage"] = uploadedImageUrl
            }

            fbDbRef.getReference("Chats").child(senderRoom!!)
                .child("messages").push()
                .setValue(hashMap)
                .addOnSuccessListener {

                    fbDbRef.getReference("Chats").child(receiverRoom!!)
                        .child("messages").push()
                        .setValue(hashMap)

                    binding.uploadImgBtn.setImageResource(R.drawable.ic_baseline_add_a_photo_gray24)
                    binding.msgBoxEt.hint = "Send a message..."

                    // nullify the value of imageUri for the next message
                    imageUri = null
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to send message. Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            binding.msgBoxEt.setText("")

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
                checkImageUri()
                binding.msgBoxEt.hint = "Send the image attached"
            }else{
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )




}