package com.sam.tippy

import userData.PMatch
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sam.tippy.databinding.ActivityResultsBinding
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.random.Random


class SearchResults : AppCompatActivity()
{
    // Global variables
    private lateinit var binding: ActivityResultsBinding
    private lateinit var database: DatabaseReference
    private val user = FirebaseAuth.getInstance().currentUser
    private val userUID = user?.uid
    private lateinit var uid1: String
    private lateinit var uid2: String
    private lateinit var uid3: String
    private lateinit var uid4: String
    private lateinit var uid5: String
    private lateinit var radius1: String
    private lateinit var radius2: String
    private lateinit var radius3: String
    private lateinit var radius4: String
    private lateinit var radius5: String


    // On create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ///////////

        database = FirebaseDatabase.getInstance().getReference("Usernames")

        pullUsernames()

        // Buttons
        binding.pMatchBut1.setOnClickListener {
            val intent = Intent(this, ViewMatchProfile::class.java)
            intent.putExtra("uid", uid1)
            intent.putExtra("radius", radius1)
            startActivity(intent)
        }
        binding.pMatchBut2.setOnClickListener {
            val intent = Intent(this, ViewMatchProfile::class.java)
            intent.putExtra("uid", uid2)
            intent.putExtra("radius", radius2)
            startActivity(intent)
        }
        binding.pMatchBut3.setOnClickListener {
            val intent = Intent(this, ViewMatchProfile::class.java)
            intent.putExtra("uid", uid3)
            intent.putExtra("radius", radius3)
            startActivity(intent)
        }
        binding.pMatchBut4.setOnClickListener {
            val intent = Intent(this, ViewMatchProfile::class.java)
            intent.putExtra("uid", uid4)
            intent.putExtra("radius", radius4)
            startActivity(intent)
        }
        binding.pMatchBut5.setOnClickListener {
            val intent = Intent(this, ViewMatchProfile::class.java)
            intent.putExtra("uid", uid5)
            intent.putExtra("radius", radius5)
            startActivity(intent)
        }
    }


    // Pulls all usernames from DB
    private fun pullUsernames()
    {
        database = FirebaseDatabase.getInstance().getReference("Usernames")
        database.child("allUIDs").get().addOnSuccessListener {

            if (it.exists()) {
                val uidList = it.child("uidsOfUsers").value.toString()
                    .trim()
                    .replace("[", "")
                    .replace("]", "")
                    .split(", ")
                    .toMutableList()

                matchInstrMileAge(uidList)
            }
        }
    }


    // Matches searched instr, mile and age to all user deets
    private fun matchInstrMileAge(uidList: MutableList<String>)
    {
        // ++ as uidList is iterated
        var count = 0

        // All need to be true to allow pMatchUID to be added to list 1
        var instrMatch = false
        var distanceMatch = false
        var ageMatch = false

        // User location
        var userLat = 0.0
        var userLong = 0.0

        // User's search terms
        var searchedInstr: String
        var searchedRadius: Double
        var searchedAgeBracket: String

        // Size of UID list
        val sizeOfUIDList: Int = uidList.size

        // Passed list
        val passedList: MutableList<String> = mutableListOf()

        database = FirebaseDatabase.getInstance().getReference("Users")
        if (userUID != null) {

            database.child(userUID).get().addOnSuccessListener { it1 ->
                userLat = it1.child("latitude").value.toString().toDouble()
                userLong = it1.child("longitude").value.toString().toDouble()
            }.addOnSuccessListener {
                database.child(userUID).child("search").get().addOnSuccessListener {
                    searchedInstr = it.child("searchInstr").value.toString()
                    searchedRadius = it.child("searchRadius").value.toString().toDouble()
                    searchedAgeBracket = it.child("oldestBracket").value.toString()

                    for (pMatchUID in uidList) {

                        // We in the loop

                        database.child(pMatchUID).get().addOnSuccessListener { it2 ->
                            if (it2.exists()) {

                                // We in the pMatch listener
                                val pMatchProfileType = it2.child("profileType").value.toString()

                                // pMatch instrument(s)
                                val pMatchInstr: String = it2.child("instruments").value.toString()
                                val pMatchInstruments = pMatchInstr
                                    .trim()
                                    .replace("[", "")
                                    .replace("]", "")
                                    .split(", ")
                                    .toMutableList()

                                // Checks to see if any of pMatch's instruments match the searched instrument
                                for (i in pMatchInstruments) {
                                    if (i == searchedInstr || searchedInstr == "anything") {
                                        instrMatch = true
                                    }
                                }

                                ////////////////////////////////////

                                // pMatch lat and long
                                val pMatchLat: Double = it2.child("latitude").value.toString().toDouble()
                                val pMatchLong: Double = it2.child("longitude").value.toString().toDouble()

                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(
                                    userLat,
                                    userLong,
                                    pMatchLat,
                                    pMatchLong,
                                    results
                                )

                                val distanceRadius: Double = String.format("%.1f", results[0] / 1609.344).toDouble()

                                // Checks to see if distance between user and pMatch fits with searched max distance
                                if (distanceRadius < searchedRadius) {
                                    distanceMatch = true
                                }

                                ///////////////////////////////////////////

                                // pMatch age
                                val pMatchAge: String = it2.child("age").value.toString()

                                // Checks to see if ages align
                                if (searchedAgeBracket == pMatchAge
                                    || searchedAgeBracket == "60+"
                                ) {
                                    ageMatch = true
                                }
                                if (searchedAgeBracket == "26-35" && pMatchAge == "18-25"
                                    || searchedAgeBracket == "36-45" && pMatchAge == "18-25"
                                    || searchedAgeBracket == "36-45" && pMatchAge == "26-35"
                                    || searchedAgeBracket == "46-59" && pMatchAge == "18-25"
                                    || searchedAgeBracket == "46-59" && pMatchAge == "26-35"
                                    || searchedAgeBracket == "46-59" && pMatchAge == "36-45"
                                ) {
                                    ageMatch = true
                                }

                                count++

                                // THE MIGHTY MATCHER
                                if (pMatchUID != userUID && pMatchProfileType != "Band" && instrMatch && distanceMatch && ageMatch) {
                                    passedList.add(pMatchUID)
                                }
                                if(count >= sizeOfUIDList && passedList.isNotEmpty()){
                                    matchInfluencesAndGenres(passedList)
                                }else if(count >= sizeOfUIDList){
                                    binding.pMatchName1.text = getString(R.string.nomatch)
                                    binding.pMatchImage1.visibility = View.INVISIBLE
                                    binding.pMatchImage2.visibility = View.INVISIBLE
                                    binding.pMatchImage3.visibility = View.INVISIBLE
                                    binding.pMatchImage4.visibility = View.INVISIBLE
                                    binding.pMatchImage5.visibility = View.INVISIBLE
                                    binding.pMatchBut1.visibility = View.INVISIBLE
                                    binding.pMatchBut2.visibility = View.INVISIBLE
                                    binding.pMatchBut3.visibility = View.INVISIBLE
                                    binding.pMatchBut4.visibility = View.INVISIBLE
                                    binding.pMatchBut5.visibility = View.INVISIBLE
                                    binding.aLine1.visibility = View.INVISIBLE
                                    binding.aLine2.visibility = View.INVISIBLE
                                    binding.aLine3.visibility = View.INVISIBLE
                                    binding.aLine4.visibility = View.INVISIBLE
                                    binding.aLine5.visibility = View.INVISIBLE
                                }
                                instrMatch = false
                                distanceMatch = false
                                ageMatch = false

                            }
                        }
                    }
                }
            }
        }
    }


    // Finds out if user's search terms align with data of pMatch
    // Also adds points for pMatch having first match, genre and instr trophies
    private fun matchInfluencesAndGenres(passed: MutableList<String>?)
    {
//        binding.tvUsernames.text = passed.toString()

        val newMap = hashMapOf<Double, PMatch>()
        val newIndexesOfMap: MutableList<Double> = mutableListOf()

        var point = 0.0
        var count = 0.0
        val sizeOfPassed = passed?.size

        // User location
        var userLat: Double
        var userLong: Double

        // Gets current user's influences and genres
        database = FirebaseDatabase.getInstance().getReference("Users")
        if (userUID != null) {
            database.child(userUID).get().addOnSuccessListener { itNow ->

                val userInfluences = itNow.child("influences").value.toString()
                    .trim()
                    .replace("[", "")
                    .replace("]", "")
                    .split(", ")
                    .toMutableList()

                val userGenres = itNow.child("genres").value.toString()
                    .trim()
                    .replace("[", "")
                    .replace("]", "")
                    .split(", ")
                    .toMutableList()

                userLat = itNow.child("latitude").value.toString().toDouble()
                userLong = itNow.child("longitude").value.toString().toDouble()

                // Loops over pMatches
                if (passed != null) {
                    for (pMatchUID in passed){

                        // We in the loop

                        // Gets pMatch influences and genres
                        database.child(pMatchUID).get().addOnSuccessListener { it2 ->
                            if (it2.exists()) {

                                val pMatchLat: Double = it2.child("latitude").value.toString().toDouble()
                                val pMatchLong: Double = it2.child("longitude").value.toString().toDouble()

                                val pMatchInfluences = it2.child("influences").value.toString()
                                    .trim()
                                    .replace("[", "")
                                    .replace("]", "")
                                    .split(", ")
                                    .toMutableList()

                                val pMatchImageUrl = it2.child("imageUrl").value.toString()
                                val displayName = it2.child("displayName").value.toString()

                                val matchedInfluences = mutableListOf<String>()

                                val pMatchGenres = it2.child("genres").value.toString()
                                    .trim()
                                    .replace("[", "")
                                    .replace("]", "")
                                    .split(", ")
                                    .toMutableList()

                                val matchedGenres = mutableListOf<String>()

                                for (pMatchArtist in pMatchInfluences){
                                    for (userArtist in userInfluences){
                                        if (pMatchArtist == userArtist){
                                            matchedInfluences.add(pMatchArtist)
                                            point++
                                        }
                                    }
                                }

                                for (pMatchGenre in pMatchGenres){
                                    for (userGenre in userGenres){
                                        if (pMatchGenre == userGenre){
                                            matchedGenres.add(pMatchGenre)
                                            point++
                                        }
                                    }
                                }

                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(
                                    userLat,
                                    userLong,
                                    pMatchLat,
                                    pMatchLong,
                                    results
                                )

                                val distanceRadius: Double =
                                    String.format("%.1f", results[0] / 1609.344).toDouble()


                                // Trophies - First Match
                                if (it2.child("connectedMatches").exists()){
                                    point++
                                }

                                // Trophies - Maestro
                                val pMatchInstrList = it2.child("instruments").value.toString()
                                    .trim()
                                    .replace("[", "")
                                    .replace("]", "")
                                    .split(", ")
                                    .toMutableList()

                                if (pMatchInstrList.size == 5){
                                    point++
                                }

                                // Trophies - Genrehead
                                if (pMatchGenres.size == 5){
                                    point++
                                }


                                count++

                                val rand = Random.nextDouble(0.00001, 0.99999)

                                point += rand

                                newIndexesOfMap.add(point)
                                newMap[point] = PMatch(pMatchUID, matchedInfluences, matchedGenres, distanceRadius, pMatchImageUrl, displayName)

                                point = 0.0
                                if(count >= sizeOfPassed!!){
                                    val sortedMap = newMap.toSortedMap(compareByDescending { it })
                                    val sortedSet = newIndexesOfMap.toSortedSet(compareByDescending { it })
                                    outputPMatches(sortedMap, sortedSet)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    //  Outputs to UI
    private fun outputPMatches(sortedPMatches: SortedMap<Double, PMatch>, pMatchIndexes: SortedSet<Double>)
    {

        val sizeOfIndex = pMatchIndexes.size
        var count = 1
        val youBothLike = "You both like:\n"
        val milesAway = " miles away"

        for (pMatchIndex in pMatchIndexes){

            // Module 1
            if (sizeOfIndex >= 1 && count == 1){
                binding.pMatchName1.text = sortedPMatches[pMatchIndexes.elementAt(0)]?.displayName
                (sortedPMatches[pMatchIndexes.elementAt(0)]?.distanceToPMatch.toString() + milesAway)
                    .also { binding.pMatchMilesAway1.text = it }

                (youBothLike + sortedPMatches[pMatchIndexes.elementAt(0)]?.matchedInfluences.toString()
                    .replace("]", "")
                    .replace("[", "")).also { binding.pMatchInf1.text = it }

                binding.pMatchGen1.text =
                    sortedPMatches[pMatchIndexes.elementAt(0)]?.matchedGenres.toString()
                        .replace("]", "")
                        .replace("[", "")

                Picasso.get().load(sortedPMatches[pMatchIndexes.elementAt(0)]?.imageUrl).into(binding.pMatchImage1)

                uid1 = sortedPMatches[pMatchIndexes.elementAt(0)]?.uid.toString()
                radius1 = sortedPMatches[pMatchIndexes.elementAt(0)]?.distanceToPMatch.toString()

                if (sizeOfIndex == 1){
                    binding.pMatchImage2.visibility = View.INVISIBLE
                    binding.pMatchImage3.visibility = View.INVISIBLE
                    binding.pMatchImage4.visibility = View.INVISIBLE
                    binding.pMatchImage5.visibility = View.INVISIBLE
                    binding.pMatchBut2.visibility = View.INVISIBLE
                    binding.pMatchBut3.visibility = View.INVISIBLE
                    binding.pMatchBut4.visibility = View.INVISIBLE
                    binding.pMatchBut5.visibility = View.INVISIBLE
                    binding.aLine2.visibility = View.INVISIBLE
                    binding.aLine3.visibility = View.INVISIBLE
                    binding.aLine4.visibility = View.INVISIBLE
                    binding.aLine5.visibility = View.INVISIBLE
                    break
                }
            }

            // Module 2
            if (sizeOfIndex >= 2 && count == 2){
                binding.pMatchName2.text = sortedPMatches[pMatchIndexes.elementAt(1)]?.displayName
                (sortedPMatches[pMatchIndexes.elementAt(1)]?.distanceToPMatch.toString() + milesAway)
                    .also { binding.pMatchMilesAway2.text = it }

                (youBothLike + sortedPMatches[pMatchIndexes.elementAt(1)]?.matchedInfluences.toString()
                    .replace("]", "")
                    .replace("[", "")).also { binding.pMatchInf2.text = it }

                binding.pMatchGen2.text =
                    sortedPMatches[pMatchIndexes.elementAt(1)]?.matchedGenres.toString()
                        .replace("]", "")
                        .replace("[", "")

                Picasso.get().load(sortedPMatches[pMatchIndexes.elementAt(1)]?.imageUrl).into(binding.pMatchImage2)

                uid2 = sortedPMatches[pMatchIndexes.elementAt(1)]?.uid.toString()
                radius2 = sortedPMatches[pMatchIndexes.elementAt(1)]?.distanceToPMatch.toString()

                if (sizeOfIndex == 2) {
                    binding.pMatchImage3.visibility = View.INVISIBLE
                    binding.pMatchImage4.visibility = View.INVISIBLE
                    binding.pMatchImage5.visibility = View.INVISIBLE
                    binding.pMatchBut3.visibility = View.INVISIBLE
                    binding.pMatchBut4.visibility = View.INVISIBLE
                    binding.pMatchBut5.visibility = View.INVISIBLE
                    binding.aLine3.visibility = View.INVISIBLE
                    binding.aLine4.visibility = View.INVISIBLE
                    binding.aLine5.visibility = View.INVISIBLE
                    break
                }
            }

            // Module 3
            if (sizeOfIndex >= 3 && count == 3){
                binding.pMatchName3.text = sortedPMatches[pMatchIndexes.elementAt(2)]?.displayName
                (sortedPMatches[pMatchIndexes.elementAt(2)]?.distanceToPMatch.toString() + milesAway)
                    .also { binding.pMatchMilesAway3.text = it }

                (youBothLike + sortedPMatches[pMatchIndexes.elementAt(2)]?.matchedInfluences.toString()
                    .replace("]", "")
                    .replace("[", "")).also { binding.pMatchInf3.text = it }

                binding.pMatchGen3.text =
                    sortedPMatches[pMatchIndexes.elementAt(2)]?.matchedGenres.toString()
                        .replace("]", "")
                        .replace("[", "")

                Picasso.get().load(sortedPMatches[pMatchIndexes.elementAt(2)]?.imageUrl).into(binding.pMatchImage3)

                uid3 = sortedPMatches[pMatchIndexes.elementAt(2)]?.uid.toString()
                radius3 = sortedPMatches[pMatchIndexes.elementAt(2)]?.distanceToPMatch.toString()

                if (sizeOfIndex == 3) {
                    binding.pMatchImage4.visibility = View.INVISIBLE
                    binding.pMatchImage5.visibility = View.INVISIBLE
                    binding.pMatchBut4.visibility = View.INVISIBLE
                    binding.pMatchBut5.visibility = View.INVISIBLE
                    binding.aLine4.visibility = View.INVISIBLE
                    binding.aLine5.visibility = View.INVISIBLE
                    break
                }
            }

            // Module 4
            if (sizeOfIndex >= 4 && count == 4){
                binding.pMatchName4.text = sortedPMatches[pMatchIndexes.elementAt(3)]?.displayName
                (sortedPMatches[pMatchIndexes.elementAt(3)]?.distanceToPMatch.toString() + milesAway)
                    .also { binding.pMatchMilesAway4.text = it }

                (youBothLike + sortedPMatches[pMatchIndexes.elementAt(3)]?.matchedInfluences.toString()
                    .replace("]", "")
                    .replace("[", "")).also { binding.pMatchInf4.text = it }

                binding.pMatchGen4.text =
                    sortedPMatches[pMatchIndexes.elementAt(3)]?.matchedGenres.toString()
                        .replace("]", "")
                        .replace("[", "")

                Picasso.get().load(sortedPMatches[pMatchIndexes.elementAt(3)]?.imageUrl).into(binding.pMatchImage4)

                uid4 = sortedPMatches[pMatchIndexes.elementAt(3)]?.uid.toString()
                radius4 = sortedPMatches[pMatchIndexes.elementAt(3)]?.distanceToPMatch.toString()

                if (sizeOfIndex == 4) {
                    binding.pMatchImage5.visibility = View.INVISIBLE
                    binding.pMatchBut5.visibility = View.INVISIBLE
                    binding.aLine5.visibility = View.INVISIBLE
                    break
                }
            }

            // Module 5
            if (sizeOfIndex >= 5 && count == 5){
                binding.pMatchName5.text = sortedPMatches[pMatchIndexes.elementAt(4)]?.displayName
                (sortedPMatches[pMatchIndexes.elementAt(4)]?.distanceToPMatch.toString() + milesAway)
                    .also { binding.pMatchMilesAway5.text = it }

                (youBothLike + sortedPMatches[pMatchIndexes.elementAt(4)]?.matchedInfluences.toString()
                    .replace("]", "")
                    .replace("[", "")).also { binding.pMatchInf5.text = it }

                binding.pMatchGen5.text =
                    sortedPMatches[pMatchIndexes.elementAt(4)]?.matchedGenres.toString()
                        .replace("]", "")
                        .replace("[", "")

                Picasso.get().load(sortedPMatches[pMatchIndexes.elementAt(4)]?.imageUrl).into(binding.pMatchImage5)

                uid5 = sortedPMatches[pMatchIndexes.elementAt(4)]?.uid.toString()
                radius5 = sortedPMatches[pMatchIndexes.elementAt(4)]?.distanceToPMatch.toString()
            }

            // Increase count as last thing
            if (count == 6){
                break
            }
            count++
        }
    }
}
