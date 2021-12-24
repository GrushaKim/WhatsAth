package com.example.mywhatsath.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mywhatsath.R
import com.example.mywhatsath.adapters.MessageAdapter
import com.example.mywhatsath.databinding.ActivityChatBinding
import com.example.mywhatsath.models.ModelMessage
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import es.dmoral.toasty.Toasty
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    // fb vars
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase

    //image uri
    private var imageUri: Uri? = null

    //voice recognition intent
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    //check hearts
    private var hasHearts = false

    //single selection dialog for report
    private var selectedReport = ""

    companion object{
        const val TAG = "CHAT_TAG"
        const val REQ_PERMISSIONS_CODE = 100
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
        val receiverName = intent.getStringExtra("userName")
        val receiverImg = intent.getStringExtra("userProfileImage")

        // set recyclerView
        chatRecyclerView = binding.chatRecyclerView
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

        // load name&profileImage of recipient
        loadToolbarInfo(receiverName, receiverImg)

        // load previous messages
        loadMessages(senderId, receiverId)

        // check if imageUri exists
        checkImageUri(imageUri)

        // check if the user liked the receiver
        checkHearts(receiverId)

        // back button click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // report button click
        binding.reportBtn.setOnClickListener {
            reportUser(senderId, receiverId)
        }

        // convert speech to text
        binding.voiceBtn.setOnClickListener(View.OnClickListener {
            speechToText()
        })

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
            // check if the message contains an image
            if (imageUri == null) {
                sendMsg(senderId, receiverId, "")
            } else {
                uploadImage(senderId, receiverId)
            }
        }

        // hearts button click
        binding.heartsBtn.setOnClickListener {
            // check HasHearts
            if(hasHearts){
                removeHearts(receiverId)
            }else{
                addHearts(receiverId)
            }
        }

    }

    private fun reportUser(senderId: String?, receiverId: String?) {

        val reportSelection = arrayOf(
            "Unwanted commercial content",
            "Sexual content",
            "Child abuse",
            "Hate speech",
            "Etc"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Report")
            .setSingleChoiceItems(reportSelection, -1){_, which ->
                selectedReport = reportSelection[which]
            }
            .setPositiveButton("Submit"){ dialog, _ ->
                val hashMap = HashMap<String, Any>()
                hashMap["reporterId"] = senderId!!
                hashMap["reported"] = selectedReport

                val ref = fbDbRef.getReference("Blacklist")
                ref.child(receiverId!!)
                    .setValue(hashMap)
                    .addOnCompleteListener {
                        Log.d(TAG, "reportUser: successfully reported the user - $receiverId")
                    }

                Toasty.info(this, "Your report will be reviewed within 14 working days", Toast.LENGTH_SHORT, true).show()
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }

        val reportDialog = builder.create()
        reportDialog.show()
    }

    private fun speechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak")

        try{
            activityResultLauncher.launch(intent)
        }catch(e:ActivityNotFoundException){
            Toasty.warning(this, "Your device does not support", Toast.LENGTH_SHORT, true).show()
        }

        // implement the function above
        activityResultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){ result: ActivityResult? ->
            if(result!!.resultCode == RESULT_OK && result!!.data!=null){
                val speechText = result!!.data!!.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS) as ArrayList<Editable>
                binding.msgBoxEt.text = speechText[0]
            }
        }
    }

    private fun addHearts(receiverId: String?) {

        val timestamp = System.currentTimeMillis()

        //setup data
        val hashMap = HashMap<String, Any>()
        hashMap["userId"] = fbAuth.uid!!
        hashMap["timestamp"] = timestamp

        //save to database
        val ref = fbDbRef.getReference("Users")
        ref.child(receiverId!!).child("hearts").child(fbAuth.uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addHearts: adding to hearts")
                ref.child(receiverId!!)
                    .addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var heartsCnt = "${snapshot.child("heartsCnt").value}"
                            if(heartsCnt=="" || heartsCnt=="null"){
                                heartsCnt = "0"
                            }
                            // increase count
                            val newHeartsCnt = heartsCnt.toInt() +1
                            val hashMap = HashMap<String, Any>()
                            hashMap["heartsCnt"] = newHeartsCnt
                            ref.child(receiverId!!)
                                .updateChildren(hashMap)
                                .addOnCompleteListener {
                                    Log.d(TAG, "onDataChange: updated heartsCnt")
                                }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "addHearts: failed to add to hearts. Error: ${e.message}")
            }
    }

    private fun removeHearts(receiverId: String?) {
        // save to database
        val ref = fbDbRef.getReference("Users")
        ref.child(receiverId!!).child("hearts").child(fbAuth.uid!!)
            .removeValue()
            .addOnSuccessListener {
                ref.child(receiverId!!)
                    .addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var heartsCnt = "${snapshot.child("heartsCnt").value}"
                            if(heartsCnt=="" || heartsCnt=="null"){
                                Log.d(TAG, "removeHearts: removed hearts")
                            }else{
                                // decrease count
                                val newHeartsCnt = heartsCnt.toInt() -1
                                val hashMap = HashMap<String, Any>()
                                hashMap["heartsCnt"] = newHeartsCnt
                                ref.child(receiverId!!)
                                    .updateChildren(hashMap)
                                    .addOnCompleteListener {
                                        Log.d(TAG, "onDataChange: updated heartsCnt")
                                    }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "removeHearts: failed to remove hearts. Error: ${e.message}")
            }
    }

    private fun checkHearts(receiverId: String?) {
        Log.d(TAG, "checkHearts: check if the current user liked the receiver")
        val ref = fbDbRef.getReference("Users")
        ref.child(receiverId!!).child("hearts").child(fbAuth.uid!!)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    hasHearts = snapshot.exists()
                    if(hasHearts){
                        Log.d(TAG, "onDataChange: unavailable to click Hearts")
                        binding.heartsBtn.setImageResource(R.drawable.ic_hearts_red)
                    }else{
                        Log.d(TAG, "onDataChange: available to click Hearts")
                        binding.heartsBtn.setImageResource(R.drawable.ic_hearts)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadMessages(senderId: String, receiverId: String?) {
        val ref = fbDbRef.getReference("/Chats/$senderId/$receiverId")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()

                // get all messages
                for(ds: DataSnapshot in snapshot.children){
                    val chat = ds.getValue(ModelMessage::class.java)
                    messageList.add(chat!!)
                }
                messageAdapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messageList.size -1)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun sendMsg(senderId: String, receiverId: String?, uploadedImgUrl: String){

        // set data
        val message = binding.msgBoxEt.text.toString()
        val timestamp = System.currentTimeMillis()

        if(message.isNullOrEmpty() && uploadedImgUrl == ""){
            Toasty.warning(this, "Please type your message or attach an image", Toast.LENGTH_SHORT).show()
        }else{
            // save all messages to each room
            val senderRef = fbDbRef.getReference("/Chats/$senderId/$receiverId").push()
            val receiverRef = fbDbRef.getReference("/Chats/$receiverId/$senderId").push()

            val modelMessage = ModelMessage(
                senderRef.key!!, message, receiverId, senderId, timestamp, uploadedImgUrl)

            senderRef.setValue(modelMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "sendMsg: succesfully saved the message to senderRef")
                    imageUri = null
                    checkImageUri(imageUri)
                    binding.msgBoxEt.text.clear()
                }
                .addOnFailureListener{ e ->
                    Log.d(TAG, "sendMsg: failed to save the message. Error: ${e.message}")
                }

            receiverRef.setValue(modelMessage)

            // save the latest message
            val latestMsgRef = fbDbRef.getReference("/Latest-Message/$senderId/$receiverId")
            latestMsgRef.setValue(modelMessage)

            val latestMsgRefTo = fbDbRef.getReference("/Latest-Message/$receiverId/$senderId")
            latestMsgRefTo.setValue(modelMessage)

        }
    }

    private fun loadToolbarInfo(receiverName: String?, receiverImg: String?) {
                // set toolbar info
                binding.nameTv.text = receiverName
                if(receiverImg.isNullOrBlank()){
                    binding.profileIv.setImageResource(R.drawable.ic_baseline_person_24)
                }else{
                    try{
                        Glide.with(this@ChatActivity)
                            .load(receiverImg)
                            .into(binding.profileIv)
                    } catch(e: Exception){
                        Log.d(TAG, "onDataChange: failed to load user profileImage")
                    }
                }
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
                Toasty.error(this, "Failed to upload image. Error - ${e.message}", Toast.LENGTH_SHORT, true).show()
            }
    }


    private fun pickImageFromGallery() {
        // check permission
        var writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        var readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if(writePermission == PackageManager.PERMISSION_DENIED ||
                readPermission == PackageManager.PERMISSION_DENIED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE), REQ_PERMISSIONS_CODE)

        } else{

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            galleryActivityResultLauncher.launch(intent)

        }
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
                Toasty.normal(this, "Cancelled to upload", Toast.LENGTH_SHORT).show()
            }
        }
    )



}
