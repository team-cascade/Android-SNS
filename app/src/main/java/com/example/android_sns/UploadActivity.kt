package com.example.android_sns

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android_sns.databinding.FragmentUploadBinding
import com.google.firebase.storage.FirebaseStorage

class UploadActivity : AppCompatActivity() {
    // var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_upload)
        val binding = FragmentUploadBinding.inflate(layoutInflater)
        //Initiate storage
        storage = FirebaseStorage.getInstance()

        //콜백 함수
        val getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    photoUri = it.data?.data
                  //  Log.w("mydebug" , photoUri.toString())
                  //  val source = ImageDecoder.createSource(this.contentResolver, photoUri)
//                    try {
//                        val bitmap = ImageDecoder.decodeBitmap(source)
//                        binding.uploadImage.setImageBitmap(bitmap)
//                    }
//                    catch(e: Exception) {e.printStackTrace()}
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