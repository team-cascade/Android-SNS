package com.example.android_sns

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_sns.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val binding = ActivityLoginBinding.inflate(layoutInflater)
                setContentView(binding.root)

                binding.login.setOnClickListener {
                        val userEmail = binding.username.text.toString()
                        val password = binding.password.text.toString()
                        doLogin(userEmail, password)
                }

//                binding.signup.setOnClickListener {
//                        val userEmail = binding.username.text.toString()
//                        val password = binding.password.text.toString()
//                        doSignUp(userEmail, password)
//                }
                binding.signup.setOnClickListener {
                        startActivity(
                                Intent(this, SignupActivity::class.java))
                        finish()
                }
        }

        private fun doLogin(userEmail: String, password: String) {
                Firebase.auth.signInWithEmailAndPassword(userEmail, password)
                        .addOnCompleteListener(this) {
                                if (it.isSuccessful) {
                                        startActivity(
                                                Intent(this, MainActivity::class.java))
                                        finish()
                                } else {
                                        Log.w("LoginActivity", "signInWithEmail", it.exception)
                                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                                }
                        }
        }

        private fun doSignUp(userEmail: String, password: String) {
                Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
                        .addOnCompleteListener(this) {
                                if (it.isSuccessful) {
                                        startActivity(
                                                Intent(this, MainActivity::class.java))
                                        finish()
                                } else {
                                        Log.w("LoginActivity", "createUserWithEmail", it.exception)
                                        Toast.makeText(this, "Sign Up failed.", Toast.LENGTH_SHORT).show()
                                }
                        }
        }
}