package com.sam.tippy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.sam.tippy.databinding.ActivitySignupBinding

class Signup : AppCompatActivity() {

    // for general view setup
    private lateinit var binding:ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    // On create
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // for general view setup
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.bSignUpNow.setOnClickListener {
            val email = binding.tietInputEmailSignup.text.toString()
            val pass = binding.tietInputPasswordSignup.text.toString()
            val confirmPass = binding.tietCheckPassword.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()){ // will need to be changed to allow multiple profile types
                if(pass == confirmPass){
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if(it.isSuccessful){
                            retrieveAndStoreToken()
                            toMusicianProfile()
                        }else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Empty fields aren't allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // To get token for messages
    private fun retrieveAndStoreToken()
    {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if  (task.isSuccessful){
                    val token = task.result

                    val userId = FirebaseAuth.getInstance().currentUser!!.uid

                    FirebaseDatabase.getInstance()
                        .getReference("tokens")
                        .child(userId)
                        .setValue(token)
                }
            }
    }


    // opens first view inside app
    private fun toMusicianProfile() {
        val intent = Intent(this, YoureInActivity::class.java)
        startActivity(intent)
    }
}