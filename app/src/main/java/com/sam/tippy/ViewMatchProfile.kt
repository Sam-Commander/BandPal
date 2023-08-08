package com.sam.tippy

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sam.tippy.databinding.ActivityMatchViewProfileBinding
import com.squareup.picasso.Picasso
import java.util.*

class ViewMatchProfile : AppCompatActivity()
{
    // Global variables
    private lateinit var binding: ActivityMatchViewProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var dialog: Dialog

    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val matchUID = intent.getStringExtra("uid")
        val matchRadius = intent.getStringExtra("radius")

        showProgressBar()
        if (matchUID != null) {
            readMatchData(matchUID, matchRadius)
        }

        binding.chatButtonNew.setOnClickListener {
            val messageText = "Connecting will send a notification to this user " +
                    "and allow you to message them. Do you want to do this?"
            val builder = AlertDialog.Builder(this)

            builder.setTitle("Message Match?")
            builder.setMessage(messageText)
            builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                messageMatch(matchUID!!) // entire method body can be swapped out for just this line
            }
            builder.setNegativeButton("No") { _: DialogInterface, _: Int -> }
            builder.show()
        }

        // Trophy Toasts
        binding.matchTrophyFirstMatch.setOnClickListener {
            Toast.makeText(this, "First Match: Attained by making a first match", Toast.LENGTH_SHORT).show()
        }

        binding.matchTrophyInstr.setOnClickListener {
            Toast.makeText(this, "Maestro: Attained by playing five instruments", Toast.LENGTH_SHORT).show()
        }

        binding.matchTrophyGenre.setOnClickListener {
            Toast.makeText(this, "Genrehead: Attained by adding five genres to your profile", Toast.LENGTH_SHORT).show()
        }

        binding.matchTrophyGlobe.setOnClickListener {
            Toast.makeText(this, "Globetrotter: Attained from having searched for a BandPal in 3 or more locations", Toast.LENGTH_SHORT).show()
        }

        // Back to user's own profile
        binding.backToProfile.setOnClickListener {
            val intent = Intent(this, ViewProfile::class.java)
            startActivity(intent)
        }
    }


    // Connects user with match
    private fun messageMatch(matchUID: String)
    {
        val userAuth = FirebaseAuth.getInstance().currentUser
        val userUID = userAuth?.uid

        // For user's connectedMatches
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(userUID!!).get().addOnSuccessListener {
            if (it.exists()) {
                if (it.child("connectedMatches").exists()){
                    val connectedMatches: String =
                        it.child("connectedMatches").value.toString().trim()
                            .replace("[", "")
                            .replace("]", "")
                    val connectedMatches2 = connectedMatches.split(", ").toMutableList()

                    if(connectedMatches2.contains(matchUID)){
                        database.child(userUID).child("connectedMatches").setValue(connectedMatches2)
                    }else{
                        connectedMatches2.add(matchUID)
                        database.child(userUID).child("connectedMatches").setValue(connectedMatches2)
                    }
                }else{
                    val connectedMatches = mutableListOf<String>()
                    connectedMatches.add(matchUID)
                    database.child(userUID).child("connectedMatches").setValue(connectedMatches)
                }
            }
        }

        // For match's connectedMatches
        database.child(matchUID).get().addOnSuccessListener {
            if (it.child("connectedMatches").exists()){
                val matchConnectedMatches: String =
                    it.child("connectedMatches").value.toString().trim()
                        .replace("[", "")
                        .replace("]", "")
                val matchConnectedMatches2 = matchConnectedMatches.split(", ").toMutableList()

                if(matchConnectedMatches2.contains(userUID)){
                    database.child(matchUID).child("connectedMatches").setValue(matchConnectedMatches2)
                }else{
                    matchConnectedMatches2.add(userUID)
                    database.child(matchUID).child("connectedMatches").setValue(matchConnectedMatches2)
                }
            }else{
                val matchConnectedMatches = mutableListOf<String>()
                matchConnectedMatches.add(userUID)
                database.child(matchUID).child("connectedMatches").setValue(matchConnectedMatches)
            }
        }

        database.child(userUID).get().addOnSuccessListener {
            val userName = it.child("displayName").value.toString()
            val titleToMatch = "You've got a potential match"
            val messageToMatch = "$userName wants to connect with you"

            sendNotification(titleToMatch, messageToMatch, matchUID)

            // Navigates to user's messages page
            val intent = Intent(this, MusicianMessages::class.java)
            startActivity(intent)
        }
    }


    // To send notification
    private fun sendNotification(title: String, text: String, receiver_id: String)
    {
        val notification = Notification(text, title, receiver_id)

        FirebaseDatabase
            .getInstance()
            .getReference("Notification")
            .push()
            .setValue(notification)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Connection made, match notified", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Connection not made", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // For loading screen
    private fun showProgressBar() {
        dialog = Dialog(this@ViewMatchProfile)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


    // For loading screen
    private fun hideProgressBar() {
        dialog.dismiss()
    }


    // Gets Match data
    @SuppressLint("SetTextI18n")
    private fun readMatchData(userUID: String, matchRadius: String?) {

        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(userUID).get().addOnSuccessListener { it ->

            if (it.exists()) {

                // Setting the match's data to viewable
                binding.matchDisplayNameOutputNew.text = it.child("displayName").value.toString()
                binding.tietMatchOutputBioNew.setText(it.child("bio").value.toString())
                binding.matchDisplayAgeOutputNew.text = it.child("age").value.toString()
                binding.tietMatchOutputURLNew.setText(it.child("website").value.toString())

                if(matchRadius == "0"){
                    binding.matchMilesAwayNew.setTextColor(Color.parseColor("#FA5858"))
                    binding.matchMilesAwayNew.text = "You've matched with this user"
                }else{
                    ("$matchRadius miles away").also { binding.matchMilesAwayNew.text = it }
                }

                val imageURL = it.child("imageUrl").value.toString()
                Picasso.get().load(imageURL).into(binding.matchProfileImageOutputNew)

                if(it.child("connectedMatches").exists()){
                    val nothing = "nothing"
                }else{
                    binding.matchTrophyFirstMatch.visibility = View.GONE
                }

                val influences: String =
                    it.child("influences").value.toString().trim()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(", ", "\n")
                binding.tietMatchInfluences.setText(influences)

                val instruments: String =
                    it.child("instruments").value.toString().trim()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(", ", "\n")

                val instrForTrophy = instruments.split("\n").toTypedArray()
                if (instrForTrophy.size < 5){
                    binding.matchTrophyInstr.visibility = View.GONE
                }

                val str = instruments.split(" ")
                    .joinToString(" ") { itNow ->
                        itNow.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                    }.split("\n").joinToString("\n") { itNow2 ->
                        itNow2.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                    }
                binding.tietMatchInstruments.setText(str)

                val genres: String =
                    it.child("genres").value.toString().trim()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(", ", "\n")
                binding.tietMatchGenres.setText(genres)

                val genForTrophy = genres.split("\n").toTypedArray()
                if (genForTrophy.size < 5){
                    binding.matchTrophyGenre.visibility = View.GONE
                }

                binding.matchTrophyGlobe.visibility = View.GONE

                // Making everything uneditable
                binding.tietMatchOutputURLNew.isEnabled = false
                binding.tietMatchOutputBioNew.isEnabled = false
                binding.tietMatchInfluences.isEnabled = false
                binding.tietMatchInstruments.isEnabled = false
                binding.tietMatchGenres.isEnabled = false

            } else {
                Toast.makeText(this, "No user present", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "No user present", Toast.LENGTH_SHORT).show()
        }
        hideProgressBar()
    }
}
