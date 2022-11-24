package com.example.android_sns

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import model.ContentDTO
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment(userId: String?) : Fragment() {

    lateinit var fragmentView : View
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid: String? = null

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_profile, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = arguments?.getString("destinationUid")

        currentUserUid = auth!!.currentUser?.uid
        if(uid==null || uid=="") {
            uid = auth!!.currentUser?.uid
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        }
        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity,3)

        ////////////////   프로필 설정 부분   ///////////////////
        var photoUri: Uri? = null
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storage = FirebaseStorage.getInstance()
        var storageRef = storage?.reference?.child("profiles/uid")?.child(imageFileName)

        // 콜백함수
        val getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    photoUri = it.data?.data

                    //파일 업로드
                    // Promise 방식
                    storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                        return@continueWithTask storageRef.downloadUrl
                    }?.addOnSuccessListener { uri ->
                        var contentDTO = ContentDTO()
                        // 이미지
                        contentDTO.imageUrl = uri.toString()
                        // uid
                        contentDTO.uid = auth?.currentUser?.uid
                        // userID
                        contentDTO.userId = auth?.currentUser?.email
                        // 시간
                        contentDTO.timestamp = System.currentTimeMillis()
                        contentDTO.uid?.let { it1 -> firestore?.collection("profiles")?.document(it1)?.set(contentDTO) }
                    }

                }
            }
        fragmentView.edit_profile.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            getResult.launch(photoPickerIntent)
        }


        // 프로필사진 가져오기
        var contentDTOp : ArrayList<ContentDTO> = arrayListOf()
        firestore?.collection("profiles")?.whereEqualTo("uid", currentUserUid)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (querySnapshot == null) {
                    return@addSnapshotListener}
                //Get data
                contentDTOp.clear()
                for (snapshot in querySnapshot.documents) {
                    contentDTOp.add(snapshot.toObject(ContentDTO::class.java)!!)
                    fragmentView?.account_iv_profile?.context?.let { Glide.with(it).load(contentDTOp[0].imageUrl?.toUri())
                        .into(fragmentView.account_iv_profile) }
                }
            }
        return fragmentView
    }
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.whereEqualTo("uid", currentUserUid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    //Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) {
                        return@addSnapshotListener}
                    //Get data
                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var imageview = ImageView(p0.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CustomViewHolder(imageview)
        }
        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {

        }
        override fun getItemCount(): Int {
            return contentDTOs.size
        }
        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var imageview = (p0 as CustomViewHolder).imageview
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }
    }
}