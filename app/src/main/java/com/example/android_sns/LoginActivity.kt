package com.example.android_sns

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android_sns.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
        private lateinit var auth : FirebaseAuth
        private lateinit var binding: ActivityLoginBinding

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                binding = ActivityLoginBinding.inflate(layoutInflater)
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

        // 파이어베이스 로그인
        private fun doLogin(userEmail: String, password: String) {
                if(userEmail.isNullOrEmpty() || password.isNullOrEmpty()) {
                        binding.loginErrorText.text = "이메일과 비밀번호를 입력해주세요."
                        return
                }
                Firebase.auth.signInWithEmailAndPassword(userEmail, password)
                        .addOnCompleteListener(this) {
                                if (it.isSuccessful) {
                                        startActivity(
                                                Intent(this, MainActivity::class.java))
                                        finish()
                                } else {
                                        Log.w("LoginActivity", "signInWithEmail", it.exception)
                                        //Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                                        var errorMessage = it.exception?.localizedMessage
                                        when (errorMessage) {
                                                "The email address is badly formatted." ->
                                                                errorMessage = "이메일이 형식에 맞지 않습니다."
                                                "There is no user record corresponding to this identifier. The user may have been deleted." ->
                                                                errorMessage = "존재하지 않는 유저 이메일입니다."
                                                "The password is invalid or the user does not have a password." ->
                                                                errorMessage = "비밀번호가 일치하지 않습니다."
                                        }
                                        binding.loginErrorText.text = errorMessage
                                }
                        }
        }
}