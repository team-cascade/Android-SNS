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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import model.ContentDTO
import java.util.*

// 게시물 업로드 액티비티
class UploadActivity : AppCompatActivity() {
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //앨범에서 사진 선택 시 콜백 함수
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

        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    // 사진 업로드
    fun contentUpload() {
        // 파일명 생성
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        //파이어베이스 스토리지 사진 업로드
        storageRef?.putFile(photoUri!!)?.continueWithTask { task:Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri -> // 성공하면 파이어스토어에 게시글 정보 업로드 후 액티비티 종료
            var contentDTO: ContentDTO
            firestore?.collection("users")?.document(auth?.currentUser?.uid.toString())?.get()
                ?.addOnSuccessListener {
                    contentDTO = it.toObject(ContentDTO::class.java)!!
                    contentDTO.timestamp = System.currentTimeMillis()
                    contentDTO.imageUrl = uri.toString()
                    contentDTO.explain =
                        findViewById<EditText>(R.id.uploadImage_edit_explain).text.toString()
                    firestore?.collection("images")?.document()?.set(contentDTO)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
        }
    }

}