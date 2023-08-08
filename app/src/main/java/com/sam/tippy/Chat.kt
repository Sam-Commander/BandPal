package com.sam.tippy

import userData.MessageAdaptor
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sam.tippy.databinding.ActivityChatPageBinding

class Chat : AppCompatActivity()
{
    // global variables
    private lateinit var binding: ActivityChatPageBinding
    private lateinit var messageAdaptor: MessageAdaptor
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dbRef: DatabaseReference

    // Creates a unique room for sender and receiver, so message is private
    var receiverRoom: String ?= null
    var senderRoom: String ?= null


    // On create
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityChatPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ////////////

        // Received from previous activity
        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        dbRef = FirebaseDatabase.getInstance().reference

        // Create unique room
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        // Updating UI
        supportActionBar?.title = name
        messageList = ArrayList()
        messageAdaptor = MessageAdaptor(this, messageList)
        binding.chatRV.layoutManager = LinearLayoutManager(this)
        binding.chatRV.adapter = messageAdaptor

        // Handles logic for adding data to RV
        dbRef.child("allChats").child(senderRoom!!).child("messages")
            .addValueEventListener(object: ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    messageList.clear()

                    for (postSnapshot in snapshot.children){
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    messageAdaptor.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })


        // Handles logic of adding message to database
        binding.sendButton.setOnClickListener {
            val message = binding.chatBox.text.toString()
            val messageObj = Message(message, senderUid)

            dbRef.child("allChats").child(senderRoom!!).child("messages").push()
                .setValue(messageObj).addOnSuccessListener {
                    dbRef.child("allChats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObj)
                }
            binding.chatBox.setText("")
        }
    }
}