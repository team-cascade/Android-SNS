package com.example.androd_sns_wearos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.androd_sns_wearos.databinding.ActivityMainBinding
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import model.UserDTO

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    var firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (firebaseAuth.currentUser == null) {
            startActivity(
                Intent(this, LoginActivity::class.java))
            finish()
        }
        else {
            var userDTO: UserDTO? = null
            firestore?.collection("users")?.document(firebaseAuth.currentUser!!.uid)?.get()
                ?.addOnSuccessListener {
                    userDTO = it.toObject(UserDTO::class.java)!!
                    if (userDTO!!.profileImageUrl != null)
                        Glide.with(binding.profileImageView.context)
                            .load(userDTO!!.profileImageUrl?.toUri())
                            .apply(RequestOptions().circleCrop()).into(binding.profileImageView)
                }
            binding.profileImageView.setOnClickListener {
                startActivity(
                    Intent(this, AlarmActivity::class.java)
                )
            }
        }
    }
}