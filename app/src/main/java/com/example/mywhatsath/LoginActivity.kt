package com.example.mywhatsath

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.mywhatsath.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private companion object{
        private const val RC_SIGN_IN = 100
        private const val GOOGLE_TAG = "GOOGLE_SIGN_IN_TAG"
        private const val TAG = "LOGIN_TAG"
    }

    private var email = ""
    private var pwd = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init auth
        fbAuth = FirebaseAuth.getInstance()

        // configure google signin
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // reset password click
        binding.forgotPwdTv.setOnClickListener{
            val intent = Intent(this@LoginActivity, ResetPwdActivity::class.java)
            startActivity(intent)
        }

        // signup click
        binding.signupTv.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        // signin with email click
        binding.loginBtn.setOnClickListener {
            login()
        }

        // google signin click
        binding.googleSignInBtn.setOnClickListener {
            Log.d(GOOGLE_TAG, "onCreate: proceeding with signing in google")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    private fun login() {
        email = binding.emailEt.text.toString().trim()
        pwd = binding.pwdEt.text.toString().trim()

        fbAuth?.signInWithEmailAndPassword(email, pwd)
            ?.addOnCompleteListener { task ->
                Log.d(TAG, "login: successfully logged in with email")
                if(task.isSuccessful){
                    val intent = Intent(this@LoginActivity, DashboardUserActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Log.d(TAG, "login: failed to log in with your email")
                    Toast.makeText(this, "Please verify your email&password.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        //result returned from intent launching from GoogleSignInApi.getSignInIntent
        if (requestCode == RC_SIGN_IN){
            Log.d(GOOGLE_TAG, "onActivityResult: Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                // if it is successful, integrate into firebase auth
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogleAccount(account)
            }catch(e: Exception){
                // when it fails
                Log.d(GOOGLE_TAG, "onActivityResult: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        Log.d(GOOGLE_TAG, "firebaseAuthWithGoogleAccount: starts firebase auth with google account")

        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
        fbAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                Log.d(GOOGLE_TAG, "firebaseAuthWithGoogleAccount: Logged in with fbauth based in google account")
                val fbUser = fbAuth.currentUser
                val uid = fbUser!!.uid
                val email = fbUser!!.email
            }

    }
}