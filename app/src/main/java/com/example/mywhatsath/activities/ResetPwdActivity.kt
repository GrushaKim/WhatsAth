package com.example.mywhatsath.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mywhatsath.databinding.ActivityResetPwdBinding
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty

class ResetPwdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPwdBinding

    // firebase vars
    private lateinit var fbAuth: FirebaseAuth

    private var email = ""

    companion object{
        const val TAG = "RESET_PWD_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPwdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth
        fbAuth = FirebaseAuth.getInstance()

        // send new password click
        binding.sendPwdBtn.setOnClickListener {
            checkEmail()
        }
    }

    private fun checkEmail() {
        email = binding.emailEt.text.toString().trim()
        fbAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if(task.result?.signInMethods?.size == 0){
                    Log.d(TAG, "checkEmail: unregistered email")
                    binding.invalidTv.visibility = View.VISIBLE
                }else {
                    Log.d(TAG, "checkEmail: this email exists")
                    binding.invalidTv.visibility = View.INVISIBLE
                    sendPwd(email)
                }
            }
    }

    private fun sendPwd(email: String) {
        fbAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Log.d(TAG, "sendPwd: email sent with new pwd")
                    Toasty.info(this, "Please check a new password sent to your email", Toast.LENGTH_SHORT, true).show()
                }
            }
    }


}