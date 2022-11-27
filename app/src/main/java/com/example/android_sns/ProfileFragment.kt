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
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import model.ContentDTO
import kotlinx.android.synthetic.main.fragment_profile.view.*
import model.UserDTO
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment() : Fragment() {

    lateinit var fragmentView : View
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUID: String? = null
    var currentUserDTO: UserDTO? = null

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_profile, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        uid = arguments?.getString("destinationUid") // 인자로 넘어온 uid, auth의 currentUid와 같을 경우 자신의 프로필
        currentUserUID = auth!!.currentUser?.uid

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity,3)

        // 버튼 설정
        // 인자 UID와 auth의 currentUID가 같을 경우 자신의 프로필 : 프로필 편집, 로그아웃 버튼
        // 다를 경우 검색창에서 넘어온 유저의 프로필 : 팔로우 버튼
        firestore!!.collection("users")?.document(currentUserUID!!)!!.get().addOnSuccessListener {
            currentUserDTO = it.toObject(UserDTO::class.java)
        }.addOnSuccessListener {
            if(currentUserUID == uid) {
                fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
                fragmentView?.account_btn_follow_signout?.setOnClickListener {
                    activity?.finish()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    auth?.signOut()
                }
            }
            else {
                fragmentView?.account_btn_follow_signout?.setOnClickListener {
                    followEvent(uid!!)
                }
                if(currentUserDTO!!.followings.containsKey(uid))
                    fragmentView.account_btn_follow_signout.text = "팔로우 취소"
            }
        }

        ////////////////   프로필 설정 부분   ///////////////////
        var photoUri: Uri? = null
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storage = FirebaseStorage.getInstance()
        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 파이어스토어에서 팔로워 수, 팔로잉 수, 유저 이름 데이터를 받아와서 갱신
        var userDTO: UserDTO? = null
        firestore?.collection("users")?.document(uid!!)?.get()
            ?.addOnSuccessListener {
                userDTO = it.toObject(UserDTO::class.java)!!
                fragmentView.account_tv_follower_count.text = userDTO?.followerCount.toString()
                fragmentView.account_tv_following_count.text = userDTO?.followingCount!!.toString()
                fragmentView.account_tv_username.text = userDTO?.username
            }

        // 프로필 이미지 편집 콜백 함수
        val getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    photoUri = it.data?.data

                    //파일 업로드
                    // Promise 방식
                    storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                        return@continueWithTask storageRef.downloadUrl
                    }?.addOnSuccessListener { uri ->

                        // 이미지
                        // uid
                        // username

                        userDTO?.profileImageUrl = uri.toString()
                        userDTO?.uid = auth?.currentUser?.uid
                        userDTO?.uid?.let { it1 -> firestore?.collection("users")?.document(it1)?.set(
                            userDTO!!
                        ) }
                        getProfileImage()
                    }

                }
            }

        // 프로필 이미지를 편집
        if(currentUserUID == uid) {
            fragmentView.edit_profile.setOnClickListener {
                var photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                getResult.launch(photoPickerIntent)
            }
        }
        else {
            fragmentView.edit_profile.visibility = View.INVISIBLE
        }
        getProfileImage()
        // 프로필사진 가져오기
        return fragmentView
    }
    // 팔로우 시 해당 유저의 팔로워 데이터와 현재 유저의 팔로우 데이터 갱신
    fun followEvent(uid: String) {
        var tsDoc = firestore?.collection("users")?.document(uid)
        var userDTO: UserDTO ?= null

        firestore?.runTransaction { transition ->
            userDTO = transition.get(tsDoc!!).toObject(UserDTO::class.java)
            if (userDTO!!.followers.containsKey(currentUserUID)) {
                userDTO!!.followerCount = userDTO!!.followerCount - 1
                userDTO!!.followers.remove(currentUserUID)
                currentUserDTO!!.followingCount -= 1
                currentUserDTO!!.followings.remove(uid)
                fragmentView.account_btn_follow_signout.text = "팔로우"

                // 팔로우 알람
            } else {
                userDTO!!.followerCount = userDTO!!.followerCount + 1
                userDTO!!.followers[currentUserUID!!] = true
                currentUserDTO!!.followingCount += 1
                currentUserDTO!!.followings[uid] = true
                fragmentView.account_btn_follow_signout.text = "팔로우 취소"
                followAlarm(uid)
            }
            transition.set(tsDoc!!, userDTO!!)
        }!!.addOnSuccessListener {
            fragmentView.account_tv_follower_count.text = userDTO?.followerCount.toString()
            firestore!!.collection("users").document(currentUserUID!!).set(currentUserDTO!!)
        }
    }

    //팔로우시 알림 생성
    fun followAlarm(destinationUid: String) {
        // 알람 데이터 클래스 세팅
        var alarmDTO = UserDTO.AlarmDTO()
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        var firestore = FirebaseFirestore.getInstance()
        firestore?.collection("users")?.document(uid.toString())?.get()
            ?.addOnSuccessListener {
                alarmDTO.uid = uid
                alarmDTO.destinationUid = destinationUid
                alarmDTO.username = it.get("username").toString()
                alarmDTO.kind = 2 // 알람종류: 팔로우
                alarmDTO.timestamp = System.currentTimeMillis()
                // FirebaseFirestore 알람 세팅
                FirebaseFirestore.getInstance().collection("users").document(destinationUid)
                    .collection("alarms").document().set(alarmDTO)
            }
    }
    // 프로필 이미지를 가져옴
    fun getProfileImage() {
        var userDTO: UserDTO? = null
        firestore?.collection("users")?.document(uid!!)?.get()?.addOnSuccessListener {
            userDTO = it.toObject(UserDTO::class.java)!!
            if(userDTO!!.profileImageUrl != null)
                fragmentView?.account_iv_profile?.context?.let { Glide.with(it).load(userDTO!!.profileImageUrl?.toUri())
                    .apply(RequestOptions().circleCrop()).into(fragmentView.account_iv_profile) }
        }
    }
    // 유저의 게시글을 보여주는 리사이클러뷰
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()

        // 유저와 uid가 일치하는 게시글을 찾아서 초기화
        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)!!.orderBy("timestamp", Query.Direction.DESCENDING)
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

        // 유저의 게시글을 가로 3칸으로 나눠서 보여줌
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