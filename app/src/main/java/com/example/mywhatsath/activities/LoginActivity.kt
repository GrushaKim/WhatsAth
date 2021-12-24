package com.example.mywhatsath.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.set
import androidx.core.text.toSpannable
import com.example.mywhatsath.R
import com.example.mywhatsath.databinding.ActivityLoginBinding
import com.example.mywhatsath.utils.LinearGradientSpan
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import es.dmoral.toasty.Toasty

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var fbAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var callbackManager: CallbackManager? = null

    private companion object{
        private const val RC_SIGN_IN = 200
        private const val GOOGLE_TAG = "GOOGLE_SIGN_IN_TAG"
        private const val FACEBOOK_TAG = "FACEBOOK_SIGN_IN_TAG"
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

        // get gradient linear for app name
        getGradientTextView()

        // init facebook callbackManager
        callbackManager = CallbackManager.Factory.create()

        // configure google signin
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
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

        // facebook signin click
        binding.facebookSignInBtn.setOnClickListener {
            Log.d(FACEBOOK_TAG, "onCreate: proceeding with signing in facebook")
            facebookSignInClient()
        }
    }

    private fun getGradientTextView() {
        val sloganText = binding.appNameTv.text.toString()
        val startColor = ContextCompat.getColor(this, R.color.start)
        val endColor = ContextCompat.getColor(this, R.color.end)
        val spannable = sloganText.toSpannable()
        spannable[0..sloganText.length] = LinearGradientSpan(sloganText, sloganText, startColor, endColor)
        binding.appNameTv.text = spannable
    }

    private fun facebookSignInClient() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        // extract access token to be handled
        LoginManager.getInstance()
            .registerCallback(callbackManager, object:FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult) {
                    handleFBToken(result?.accessToken)
                }
                override fun onCancel() {
                }
                override fun onError(error: FacebookException) {
                }
            })
    }

    private fun handleFBToken(token: AccessToken) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)

        fbAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    Log.d(FACEBOOK_TAG, "handleFBToken: successfully logged in with fb account")
                    //move to dashboard
                    startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                    finish()
                }else{
                    Log.d(FACEBOOK_TAG, "handleFBToken: failed to log in with fb account. Error: ${task.exception}")
                }
            }

    }

    // check if the user logged in with google or facebook
    private fun checkUser(){
        val fbUser = fbAuth.currentUser
        if(fbUser != null) {
            //move to dashboard
            startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
            finish()
        }
    }

    // login with email and pwd
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
                    Toasty.error(this, "Please verify your email&password", Toast.LENGTH_SHORT, true).show()
                }
            }
    }

    // handle other method to sign in
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // facebook, result related to intent is not needed
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        
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
                Log.d(
                    GOOGLE_TAG,
                    "firebaseAuthWithGoogleAccount: Logged in with fbauth based in google account"
                )
                val fbUser = fbAuth.currentUser
                val uid = fbUser!!.uid
                val email = fbUser!!.email

                //check if user is new or registered
                if (authResult.additionalUserInfo!!.isNewUser) {
                    Log.d(
                        GOOGLE_TAG,
                        "firebaseAuthWithGoogleAccount: New account created with $email"
                    )
                    Toasty.success(this, "Logged in with $email", Toast.LENGTH_SHORT, true).show()
                } else {
                    Log.d(
                        GOOGLE_TAG,
                        "firebaseAuthWithGoogleAccount: This $email is already being used"
                    )
                    Toasty.info(this, "This $email is already being used", Toast.LENGTH_SHORT, true).show()

                    //move to dashboard
                    startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.d(GOOGLE_TAG, "firebaseAuthWithGoogleAccount: Login failed. Error: ${e.message}")
                Toasty.error(this, "This $email is unavailable to log in. Error - ${e.message}", Toast.LENGTH_SHORT, true).show()
            }
            }
    }