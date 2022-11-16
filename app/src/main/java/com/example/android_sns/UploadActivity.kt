package com.example.android_sns

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage

class UploadActivity : AppCompatActivity() {
    var storage: FirebaseStorage? = null
    var photoUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_upload)
        //Initiate storage
        storage = FirebaseStorage.getInstance()

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
    }

}