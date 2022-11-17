package com.example.android_sns

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import model.ContentDTO
import java.util.*

class UploadActivity : AppCompatActivity() {
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_upload)
        //Initiate storage

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //콜백 함수
        val getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    photoUri = it.data?.data
                    findViewById<ImageView>(R.id.upload_image).setImageURI(photoUri)
                } else {
                    finish()
                }
            }
        // 액티비티 실행하자마자 앨범 열리기
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        getResult.launch(photoPickerIntent)

        findViewById<Button>(R.id.btn_upload).setOnClickListener {
            contentUpload()
        }
    }

    fun contentUpload() {
        // 파일명 생성
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //파일 업로드
        // Promise 방식
        storageRef?.putFile(photoUri!!)?.continueWithTask { task:Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()
            // 이미지
            contentDTO.imageUrl = uri.toString()
            // uid
            contentDTO.uid = auth?.currentUser?.uid
            // userID
            contentDTO.userId = auth?.currentUser?.email
            // 이미지설명
            contentDTO.explain =
                findViewById<EditText>(R.id.uploadImage_edit_explain).text.toString()
            // 시간
            contentDTO.timestamp = System.currentTimeMillis()

            firestore?.collection("images")?.document()?.set(contentDTO)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

}