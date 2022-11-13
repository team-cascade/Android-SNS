package com.example.android_sns

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android_sns.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
        var auth : FirebaseAuth? = null

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val binding = ActivityLoginBinding.inflate(layoutInflater)
                setContentView(binding.root)
                auth = FirebaseAuth.getInstance()


                binding.btnLogin.setOnClickListener {
                        val userEmail = binding.textID.text.toString()
                        val password = binding.textPWD.text.toString()
                        doLogin(userEmail, password)
                }


                binding.btnSignup.setOnClickListener {
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
}