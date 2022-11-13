package com.example.android_sns

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android_sns.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
        private lateinit var auth : FirebaseAuth
        private lateinit var googleSignInClient : GoogleSignInClient

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val binding = ActivityLoginBinding.inflate(layoutInflater)
                setContentView(binding.root)
                auth = FirebaseAuth.getInstance()

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()

                googleSignInClient = GoogleSignIn.getClient(this , gso)


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

                binding.btnGoogleLogin.setOnClickListener {
                        doGoogleLogin()
                }
        }

        private fun doGoogleLogin() {
                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
        }

        private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                        result ->
                if (result.resultCode == Activity.RESULT_OK){

                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        handleResults(task)
                }
        }

        private fun handleResults(task: Task<GoogleSignInAccount>) {
                if (task.isSuccessful){
                        val account : GoogleSignInAccount? = task.result
                        if (account != null){
                                updateUI(account)
                        }
                }else{
                        Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
                }
        }

        private fun updateUI(account: GoogleSignInAccount) {
                val credential = GoogleAuthProvider.getCredential(account.idToken , null)
                auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful){
                                val intent : Intent = Intent(this , MainActivity::class.java)
                                //intent.putExtra("email" , account.email)
                                //intent.putExtra("name" , account.displayName)
                                startActivity(intent)
                        }else{
                                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

                        }
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