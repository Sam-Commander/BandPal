package com.sam.tippy

import userData.UserSearchData
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.sam.tippy.databinding.ActivitySearchBinding
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class Search : AppCompatActivity()
{
    // global variables
    private lateinit var binding: ActivitySearchBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var instrument: String
    private var mileRadius: Int = 0
    private lateinit var userAge: String
    private lateinit var searchedAge: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        /////////

        val userAuth = FirebaseAuth.getInstance().currentUser
        val userUID = userAuth?.uid
        pullUserData()


        // Search for instrument
        binding.confirmInstrButton.setOnClickListener {
            if (binding.tietInstrumentSearched.text.toString() == "" ||
                binding.tietInstrumentSearched.text.toString() == "vocals"){
                val toUIBlank = binding.tietInstrumentSearched.text.toString()
                upUIBlank(toUIBlank)
            }
            else{
                fetchInstrumentData(binding.tietInstrumentSearched.text.toString()).start()
            }
        }


        // Input number radius
        binding.confirmMileButton.setOnClickListener {
            binding.tvWithin.text = "within ${binding.tietNumberInputted.text.toString()} miles"
            binding.tvWithin.setTextColor(Color.parseColor("#4ca770"))
            mileRadius = binding.tietNumberInputted.text.toString().toInt()
        }


        // for Age SeekBar
        binding.seekBarSearchAge.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateAgeDesc(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}})


        // BIG SAVE BUTTON
        binding.searchButtonPage.setOnClickListener {

            database = FirebaseDatabase.getInstance().getReference("Users")
            if (userUID != null){
                database.child(userUID).get().addOnSuccessListener {
                    if(instrument.isNotEmpty()
                        && mileRadius.toString().isNotEmpty()
                        && searchedAge.isNotEmpty()){
                        saveSearchDetails()
                    }else{
                        Toast.makeText(this, "Missing information, check that all fields are filled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    // Saves search details
    private fun saveSearchDetails()
    {
        val search = UserSearchData(instrument, mileRadius, searchedAge)
        val uid = auth.currentUser?.uid

        if (uid != null) {
            database.child(uid).child("search").setValue(search).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Generating results...", Toast.LENGTH_SHORT).show()
                    if (auth.currentUser != null) {
                        val intent = Intent(this, SearchResults::class.java)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, "Failed to generate results", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // for age bracket
    @SuppressLint("SetTextI18n")
    private fun updateAgeDesc(ageBracket: Int)
    {
        val ageDescription: String

        if (userAge == "18-25"){
            ageDescription = when (ageBracket){
                in 0..7 -> "18-25"
                in 8..12 -> "26-35"
                in 13..17 -> "36-45"
                in 18..23 -> "46-59"
                else -> "60+"
            }
            binding.ageCat.setTextColor(Color.parseColor("#4ca770"))
            binding.ageCat.text = "who is not over $ageDescription years old"
            searchedAge = ageDescription
        }
        else if (userAge == "26-35"){
            ageDescription = when (ageBracket){
                in 0..12 -> "26-35"
                in 13..17 -> "36-45"
                in 18..23 -> "46-59"
                else -> "60+"
            }
            binding.ageCat.setTextColor(Color.parseColor("#4ca770"))
            binding.ageCat.text = "who is not over $ageDescription years old"
            searchedAge = ageDescription
        }
        else if (userAge == "36-45"){
            ageDescription = when (ageBracket){
                in 0..10 -> "36-45"
                in 11..20 -> "46-59"
                else -> "60+"
            }
            binding.ageCat.setTextColor(Color.parseColor("#4ca770"))
            binding.ageCat.text = "who is not over $ageDescription years old"
            searchedAge = ageDescription
        }
        else if (userAge == "46-59"){
            ageDescription = when (ageBracket){
                in 0..15 -> "46-59"
                else -> "60+"
            }
            binding.ageCat.setTextColor(Color.parseColor("#4ca770"))
            binding.ageCat.text = "who is not over $ageDescription years old"
            searchedAge = ageDescription
        }
        else if (userAge == "60+"){
            ageDescription = when (ageBracket){
                in 0..30 -> "60+"
                else -> "60+"
            }
            binding.ageCat.setTextColor(Color.parseColor("#4ca770"))
            binding.ageCat.text = "who is $ageDescription years old"
            searchedAge = ageDescription
        }
        binding.tvInfluences.setTextColor(Color.parseColor("#4ca770"))
    }


    // Fetches user data from DB
    private fun pullUserData()
    {
        val user = FirebaseAuth.getInstance().currentUser
        val userUID = user?.uid

        database = FirebaseDatabase.getInstance().getReference("Users")
        if (userUID != null) {
            database.child(userUID).get().addOnSuccessListener {

                if (it.exists())
                {
                    userAge = it.child("age").value.toString()

                    val influences: String =
                        "Influences:\n\n" + it.child("influences").value.toString().trim()
                            .replace("[", "")
                            .replace("]", "")
                            .replace(", ", "\n")
                    binding.searchInfluencesAct.text = influences

                    val genres: String =
                        "Genres:\n\n" + it.child("genres").value.toString().trim()
                            .replace("[", "")
                            .replace("]", "")
                            .replace(", ", "\n")
                    binding.searchGenresAct.text = genres
                }
            }
        }
    }


    // Connects to instrument API
    private fun fetchInstrumentData(instrumentName: String): Thread
    {
        return Thread {

            val url = URL("https://musicbrainz.org/ws/2/instrument?query=$instrumentName&limit=5&offset=0&fmt=json")
            val connection  = url.openConnection() as HttpsURLConnection

            if(connection.responseCode == 200) {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val request = Gson().fromJson(inputStreamReader, instrumentsAPI.Request::class.java)
                upUI2(request)

                // closing stream
                inputStreamReader.close()
                inputSystem.close()
            }
            else
            {
                Toast.makeText(this, "Connection cannot be made", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // to update UI with instrument data
    @SuppressLint("SetTextI18n")
    private fun upUI2(request: instrumentsAPI.Request)
    {
        runOnUiThread {
            kotlin.run {
                if (request.instruments.isEmpty()){
                    Toast.makeText(this, "No instrument found that fits that name", Toast.LENGTH_SHORT).show()
                }else{
                    binding.iAmLookingFor.text = "I am looking for a(n) \n${request.instruments.elementAt(0).name} player"
                    binding.iAmLookingFor.setTextColor(Color.parseColor("#4ca770"))
                    instrument = request.instruments.elementAt(0).name
                }
            }
        }
    }


    // to update UI with 'no instrument' data
    private fun upUIBlank(blankOrVocals: String)
    {
        if (blankOrVocals == ""){
            val messageOne = "I am looking for an\nanything player"
            binding.iAmLookingFor.text = messageOne
            instrument = "anything"
        }else{
            val messageTwo = "I am looking for a\nvocalist"
            binding.iAmLookingFor.text = messageTwo
            instrument = "vocals"
        }
        binding.iAmLookingFor.setTextColor(Color.parseColor("#4ca770"))
    }
}
