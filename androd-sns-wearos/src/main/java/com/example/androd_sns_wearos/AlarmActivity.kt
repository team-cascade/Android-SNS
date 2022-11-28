package com.example.androd_sns_wearos

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.androd_sns_wearos.databinding.ActivityAlarmBinding
import com.example.androd_sns_wearos.databinding.ActivityMainBinding
import com.example.androd_sns_wearos.databinding.ItemCommentBinding
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.UserDTO
import java.lang.Thread.sleep

class AlarmActivity : Activity() {

    private lateinit var binding: ActivityAlarmBinding
    var currentUserUid : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentUserUid = FirebaseAuth.getInstance().currentUser!!.uid

        binding.alarmRecyclerview.adapter = AlarmRecyclerViewAdapter()
        binding.alarmRecyclerview.layoutManager = LinearLayoutManager(this)
    }

    // 리사이클러뷰 어댑터
    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        private val alarmDTOList: ArrayList<UserDTO.AlarmDTO> = arrayListOf()

        // 알람 데이터 초기화
        init {
            val uid = currentUserUid
            if (uid != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid).collection("alarms").get()
                    .addOnSuccessListener {
                        alarmDTOList.clear()
                        if (it == null) return@addOnSuccessListener

                        for (snapshot in it.documents) {
                            alarmDTOList.add(snapshot.toObject(UserDTO.AlarmDTO::class.java)!!)
                        }
                        alarmDTOList.sortByDescending { it.timestamp }

                        notifyDataSetChanged()
                    }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = ItemCommentBinding.inflate(layoutInflater).root
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = holder.itemView

            // 홀더 프로필 이미지
            val profileImage = viewHolder.findViewById<ImageView>(R.id.commentviewitem_imageview_profile)
            // 홀더 유저 ID
            val commentProfileTextView = viewHolder.findViewById<TextView>(R.id.commentviewitem_textview_profile)
            // 홀더 댓글 텍스트
            val commentTextView = viewHolder.findViewById<TextView>(R.id.commentviewitem_textview_comment)

            // 유저 프로필 이미지
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(alarmDTOList[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result?.get("profileImageUrl")

                        // 찾아올 유저 프로필 이미지가 null일 경우 기본 프로필 이미지 세팅
                        if (url == null) {
                            profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                        } else {
                            Glide.with(holder.itemView.context)
                                .load(url)
                                .apply(RequestOptions().circleCrop()).into(profileImage)
                        }
                    }
                }

            // 알람 종류에 따른 텍스트 표시
            when (alarmDTOList[position].kind){
                // 좋아요
                0 -> {
                    val favoriteTxt = alarmDTOList[position].username + " " + getString(R.string.alarm_favorite)
                    commentProfileTextView.text = favoriteTxt
                }
                // 댓글
                1 -> {
                    val commentTxt = alarmDTOList[position].username + " 님이 \"" + alarmDTOList[position].message +"\" " + getString(R.string.alarm_comment)
                    commentProfileTextView.text = commentTxt
                }
                // 팔로우
                2 -> {
                    val followTxt = alarmDTOList[position].username + " " +getString(R.string.alarm_follow)
                    commentProfileTextView.text = followTxt
                }
            }
            commentTextView.visibility = View.INVISIBLE
        }
    }
}