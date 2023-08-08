package com.sam.tippy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.sam.tippy.databinding.ActivityMainBinding

class Login : AppCompatActivity()
{
    // for Facebook
    var callbackManager = CallbackManager.Factory.create()
    var auth2 = FirebaseAuth.getInstance() // for facebook login
    var TAG=""

    // for Google
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth // for google login
    private lateinit var binding: ActivityMainBinding

    // for normal login
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // for general / Google
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        // configuring Google login
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("~insert google token~") // !!?
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // for normal login
        firebaseAuth = FirebaseAuth.getInstance()

        binding.bLoginNormal.setOnClickListener {
            val email = binding.tietInputEmail.text.toString()
            val pass = binding.tietInputPassword.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty()){

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if(it.isSuccessful){
                        retrieveAndStoreToken()
                        signInNormal()
                    }else{
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this, "Empty fields aren't allowed", Toast.LENGTH_SHORT).show()
            }
        }

        // for normal signup
        binding.bSignup.setOnClickListener {
            openSignup()
        }

        // for Goggle login
        binding.googleSignIn.setOnClickListener{
            signIn()
        }

        // for Facebook
        var loginButton = findViewById<LoginButton>(R.id.login_button)
        loginButton.setOnClickListener {
            if(userLoggedIn()){
                auth2.signOut()
            }else{
                LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
            }
        }

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                // ...
            }
        })
        // ...
    }


    // To sign in with email in a normal manner
    private fun signInNormal() {

        val user = FirebaseAuth.getInstance().currentUser
        val userUID = user?.uid

        database = FirebaseDatabase.getInstance().getReference("Users")
        if (userUID != null) {
            database.child(userUID).get().addOnSuccessListener {
                if (it.child("displayName").exists()){
                    val intent = Intent(this, ViewProfile::class.java)
                    startActivity(intent)
                }
            }
        }else{
            val intent = Intent(this, YoureInActivity::class.java)
            startActivity(intent)
        }
    }


    // To get token
    private fun retrieveAndStoreToken()
    {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val token = task.result

                    val userId = FirebaseAuth.getInstance().currentUser!!.uid

                    FirebaseDatabase.getInstance()
                        .getReference("tokens")
                        .child(userId)
                        .setValue(token)
                }
            }
    }


    private fun openSignup() {
        val intent = Intent(this, Signup::class.java)
        startActivity(intent)
    }

    private fun userLoggedIn(): Boolean {
        if(auth2.currentUser != null && !AccessToken.getCurrentAccessToken()!!.isExpired ){
            return true
        }
        return false
    }

    // for Google
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // for Google and Facebook
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(applicationContext, YoureInActivity::class.java)
            intent.putExtra(EXTRA_NAME, user.displayName)
            startActivity(intent)
        }
    }

    companion object {
        const val RC_SIGN_IN = 1001
        const val EXTRA_NAME = "EXTRA NAME"
    }


    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    
    // onStart
    override fun onStart() {
        super.onStart()

        val user = FirebaseAuth.getInstance().currentUser
        val userUID = user?.uid

        database = FirebaseDatabase.getInstance().getReference("Users")
        if (userUID != null && !letsCheck) {
            database.child(userUID).get().addOnSuccessListener {
                if (it.child("displayName").exists()){
                    val intent = Intent(this, ViewProfile::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}