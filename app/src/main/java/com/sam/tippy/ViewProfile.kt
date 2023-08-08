package com.sam.tippy

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sam.tippy.databinding.ActivityMusicianViewProfileBinding
import com.squareup.picasso.Picasso
import java.util.*

class ViewProfile : AppCompatActivity() {
    private lateinit var binding: ActivityMusicianViewProfileBinding
    private lateinit var database: DatabaseReference
    private var userPType: String = ""

    // On create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicianViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //////////////

        val user = FirebaseAuth.getInstance().currentUser
        val userUID = user?.uid

        if (userUID != null) {
            readUserData(userUID)
        }

        // Navigate to search for a bandpal
        binding.imageView.setOnClickListener {
            val intent = Intent(this, Search::class.java)
            startActivity(intent)
        }

        // Navigate to musician messages
        binding.imageView2.setOnClickListener {
            val intent = Intent(this, MusicianMessages::class.java)
            startActivity(intent)
        }

        // Navigate to edit profile musician
        binding.imageView3.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            intent.putExtra("typeOfProfile", userPType)
            startActivity(intent)
        }

        // Handles the 'what's this?' button
        binding.bWhatThis.setOnClickListener {
            val trophyText = "Trophies are rewards for building a full profile, " +
                    "making matches and connecting with your local music community. " +
                    "The more trophies, the easier it is for fellow users to find you" +
                    " and build a thriving local scene.\n\nTap each trophy in your case " +
                    "to see why it was given, and explore other user's profiles to find out " +
                    "how to attain more."

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Trophy Cabinet")
            builder.setMessage(trophyText)
            builder.setNegativeButton("Done") { _: DialogInterface, _: Int -> }
            builder.show()
        }

        // Trophy Toasts
        binding.trophyFirstMatch.setOnClickListener {
            Toast.makeText(this, "First Match: Attained by making a first match", Toast.LENGTH_SHORT).show()
        }

        binding.trophyInstr.setOnClickListener {
            Toast.makeText(this, "Maestro: Attained by playing five instruments", Toast.LENGTH_SHORT).show()
        }

        binding.trophyGenre.setOnClickListener {
            Toast.makeText(this, "Genrehead: Attained by adding five genres to your profile", Toast.LENGTH_SHORT).show()
        }

        binding.trophyGlobe.setOnClickListener {
            Toast.makeText(this, "Globetrotter: Attained from having searched for a BandPal in 3 or more locations", Toast.LENGTH_SHORT).show()
        }
    }


    // Gets current user data
    private fun readUserData(userUID: String) {

        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(userUID).get().addOnSuccessListener { it ->

            if (it.exists()) {

                // Setting the user's data to viewable
                binding.displayNameOutput.text = it.child("displayName").value.toString()
                binding.tietOutputBio.setText(it.child("bio").value.toString())
                binding.displayAgeOutput.text = it.child("age").value.toString()
                binding.tietOutputURL.setText(it.child("website").value.toString())
                binding.profileType.text = it.child("profileType").value.toString()

                userPType = it.child("profileType").value.toString()

                if(it.child("connectedMatches").exists()){
                    val nothing = "nothing"
                }else{
                    binding.trophyFirstMatch.visibility = View.GONE
                }

                val imageURL = it.child("imageUrl").value.toString()
                Picasso.get().load(imageURL).into(binding.userProfileImageOutput)

                val influences: String =
                    it.child("influences").value.toString().trim()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(", ", "\n")
                binding.tietInfluencesNew.setText(influences)

                val instruments: String =
                    it.child("instruments").value.toString().trim()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(", ", "\n")

                val instrForTrophy = instruments.split("\n").toTypedArray()
                if (instrForTrophy.size < 5){
                    binding.trophyInstr.visibility = View.GONE
                }

                val str = instruments.split(" ")
                    .joinToString(" ") {
                        it.capitalize(Locale.ROOT)
                    }.split("\n").joinToString("\n") {
                        it.capitalize(Locale.ROOT)
                    }
                binding.tietInstrumentsNew.setText(str)

                val genres: String =
                    it.child("genres").value.toString().trim()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(", ", "\n")
                binding.tietGenresNew.setText(genres)

                val genForTrophy = genres.split("\n").toTypedArray()
                if (genForTrophy.size < 5){
                    binding.trophyGenre.visibility = View.GONE
                }

                binding.trophyGlobe.visibility = View.GONE

                // Making everything uneditable
                binding.tietOutputURL.isEnabled = false
                binding.tietOutputBio.isEnabled = false
                binding.tietInfluencesNew.isEnabled = false
                binding.tietInstrumentsNew.isEnabled = false
                binding.tietGenresNew.isEnabled = false

            } else {
                Toast.makeText(this, "No user present", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "No user present", Toast.LENGTH_SHORT).show()
        }
    }
}
