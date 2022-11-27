package com.example.android_sns

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_alarm.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import model.UserDTO

class AlarmFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)

        // 리사이클러 뷰 어댑터 연결
        view.alarm_fragment_recyclerview.adapter = AlarmRecyclerViewAdapter()
        view.alarm_fragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }

    // 리사이클러뷰 어댑터
    inner class AlarmRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        private val alarmDTOList: ArrayList<UserDTO.AlarmDTO> = arrayListOf()

        // 알람 데이터 초기화
        init {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid).collection("alarms").get()
                .addOnSuccessListener {
                    alarmDTOList.clear()
                    if(it == null)return@addOnSuccessListener

                    for (snapshot in it.documents) {
                        alarmDTOList.add(snapshot.toObject(UserDTO.AlarmDTO::class.java)!!)
                    }
                    alarmDTOList.sortByDescending { it.timestamp }

                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = holder.itemView

            // 홀더 프로필 이미지
            val profileImage = viewHolder.commentviewitem_imageview_profile
            // 홀더 유저 ID
            val commentProfileTextView = viewHolder.commentviewitem_textview_profile
            // 홀더 댓글 텍스트
            val commentTextView = viewHolder.commentviewitem_textview_comment

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
                            profileImage.setImageResource(R.drawable.ic_account)
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