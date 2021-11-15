package com.example.mywhatsath

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.mywhatsath.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var fbAuth: FirebaseAuth

    private var email = ""
    private var pwd = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth
        fbAuth = FirebaseAuth.getInstance()

        // reset password click
        binding.forgotPwdTv.setOnClickListener{
            val intent = Intent(this@LoginActivity, ResetPwdActivity::class.java)
            startActivity(intent)
        }

        // sign up with email click
        binding.signupTv.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        // sign in with email click
        binding.loginBtn.setOnClickListener {
            login()
        }
    }

    private fun login() {
        email = binding.emailEt.text.toString().trim()
        pwd = binding.pwdEt.text.toString().trim()

        fbAuth?.signInWithEmailAndPassword(email, pwd)
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val intent = Intent(this@LoginActivity, DashboardUserActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this, "Please verify your email&password.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}