package com.sam.tippy

import influencesAPI.Request
import userData.User
import usernames.UserUIDs
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.Window
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.sam.tippy.databinding.ActivityEditProfileMusicianBinding
import com.squareup.picasso.Picasso
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

class EditProfile : AppCompatActivity()
{
    // global variables
    private lateinit var binding: ActivityEditProfileMusicianBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg: Uri
    private lateinit var databaseReference: DatabaseReference // for the realtime database
    private lateinit var dialog: Dialog
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var userLat: Double = 0.0
    private var userLong: Double = 0.0
    private var genresForDialog: String = ""
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private var profilePapChanged: Boolean = false


    // On create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileMusicianBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        ////////////////

        val userProfileType = intent.getStringExtra("typeOfProfile")
        val userAuth = FirebaseAuth.getInstance().currentUser
        val userUID = userAuth?.uid

        if (userUID != null) {
            readUserDataEdit(userUID)
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        genreDataToString()

        // for profile image
        binding.userProfileImage.setOnClickListener {
            profilePapChanged = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }


        // Gets location
        binding.bLocationGetter.setOnClickListener {
            getLocation()
        }


        // for Age SeekBar
        binding.seekBarAge.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateAgeDescription(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}})


        // Search for artist
        binding.searchForArtistButton.setOnClickListener {
            fetchLastFMData(binding.tietInfluences.text.toString()).start()
        }


        // Search for instrument
        binding.searchForInstrument.setOnClickListener {
            if(binding.tietInstruments.text.toString() != "vocals"){
                fetchInstrumentData(binding.tietInstruments.text.toString()).start()
            }else{
                binding.radioOneInstr.text = binding.tietInstruments.text.toString()
                binding.radioTwoInstr.text = ""
                binding.radioThreeInstr.text = ""
            }
        }


        // Dialog for genre list
        binding.bGenreList.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogGenres = genresForDialog.replace(", ", "\n")
            builder.setTitle("Genres")
            builder.setMessage(dialogGenres)
            builder.setNegativeButton("Done") { _: DialogInterface, _: Int -> }
            builder.show()
        }


        // Search for genre
        binding.searchForGenre.setOnClickListener {
            fetchGenreData(binding.tietGenres.text.toString()
                .split(" ").joinToString(" ") { it.capitalize(Locale.ROOT) })
        }


        // Commit button LastFM
        binding.commitButton.setOnClickListener {
            val selectedID: Int = binding.radioGroup.checkedRadioButtonId
            val rb: RadioButton = findViewById(selectedID)
            val artistText: String = rb.text.toString()

            if (binding.artist1.text.toString().isEmpty()){
                binding.artist1.text = artistText
            } else if (binding.artist2.text.toString().isEmpty()){
                binding.artist2.text = artistText
            } else if (binding.artist3.text.toString().isEmpty()){
                binding.artist3.text = artistText
            } else if (binding.artist4.text.toString().isEmpty()){
                binding.artist4.text = artistText
            } else if (binding.artist5.text.toString().isEmpty()){
                binding.artist5.text = artistText
            } else {
                Toast.makeText(this, "Max number of influences is 5", Toast.LENGTH_SHORT).show()
            }
            binding.radioOne.text = ""
            binding.radioTwo.text = ""
            binding.radioThree.text = ""
        }


        // X buttons for artists 1-5
        binding.xButtonArtist1.setOnClickListener {
            binding.artist1.text = ""
        }
        binding.xButtonArtist2.setOnClickListener {
            binding.artist2.text = ""
        }
        binding.xButtonArtist3.setOnClickListener {
            binding.artist3.text = ""
        }
        binding.xButtonArtist4.setOnClickListener {
            binding.artist4.text = ""
        }
        binding.xButtonArtist5.setOnClickListener {
            binding.artist5.text = ""
        }


        // Commit button instruments
        binding.commitButtonInstr.setOnClickListener {
            val selectedID: Int = binding.radioGroupInstrument.checkedRadioButtonId

            val rb: RadioButton = findViewById(selectedID)

            val instrText: String = rb.text.toString()

            if (binding.instr1.text.toString().isEmpty()){
                binding.instr1.text = instrText
            } else if (binding.instr2.text.toString().isEmpty()){
                binding.instr2.text = instrText
            } else if (binding.instr3.text.toString().isEmpty()){
                binding.instr3.text = instrText
            } else if (binding.instr4.text.toString().isEmpty()){
                binding.instr4.text = instrText
            } else if (binding.instr5.text.toString().isEmpty()){
                binding.instr5.text = instrText
            } else {
                Toast.makeText(this, "Max number of instruments is 5", Toast.LENGTH_SHORT).show()
            }
            binding.radioOneInstr.text = ""
            binding.radioTwoInstr.text = ""
            binding.radioThreeInstr.text = ""
        }


        // X buttons for instruments 1-5
        binding.xButtonInstr1.setOnClickListener {
            binding.instr1.text = ""
        }
        binding.xButtonInstr2.setOnClickListener {
            binding.instr2.text = ""
        }
        binding.xButtonInstr3.setOnClickListener {
            binding.instr3.text = ""
        }
        binding.xButtonInstr4.setOnClickListener {
            binding.instr4.text = ""
        }
        binding.xButtonInstr5.setOnClickListener {
            binding.instr5.text = ""
        }


        // X buttons for genres 1-5
        binding.xButtonGenre1.setOnClickListener {
            binding.genre1.text = ""
        }
        binding.xButtonGenre2.setOnClickListener {
            binding.genre2.text = ""
        }
        binding.xButtonGenre3.setOnClickListener {
            binding.genre3.text = ""
        }
        binding.xButtonGenre4.setOnClickListener {
            binding.genre4.text = ""
        }
        binding.xButtonGenre5.setOnClickListener {
            binding.genre5.text = ""
        }


        // THE BIG SAVE BUTTON
        binding.epmSaveButton.setOnClickListener {

            val databaseRef = FirebaseDatabase.getInstance().getReference("Users")
            if (userUID != null) {
                databaseRef.child(userUID).get().addOnSuccessListener {

                    // Basically if the user's UID is a 'child' of "Users" then do this
                    if (it.exists()) {
                        // Setting the user's data to viewable
                        if (it.child("displayName").exists() // the critical line to tell if user data exists already
                            && binding.tietInputDisplayName.text.toString().isNotEmpty()
                            && binding.tvAgeRange.text.toString().isNotEmpty()

                            // add user lat and long here

                            && binding.artist5.text.toString().isNotEmpty()
                            && binding.instr1.text.toString().isNotEmpty()
                            && binding.genre3.text.toString().isNotEmpty()
                            && selectedImg.toString().isNotEmpty()) {

                            showProgressBar()

                            if(profilePapChanged){
                                updateUserDetails(userProfileType!!)
                            }else if(!profilePapChanged){
                                updateUserDetailsNoPhoto(userProfileType!!)
                            }

                        }
                    } else if (binding.tietInputDisplayName.text.toString().isNotEmpty()
                        && binding.tvAgeRange.text.toString().isNotEmpty()

                        // add user lat and long here

                        && binding.artist5.text.toString().isNotEmpty()
                        && binding.instr1.text.toString().isNotEmpty()
                        && binding.genre3.text.toString().isNotEmpty()
                        && selectedImg.toString().isNotEmpty()) {

                        showProgressBar()
                        saveUserDetails(userProfileType!!)
                    } else {
                        Toast.makeText(this, "Missing information, check that all fields are filled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        // for Google and signout
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("~insert google token~") // !!?
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)
        binding.signOutButtonMus.setOnClickListener {
            letsCheck = true
            mGoogleSignInClient.signOut().addOnCompleteListener{
                val intent = Intent(applicationContext, Login::class.java)

                // need to somehow cancel current user
                clearToken(userUID!!)
                startActivity(intent)
                finish()
            }
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


    // Updates user data if data already exists in database
    private fun updateUserDetails(userProfileType: String)
    {
        val reference = storage.reference.child("Profile").child(Date().time.toString())

        reference.putFile(selectedImg).addOnCompleteListener {
            if (it.isSuccessful) {
                reference.downloadUrl.addOnSuccessListener { task ->

                    val uid = auth.currentUser?.uid // gets the UID of the current user

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users")

                    // Values for user details
                    val imgUrl = task.toString()
                    val displayName = binding.tietInputDisplayName.text.toString()
                    val bio = binding.tietInputBio.text.toString()
                    val age = binding.tvAgeRange.text.toString()
                    val website = binding.tietInputURL.text.toString()
                    val influences = mutableListOf<String>()
                    val instruments = mutableListOf<String>()
                    val genres = mutableListOf<String>()
                    influences.add(binding.artist1.text.toString())
                    influences.add(binding.artist2.text.toString())
                    influences.add(binding.artist3.text.toString())
                    influences.add(binding.artist4.text.toString())
                    influences.add(binding.artist5.text.toString()) // change into loop?

                    instruments.add(binding.instr1.text.toString())
                    if (binding.instr2.text.toString().isNotEmpty()) {
                        instruments.add(binding.instr2.text.toString())
                    }
                    if (binding.instr3.text.toString().isNotEmpty()) {
                        instruments.add(binding.instr3.text.toString())
                    }
                    if (binding.instr4.text.toString().isNotEmpty()) {
                        instruments.add(binding.instr4.text.toString())
                    }
                    if (binding.instr5.text.toString().isNotEmpty()) {
                        instruments.add(binding.instr5.text.toString())
                    }

                    genres.add(binding.genre1.text.toString())
                    genres.add(binding.genre2.text.toString())
                    genres.add(binding.genre3.text.toString())
                    if (binding.genre4.text.toString().isNotEmpty()) {
                        genres.add(binding.genre4.text.toString())
                    }
                    if (binding.genre5.text.toString().isNotEmpty()) {
                        genres.add(binding.genre5.text.toString())
                    }

                    val editedUser = mapOf(
                        "profileType" to userProfileType,
                        "imageUrl" to imgUrl,
                        "displayName" to displayName,
                        "age" to age,
                        "bio" to bio,
                        "website" to website,
                        "latitude" to userLat,
                        "longitude" to userLong,
                        "influences" to influences,
                        "instruments" to instruments,
                        "genres" to genres
                    )

                    if (uid != null) {
                        databaseReference.child(uid).updateChildren(editedUser)
                            .addOnSuccessListener {
                                if (Patterns.WEB_URL.matcher(binding.tietInputURL.text.toString())
                                        .matches()
                                    && displayName.isNotEmpty()
                                    && age.isNotEmpty()
                                ) {
                                    hideProgressBar()
                                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT)
                                        .show()
                                    if (auth.currentUser != null) {
                                        val intent = Intent(this, ViewProfile::class.java)
                                        startActivity(intent)
                                    }
                                } else {
                                    hideProgressBar()
                                    Toast.makeText(
                                        this,
                                        "Failed to update profile",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            }
        }
    }


    // Updates user data if data already exists in database (but with no photo)
    private fun updateUserDetailsNoPhoto(userProfileType: String)
    {
        val uid = auth.currentUser?.uid // gets the UID of the current user

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        // Values for user details
        val displayName = binding.tietInputDisplayName.text.toString()
        val bio = binding.tietInputBio.text.toString()
        val age = binding.tvAgeRange.text.toString()
        val website = binding.tietInputURL.text.toString()
        val influences = mutableListOf<String>()
        val instruments = mutableListOf<String>()
        val genres = mutableListOf<String>()
        influences.add(binding.artist1.text.toString())
        influences.add(binding.artist2.text.toString())
        influences.add(binding.artist3.text.toString())
        influences.add(binding.artist4.text.toString())
        influences.add(binding.artist5.text.toString()) // change into loop?

        instruments.add(binding.instr1.text.toString())
        if (binding.instr2.text.toString().isNotEmpty()) {
            instruments.add(binding.instr2.text.toString())
        }
        if (binding.instr3.text.toString().isNotEmpty()) {
            instruments.add(binding.instr3.text.toString())
        }
        if (binding.instr4.text.toString().isNotEmpty()) {
            instruments.add(binding.instr4.text.toString())
        }
        if (binding.instr5.text.toString().isNotEmpty()) {
            instruments.add(binding.instr5.text.toString())
        }

        genres.add(binding.genre1.text.toString())
        genres.add(binding.genre2.text.toString())
        genres.add(binding.genre3.text.toString())
        if (binding.genre4.text.toString().isNotEmpty()) {
            genres.add(binding.genre4.text.toString())
        }
        if (binding.genre5.text.toString().isNotEmpty()) {
            genres.add(binding.genre5.text.toString())
        }

        val editedUser = mapOf(
            "profileType" to userProfileType,
            "displayName" to displayName,
            "age" to age,
            "bio" to bio,
            "website" to website,
            "latitude" to userLat,
            "longitude" to userLong,
            "influences" to influences,
            "instruments" to instruments,
            "genres" to genres
        )

        if (uid != null) {
            databaseReference.child(uid).updateChildren(editedUser)
                .addOnSuccessListener {
                    if (Patterns.WEB_URL.matcher(binding.tietInputURL.text.toString())
                            .matches()
                        && displayName.isNotEmpty()
                        && age.isNotEmpty()
                    ) {
                        hideProgressBar()
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT)
                            .show()
                        if (auth.currentUser != null) {
                            val intent = Intent(this, ViewProfile::class.java)
                            startActivity(intent)
                        }
                    } else {
                        hideProgressBar()
                        Toast.makeText(
                            this,
                            "Failed to update profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }


    // Adds user data if it exists to editable
    private fun readUserDataEdit(userUID: String)
    {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.child(userUID).get().addOnSuccessListener {

            if (it.exists()) {

                // Setting the user's data to viewable
                if (it.child("displayName").exists()){
                    binding.tietInputDisplayName.setText(it.child("displayName").value.toString())
                    binding.tietInputBio.setText(it.child("bio").value.toString())
                    binding.tvAgeRange.text = it.child("age").value.toString()
                    binding.tvAgeRange.text = it.child("age").value.toString()
                    binding.tietInputURL.setText(it.child("website").value.toString())
                    userLat = it.child("latitude").value.toString().toDouble()
                    userLong = it.child("longitude").value.toString().toDouble()
                    val imageURL = it.child("imageUrl").value.toString()
                    ////////
                    selectedImg = it.child("imageUrl").value.toString().toUri() // paaaaaaaaaaaaah
                    ////////
                    Picasso.get().load(imageURL).into(binding.userProfileImage)

                    val influences: String =
                        it.child("influences").value.toString().trim()
                            .replace("[", "")
                            .replace("]", "")

                    val inf = influences.split(", ").toTypedArray()

                    binding.artist1.text = inf.elementAt(0)
                    binding.artist2.text = inf.elementAt(1)
                    binding.artist3.text = inf.elementAt(2)
                    binding.artist4.text = inf.elementAt(3)
                    binding.artist5.text = inf.elementAt(4)

                    val instruments: String =
                        it.child("instruments").value.toString().trim()
                            .replace("[", "")
                            .replace("]", "")

                    val instr = instruments.split(", ").toTypedArray()

                    binding.instr1.text = instr.elementAt(0)

                    if (instr.size > 1){
                        binding.instr2.text = instr.elementAt(1).toString()
                    }else{
                        binding.instr2.text = ""
                    }
                    if (instr.size > 2){
                        binding.instr3.text = instr.elementAt(2).toString()
                    }else{
                        binding.instr3.text = ""
                    }
                    if (instr.size > 3){
                        binding.instr4.text = instr.elementAt(3).toString()
                    }else{
                        binding.instr4.text = ""
                    }
                    if (instr.size > 4){
                        binding.instr5.text = instr.elementAt(4).toString()
                    }else{
                        binding.instr5.text = ""
                    }

                    val genres: String =
                        it.child("genres").value.toString().trim()
                            .replace("[", "")
                            .replace("]", "")

                    val gen = genres.split(", ").toTypedArray()

                    binding.genre1.text = gen.elementAt(0)
                    binding.genre2.text = gen.elementAt(1)
                    binding.genre3.text = gen.elementAt(2)

                    if (gen.size > 3){
                        binding.genre4.text = gen.elementAt(3).toString()
                    }else{
                        binding.genre4.text = ""
                    }
                    if (gen.size > 4){
                        binding.genre5.text = gen.elementAt(4).toString()
                    }else{
                        binding.genre5.text = ""
                    }
                }
            } else {
                Toast.makeText(this, "Create profile", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "No user present", Toast.LENGTH_SHORT).show()
        }
    }


    // Saves user details
    private fun saveUserDetails(userProfileType: String) {
        val reference = storage.reference.child("Profile").child(Date().time.toString())
        reference.putFile(selectedImg).addOnCompleteListener{ it ->
            if (it.isSuccessful){
                reference.downloadUrl.addOnSuccessListener { task ->

                    val uid = auth.currentUser?.uid // gets the UID of the current user

                    // Values for user details
                    val imgUrl = task.toString()
                    val displayName = binding.tietInputDisplayName.text.toString()
                    val bio = binding.tietInputBio.text.toString()
                    val age = binding.tvAgeRange.text.toString()
                    val website = binding.tietInputURL.text.toString()
                    val influences = mutableListOf<String>()
                    val instruments = mutableListOf<String>()
                    val genres = mutableListOf<String>()
                    influences.add(binding.artist1.text.toString())
                    influences.add(binding.artist2.text.toString())
                    influences.add(binding.artist3.text.toString())
                    influences.add(binding.artist4.text.toString())
                    influences.add(binding.artist5.text.toString()) // change into loop?

                    instruments.add(binding.instr1.text.toString())
                    if(binding.instr2.text.toString().isNotEmpty()){
                        instruments.add(binding.instr2.text.toString())
                    }
                    if(binding.instr3.text.toString().isNotEmpty()){
                        instruments.add(binding.instr3.text.toString())
                    }
                    if(binding.instr4.text.toString().isNotEmpty()){
                        instruments.add(binding.instr4.text.toString())
                    }
                    if(binding.instr5.text.toString().isNotEmpty()){
                        instruments.add(binding.instr5.text.toString())
                    }

                    genres.add(binding.genre1.text.toString())
                    genres.add(binding.genre2.text.toString())
                    genres.add(binding.genre3.text.toString())
                    if(binding.genre4.text.toString().isNotEmpty()){
                        genres.add(binding.genre4.text.toString())
                    }
                    if(binding.genre5.text.toString().isNotEmpty()){
                        genres.add(binding.genre5.text.toString())
                    }

                    // Creating user details object
                    val user = User(
                        uid,
                        userProfileType,
                        imgUrl,
                        displayName,
                        age,
                        bio,
                        website,
                        userLat,
                        userLong,
                        influences,
                        instruments,
                        genres)

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users")

                    if(uid != null){
                        databaseReference.child(uid).setValue(user).addOnCompleteListener {
                            if (it.isSuccessful
                                && Patterns.WEB_URL.matcher(binding.tietInputURL.text.toString()).matches()
                                && displayName.isNotEmpty()
                                && age.isNotEmpty()) {


                                // download array and update here
                                downloadUidListAndUpdate(uid)

                                hideProgressBar()
                                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                                if (auth.currentUser != null) {
                                    val intent = Intent(this, ViewProfile::class.java)
                                    startActivity(intent)
                                }
                            }else{
                                hideProgressBar()
                                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }


    // Downloads and updates UID list
    private fun downloadUidListAndUpdate(uid: String)
    {
        databaseReference = FirebaseDatabase.getInstance().getReference("Usernames")
        databaseReference.child("allUIDs").get().addOnSuccessListener {

            if (it.exists()) {
                val uidsOfUsers: String = it.child("uidsOfUsers").value.toString().trim()
                    .replace("[", "")
                    .replace("]", "")
                val uidList = uidsOfUsers.split(", ").toMutableList()
                uidList.add(uid)
                val uids = UserUIDs(uidList)

                databaseReference.child("allUIDs").setValue(uids)
            }
        }
    }


    // for age bracket
    private fun updateAgeDescription(ageBracket: Int)
    {
        val ageDescription = when (ageBracket){
            in 0..7 -> "18-25"
            in 8..12 -> "26-35"
            in 13..17 -> "36-45"
            in 18..23 -> "46-59"
            else -> "60+"
        }
        binding.tvAgeRange.text = ageDescription
    }


    // for loading screen
    private fun showProgressBar()
    {
        dialog = Dialog (this@EditProfile)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


    // for loading screen
    private fun hideProgressBar()
    {
        dialog.dismiss()
    }


    // to get user location
    private fun getLocation()
    {
        // checks location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100
            )
            return
        }

        // gets latitude and longitude
        val location = fusedLocationProviderClient.lastLocation
        location.addOnSuccessListener {
            if(it != null){
                userLat = it.latitude.toString().toDouble()
                userLong = it.longitude.toString().toDouble()
                val userLocation= "Location found"
                binding.tvLatitude.text = userLocation
            }
        }
    }


    // to get genre data into an array
    private fun genreDataToString()
    {
        databaseReference = FirebaseDatabase.getInstance().getReference("GenreStore")
        databaseReference.child("genres").get().addOnSuccessListener {
            genresForDialog = it.value.toString().trim()
                .replace("[", "")
                .replace("]", "")
        }
    }


    // to fetch genre data from database
    private fun fetchGenreData(genreSearched: String)
    {
        databaseReference = FirebaseDatabase.getInstance().getReference("GenreStore")
        databaseReference.child("genres").get().addOnSuccessListener {

            if (it.exists()) {

                val genres: String =
                    it.value.toString().trim()
                        .replace("[", "")
                        .replace("]", "")

                val gen = genres.split(", ").toTypedArray()

                for (item in gen) {
                    if (genreSearched == item) {
                        val genreFound: String = item

                        if (binding.genre1.text.toString().isEmpty()) {
                            binding.genre1.text = genreFound
                        } else if (binding.genre2.text.toString().isEmpty()) {
                            binding.genre2.text = genreFound
                        } else if (binding.genre3.text.toString().isEmpty()) {
                            binding.genre3.text = genreFound
                        } else if (binding.genre4.text.toString().isEmpty()) {
                            binding.genre4.text = genreFound
                        } else if (binding.genre5.text.toString().isEmpty()) {
                            binding.genre5.text = genreFound
                        } else {
                            Toast.makeText(
                                this,
                                "Max number of genres is 5",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }


    // to fetch artist data
    private fun fetchLastFMData(artistSearched: String): Thread
    {
        return Thread {

            val artistSearched2 = artistSearched.replace("\\s", "%20").trim()
            val url = URL("https://ws.audioscrobbler.com/2.0/?method=artist.search&artist=$artistSearched2&api_key=~insert api key~&format=json") // !!?
            val connection  = url.openConnection() as HttpsURLConnection

            if(connection.responseCode == 200)
            {
                val inputSystem = connection.inputStream
                val inputStreamReader = InputStreamReader(inputSystem, "UTF-8")
                val request = Gson().fromJson(inputStreamReader, Request::class.java)
                upUI(request)

                // closing stream
                inputStreamReader.close()
                inputSystem.close()
            } else {
                Toast.makeText(this, "Connection cannot be made", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // to update UI with artist data
    private fun upUI(request: Request)
    {
        runOnUiThread {
            kotlin.run {

                if (request.results.artistmatches.artist.isEmpty()){
                    Toast.makeText(this, "No artist found that fits that name", Toast.LENGTH_SHORT).show()
                }else{
                    val count = request.results.artistmatches.artist.count()

                    if (count == 1){
                        binding.radioOne.text = request.results.artistmatches.artist.elementAt(0).name
                    }
                    if (count == 2){
                        binding.radioOne.text = request.results.artistmatches.artist.elementAt(0).name
                        binding.radioTwo.text = request.results.artistmatches.artist.elementAt(1).name
                    }
                    if (count >= 3){
                        binding.radioOne.text = request.results.artistmatches.artist.elementAt(0).name
                        binding.radioTwo.text = request.results.artistmatches.artist.elementAt(1).name
                        binding.radioThree.text = request.results.artistmatches.artist.elementAt(2).name
                    }
                }
            }
        }
    }


    // to fetch instrument data
    private fun fetchInstrumentData(instrumentName: String): Thread
    {
        return Thread {

            val url = URL("https://musicbrainz.org/ws/2/instrument?query=$instrumentName&limit=5&offset=0&fmt=json")
            val connection  = url.openConnection() as HttpsURLConnection

            if(connection.responseCode == 200)
            {
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
    private fun upUI2(request: instrumentsAPI.Request)
    {
        runOnUiThread {
            kotlin.run {
                if (request.instruments.isEmpty()){
                    Toast.makeText(this, "No instrument found that fits that name", Toast.LENGTH_SHORT).show()
                }else{

                    val count = request.instruments.count()

                    // Toast.makeText(this, count.toString(), Toast.LENGTH_SHORT).show()

                    if (count == 1){
                        binding.radioOneInstr.text = request.instruments.elementAt(0).name
                    }
                    if (count == 2){
                        binding.radioOneInstr.text = request.instruments.elementAt(0).name
                        binding.radioTwoInstr.text = request.instruments.elementAt(1).name
                    }
                    if (count >= 3){
                        binding.radioOneInstr.text = request.instruments.elementAt(0).name
                        binding.radioTwoInstr.text = request.instruments.elementAt(1).name
                        binding.radioThreeInstr.text = request.instruments.elementAt(2).name
                    }
                }
            }
        }
    }


    // unsure, to do with image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null){
            if (data.data != null){
                selectedImg = data.data!!
                binding.userProfileImage.setImageURI(selectedImg)
            }
        }
    }
}
