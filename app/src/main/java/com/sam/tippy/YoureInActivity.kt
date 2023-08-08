package com.sam.tippy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sam.tippy.databinding.ActivityYoureInBinding

// for login / logout
var letsCheck: Boolean = false

class YoureInActivity : AppCompatActivity() {

    // for binding
    private lateinit var binding: ActivityYoureInBinding
    lateinit var mGoogleSignInClient: GoogleSignInClient

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This allows the layout of widgets in the view
        binding = ActivityYoureInBinding.inflate(layoutInflater)
        setContentView(binding.root)


        /////////////////////


        // for Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("~insert google token~") // !!?
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)

        binding.signOutButton.setOnClickListener {
            letsCheck = true
            mGoogleSignInClient.signOut().addOnCompleteListener{
                val intent = Intent(applicationContext, Login::class.java)

                clearToken(FirebaseAuth.getInstance().currentUser!!.uid)
                startActivity(intent)
                finish()
            }
        }


        // When musician option selected, 1
        binding.musicianButton.setOnClickListener {
            val musician = "Musician"
            openEPM(musician)
        }

        // When band option selected, 2
        binding.bandButton.setOnClickListener {
            val band = "Band"
            openEPM(band)
        }
    }


    // Deletes current user's token on signout
    private fun clearToken(userUID: String){
        FirebaseDatabase
            .getInstance()
            .getReference("tokens")
            .child(userUID)
            .removeValue()
    }


    // When musician option selected, 2
    private fun openEPM(typeOfProfile: String) {
        val intent = Intent(this, EditProfile::class.java)
        intent.putExtra("typeOfProfile", typeOfProfile)
        startActivity(intent)
    }
}