package com.example.mywhatsath.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ActivityProfileEditBinding
import com.example.mywhatsath.models.ModelSport
import com.example.mywhatsath.utils.MyApplication
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase
    private lateinit var pDialog: ProgressDialog

    //image uri
    private var imageUri: Uri? = null

    // sports select dialog
    private lateinit var sportsList: ArrayList<ModelSport>
    private var selectedSportId = ""
    private var selectedSport = ""

    //info to be updated
    private var updatedName = ""
    private var updatedLevel = ""
    private var updatedAboutMe = ""

    private val maxLength = 100

    companion object{
        const val TAG = "PROFILE_EDIT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth&db
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()

        // set progressDialog
        pDialog = ProgressDialog(this)
        pDialog.setTitle("Please wait")
        pDialog.setCanceledOnTouchOutside(false)

        //load the profile
        loadUserProfile()

        // load sport categories
        loadSports()

        // upload profile image
        binding.profileIv.setOnClickListener {
            pickImageFromGallery()
        }

        binding.sportTv.setOnClickListener {
            sportPickDialog()
        }

        // level radio group click
        binding.levelRg.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId){
                R.id.amateurRb -> updatedLevel = resources.getStringArray(R.array.levels)[0].toString()
                R.id.semiProRb -> updatedLevel = resources.getStringArray(R.array.levels)[1].toString()
                R.id.proRb -> updatedLevel = resources.getStringArray(R.array.levels)[2].toString()
            }
        }

        // save profile click button
        binding.saveProfileBtn.setOnClickListener {
            if(imageUri==null){
                updateProfile("")
            }else{
                uploadImage()
            }
        }

        // cancel
        binding.cancelBtn.setOnClickListener{
            onBackPressed()
        }

        // set text limit of aboutMe section
        binding.aboutMeEt.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
    }

    private fun loadSports() {
        sportsList = ArrayList()
        // get data from db
        val ref = fbDbRef.getReference("Sports")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                sportsList.clear()
                for(ds in snapshot.children){
                    val model = ds.getValue(ModelSport::class.java)
                    sportsList.add(model!!)
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadUserProfile() {
        val ref = fbDbRef.getReference("Users")
        ref.child(fbAuth.uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user profile
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val name = "${snapshot.child("name").value}"
                    val email = "${snapshot.child("email").value}"
                    val sex = "${snapshot.child("sex").value}"
                    val regDate = "${snapshot.child("regDate").value}"
                    val aboutMe = "${snapshot.child("aboutMe").value}"


                    //convert regdate
                    val formattedRegDate = MyApplication.formatRegDate(regDate.toLong())

                    //set data
                    if(profileImage == "" || profileImage.isEmpty()){
                        binding.profileIv.setBackgroundResource(R.drawable.ic_baseline_person_24)
                    }else{
                        try{
                            Glide.with(this@ProfileEditActivity)
                                .load(profileImage)
                                .into(binding.profileIv)
                        } catch(e: Exception){
                            Log.d(TAG, "onDataChange: failed to load profileImage. Error: ${e.message}")
                        }
                    }

                    binding.nameEt.hint = name
                    binding.emailTv.text = email

                    if(sex.lowercase() == R.string.male.toString().lowercase()){
                        binding.sexIv.setImageResource(R.drawable.ic_man)
                    }else{
                        binding.sexIv.setImageResource(R.drawable.ic_woman)
                    }

                    binding.regDateTv.text = formattedRegDate

                    if(aboutMe == "" || aboutMe.isEmpty()){
                        binding.aboutMeEt.hint = "Describe yourself less than $maxLength characters  "
                    }else{
                        binding.aboutMeEt.hint = aboutMe
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun sportPickDialog() {
        Log.d(RegisterActivity.TAG, "sportPickDialog: displaying the selected sport from dialog")
        val sportsArr = arrayOfNulls<String>(sportsList.size)
        for(i in sportsList.indices){
            sportsArr[i] = sportsList[i].sport
        }
        // alertdialog for selection
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select your major sport")
            .setItems(sportsArr) {dialog, which ->
                selectedSport = sportsList[which].sport
                selectedSportId = sportsList[which].id
                binding.sportTv.text = selectedSport
            }
            .show()
    }

    private fun uploadImage(){

        // image path, name
        val filePathAndName = "ProfileImages/"+fbAuth.uid
        //storage
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->

                //get uri of uploaded image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)
                Log.d(TAG, "uploadImage: uploaded your profile image to firebase storage")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload profile image. Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "uploadImage: failed to upload your profile image to firebase storage. Error: ${e.message}")
            }

    }

    private fun updateProfile(uploadedImageUrl: String) {

        // add msg to DB
        val hashMap: HashMap<String, Any?> = HashMap()
        val name = binding.nameEt.text.toString().trim()
        val selectedSport = selectedSport
        val selectedSportId = selectedSportId
        val aboutMe = binding.aboutMeEt.text.toString()


        // check profile image
        if(imageUri != null){
            hashMap["profileImage"] = uploadedImageUrl
        // check if name is changed
        }else if(!name.isNullOrEmpty()){
            hashMap["name"] = name
        }else if(binding.levelRg.checkedRadioButtonId == -1){
            Toast.makeText(this, "Please check your level", Toast.LENGTH_SHORT).show()
        }else if(selectedSport.isNullOrEmpty()){
            Toast.makeText(this, "Please select your sport", Toast.LENGTH_SHORT).show()
        }else{

            hashMap["sport"] = selectedSport
            hashMap["sportId"] = selectedSportId
            hashMap["level"] = updatedLevel
            hashMap["aboutMe"] = aboutMe

            val ref = fbDbRef.getReference("Users")
            ref.child(fbAuth.uid!!)
                .updateChildren(hashMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Successfully uploaded your profile", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "updateProfile: Successfully updated your profile")
                    imageUri = null
                    startActivity(Intent(this@ProfileEditActivity, ProfileActivity::class.java))
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload your profile", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "updateProfile: Failed to update your profile")
                }
        }




    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }


    // to handle gallery intent result
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if(result.resultCode == RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                //set imageview
                binding.profileIv.setImageURI(imageUri)
            }else{
                Toast.makeText(this, "Cancelled to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    )
}