package com.example.mywhatsath

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.mywhatsath.databinding.ActivityRegisterBinding
import com.example.mywhatsath.models.ModelSport
import com.example.mywhatsath.utils.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.properties.Delegates

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    // firebase vars
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var fbDbRef: FirebaseDatabase
    // sports select dialog
    private lateinit var sportsList: ArrayList<ModelSport>
    private var selectedSportId = ""
    private var selectedSport = ""
    // register info vars
    private var name = ""
    private var email = ""
    private var pwd = ""
    private var gender = ""
    private var sport = ""
    private var level = ""
    private var isAtLeast8: Boolean = false
    private var hasNumber: Boolean = false
    private var isRegistered: Boolean = true

    companion object{
        const val TAG = "REGISTER_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init
        fbAuth = FirebaseAuth.getInstance()
        fbDbRef = FirebaseDatabase.getInstance()

        // load sport categories
        loadSports()


        // email check button click
        binding.chkEmailBtn.setOnClickListener {
            checkEmail()
        }

        // pwd input
        binding.pwdEt.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.validatePwdLl.visibility = View.VISIBLE
                checkPwdRegulations(cs!!)
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        // confirm pwd input
        binding.confirmPwdEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(cs: Editable?) {
                var confirmPwd = cs.toString().trim()
                checkConfirmPwd(confirmPwd!!)
            }
        })

        // gender radio group click
        binding.genderRg.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId){
                R.id.femaleRb -> gender = "Female"
                R.id.maleRb -> gender = "Male"
            }
        }

        // select major sport
        binding.sportTv.setOnClickListener {
            sportPickDialog()
        }

        // level radio group click
        binding.levelRg.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId){
                R.id.amateurRb -> level = resources.getStringArray(R.array.levels)[0].toString()
                R.id.semiProRb -> level = resources.getStringArray(R.array.levels)[1].toString()
                R.id.proRb -> level = resources.getStringArray(R.array.levels)[2].toString()
            }
        }

        // signup button click
        binding.signupBtn.setOnClickListener {
            validateData()
        }
    }

    private fun sportPickDialog() {
        Log.d(TAG, "sportPickDialog: displaying the selected sport from dialog")
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

    private fun checkEmail() {
        email = binding.emailEt.text.toString().trim()
        fbAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->

                // set email check textview visible and changes its text and color depending on boolean
                if(task.result?.signInMethods?.size == 0){
                    isRegistered = false
                    Log.d(TAG, "onCreate: email doesn't exist. email is $email. isRegistered is $isRegistered")

                    binding.chkEmailFrameLl.visibility = View.VISIBLE
                    binding.chkEmailFrameOne.setBackgroundResource(R.drawable.ic_baseline_check_circle_outline_24)
                    binding.chkEmailFrameTv.text = "This email is available"
                    binding.chkEmailFrameTv.setTextColor(Color.parseColor("#000000"))
                }else{
                    isRegistered = true
                    Log.d(TAG, "onCreate: email is already being used. email is $email. isRegistered is $isRegistered")

                    binding.chkEmailFrameLl.visibility = View.VISIBLE
                    binding.chkEmailFrameOne.setBackgroundResource(R.drawable.ic_baseline_error_outline_24)
                    binding.chkEmailFrameTv.text = "This email address is already being used"
                    binding.chkEmailFrameTv.setTextColor(Color.parseColor("#E91E63"))
                }
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "onCreate: failed to check email. Error: ${e.message}")
            }
    }

    private fun checkConfirmPwd(confirmPwd: String) {
        pwd = binding.pwdEt.text.toString().trim()

        if(confirmPwd == pwd){
            binding.confirmPwdErrLl.visibility = View.GONE
        }else{
            binding.confirmPwdErrLl.visibility = View.VISIBLE
        }
    }

    private fun checkPwdRegulations(cs: CharSequence) {

        if(cs.length >= 8){
            isAtLeast8 = true
            binding.pwdFrameOne.setCardBackgroundColor(Color.parseColor("#ffce63"))
        }else{
            isAtLeast8 = false
            binding.pwdFrameOne.setCardBackgroundColor(Color.parseColor("#dcdcdc"))
        }
        if(cs.contains(Regex("[0-9]"))){
            hasNumber = true
            binding.pwdFrameTwo.setCardBackgroundColor(Color.parseColor("#ffce63"))
        }else{
            hasNumber = false
            binding.pwdFrameTwo.setCardBackgroundColor(Color.parseColor("#dcdcdc"))
        }
    }

    private fun validateData() {
        // 1. get data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        pwd = binding.pwdEt.text.toString().trim()
        sport = binding.sportTv.toString()
        val confirmPwd = binding.confirmPwdEt.text.toString().trim()

        // 2. validate
        if(name.isNullOrEmpty()){
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show()
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Enter valid email address", Toast.LENGTH_SHORT).show()
        }else if(isRegistered){
            Toast.makeText(this, "Please enter another email address", Toast.LENGTH_SHORT).show()
        }else if(pwd.isNullOrEmpty()){
            Toast.makeText(this, "Enter your password", Toast.LENGTH_SHORT).show()
        }else if(confirmPwd.isNullOrEmpty()){
            Toast.makeText(this, "Confirm your password", Toast.LENGTH_SHORT).show()
        }else if(pwd != confirmPwd){
            binding.confirmPwdErrLl.visibility = View.VISIBLE
        }else if(binding.genderRg.checkedRadioButtonId == -1){
            Toast.makeText(this, "Select your gender", Toast.LENGTH_SHORT).show()
        }else if(binding.levelRg.checkedRadioButtonId == -1){
            Toast.makeText(this, "Select your level", Toast.LENGTH_SHORT).show()
        }else if(sport.isNullOrEmpty()){
            Toast.makeText(this, "Select your major sport", Toast.LENGTH_SHORT).show()
        }else{
            // 3. register an user with the validated info
            registerUser(name, email, pwd, gender, level, selectedSport, selectedSportId)
        }
    }

    private fun registerUser(
        name: String, email: String, pwd: String, gender: String, level: String, sport: String, sportId: String){
        fbAuth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener {

                    //set uid and info for each user
                    var user: FirebaseUser? = fbAuth.currentUser
                    var uid: String = user!!.uid
                    var timestamp = System.currentTimeMillis()

                    var hashMap: HashMap<String, Any> = HashMap()
                    hashMap["uid"] = uid
                    hashMap["name"] = name
                    hashMap["email"] = email
                    hashMap["gender"] = gender
                    hashMap["sport"] = sport
                    hashMap["sportId"] = sportId
                    hashMap["level"] = level
                    hashMap["regDate"] = timestamp
                    hashMap["role"] = 1
                    hashMap["profileImage"] = ""
                    hashMap["aboutMe"] = ""
                    hashMap["heartsCnt"] = 0


                    // update to DB
                    var fbDbRef = FirebaseDatabase.getInstance().getReference("Users")
                    fbDbRef.child(uid)
                        .setValue(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "registerUser: successfully registered user")
                            Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show()
                            //move to login
                            var intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.d(TAG, "registerUser: failed to register user. Error: ${e.message}")
                            Toast.makeText(this, "Failed to register", Toast.LENGTH_SHORT).show()
                        }

                }
            .addOnFailureListener { e ->
                Log.d(TAG, "registerUser: failed to createUserWithEmail. Error: ${e.message}")
            }


                }

}