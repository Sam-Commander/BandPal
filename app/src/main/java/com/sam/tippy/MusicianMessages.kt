package com.sam.tippy

import userData.MessengerAdaptor
import userData.User
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MusicianMessages : AppCompatActivity()
{
    // Global variables
    private lateinit var messengerRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adaptor: MessengerAdaptor
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference


    // On create
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_musician_messages)
        this.title = "Messages"

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        userList = ArrayList()
        adaptor = MessengerAdaptor(this, userList)

        messengerRecyclerView = findViewById(R.id.messengerRecyclerView)
        messengerRecyclerView.layoutManager = LinearLayoutManager(this)
        messengerRecyclerView.adapter = adaptor

        val userAuth = FirebaseAuth.getInstance().currentUser
        val userUID = userAuth?.uid

        databaseReference.child("Users").child(userUID!!).get().addOnSuccessListener {
            if (it.child("connectedMatches").exists()){
                val connectedMatches: String =
                    it.child("connectedMatches").value.toString().trim()
                        .replace("[", "")
                        .replace("]", "")
                val connectedMatches2 = connectedMatches.split(", ").toMutableList()

                // Fills recyclerview with messaged matches
                databaseReference.child("Users").addValueEventListener(object: ValueEventListener{
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {

                        userList.clear()
                        for (individualUser in snapshot.children){
                            val currentUser = individualUser.getValue(User::class.java)

                            if (auth.currentUser?.uid != currentUser?.uid && connectedMatches2.contains(currentUser?.uid)){
                                userList.add(currentUser!!)
                            }
                        }
                        adaptor.notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }


    // Overrides back button to take user to their own view profile
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ViewProfile::class.java)
        startActivity(intent)
    }
}