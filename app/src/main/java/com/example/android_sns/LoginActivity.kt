package com.example.android_sns

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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
        var auth : FirebaseAuth? = null
        var TAG = "googleLogin"
        var mGoogleSignInClient : GoogleSignInClient? = null
        private lateinit var googleSignInClient : GoogleSignInClient

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val binding = ActivityLoginBinding.inflate(layoutInflater)
                setContentView(binding.root)
                auth = FirebaseAuth.getInstance()


                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("677324009749-tgokim2qc50ni54fuoi034ng0kradiu6.apps.googleusercontent.com")
                        .requestEmail()
                        .build()
                mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

                binding.btnGoogle.setOnClickListener {
                        signIn()
                }


                binding.btnLogin.setOnClickListener {
                        val userEmail = binding.textID.text.toString()
                        val password = binding.textPWD.text.toString()
                        doLogin(userEmail, password)
                }


                binding.signup.setOnClickListener {
                        startActivity(
                                Intent(this, SignupActivity::class.java))
                        finish()
                }
        }
        var googleLogInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
                if(result.resultCode == -1) {
                        val data = result.data
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        getGoogleInfo(task)
                }
        }
        private fun signIn() {
               val signInIntent = mGoogleSignInClient!!.signInIntent
               googleLogInLauncher.launch(signInIntent)
        }
        fun getGoogleInfo(completedTask : Task<GoogleSignInAccount>) {
                try {
                        val TAG = "구글 로그인 결과"
                        val account = completedTask.getResult(ApiException::class.java)
                        Log.d(TAG, account.id!!)
                        Log.d(TAG, account.familyName!!)
                        Log.d(TAG, account.givenName!!)
                        Log.d(TAG, account.email!!)
                }
                catch (e: ApiException) {
                        Log.w(TAG, "signInResult:failed code=" + e.statusCode)
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