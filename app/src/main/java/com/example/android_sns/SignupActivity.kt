package com.example.android_sns

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_sns.databinding.ActivitySignupBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import model.UserDTO
import java.util.*

class SignupActivity: AppCompatActivity() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollectionRef = db.collection("users")
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.BackButton.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
            finish()
        }

        binding.signupButton.setOnClickListener {
            val userEmail = binding.EmailInput.text.toString()
            val password = binding.PasswordInput.text.toString()
            val name = binding.NameInput.text.toString()
            val birth = binding.BirthInput.text.toString()
            doSignUp(userEmail, password, name, birth)
        }

        binding.BirthInput.setOnClickListener {
            var calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            var dateListener = DatePickerDialog.OnDateSetListener {
                    view, year, month, dayOfMonth ->
                binding.BirthInput.setText("${year}${String.format("%02d",month + 1)}${String.format("%02d",dayOfMonth)}")
            }

            var builder = DatePickerDialog(this, dateListener, year, month, day)
            builder.show()
        }
    }

    private fun doSignUp(userEmail: String, password: String, name: String, birth: String) {
        if(userEmail.isNullOrEmpty() || password.isNullOrEmpty() || name.isNullOrEmpty() || birth.isNullOrEmpty()) {
            binding.signupErrorText.text = "입력하지 않은 항목이 있습니다."
            return
        }

        // 파이어스토어 user 이름 중복 검사
        usersCollectionRef.get().addOnSuccessListener { snapshot ->
            var overlapFlag = false
            for(doc in snapshot) {
                if(doc.get("username") == name) {
                    overlapFlag = true
                }
            }
            if(!overlapFlag)
                firebaseSignUp(userEmail, password, name, birth)
            else
                binding.signupErrorText.text = "이미 있는 사용자 이름입니다."
        }
    }
    private fun addUser(uid: String, userEmail: String, name: String, birth: String) {
        var userDTO = UserDTO()
        userDTO.uid = uid
        userDTO.useremail = userEmail
        userDTO.username = name
        userDTO.userBirth = birth
        usersCollectionRef.document(uid).set(userDTO)
    }
    private fun firebaseSignUp(userEmail: String, password: String, name: String, birth: String) {
        Firebase.auth.createUserWithEmailAndPassword(userEmail, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    var uid = it.result.user!!.uid
                    addUser(uid, userEmail, name, birth)
                    startActivity(
                        Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.w("SignUpActivity", "createUserWithEmail", it.exception )
                    //Toast.makeText(this, "Sign Up failed.", Toast.LENGTH_SHORT).show()
                    var errorMessage = it.exception?.localizedMessage
                    when (errorMessage) {
                        "The email address is badly formatted." ->
                            errorMessage = "이메일이 형식에 맞지 않습니다."
                        "The email address is already in use by another account." ->
                            errorMessage = "이미 등록되어 있는 이메일입니다."
                        "The given password is invalid. [ Password should be at least 6 characters ]" ->
                            errorMessage = "비밀번호는 최소 6자리를 입력해야 합니다."
                    }
                    binding.signupErrorText.text = errorMessage
                }
            }
    }


}