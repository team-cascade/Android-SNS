package com.example.android_sns

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.item_detail.view.*
import model.ContentDTO
import model.UserDTO



class DetailViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String ?= null
    var manager : LinearLayoutManager ?= null
    var userDTO : UserDTO ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail_view,container,false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).adapter  = DetailViewRecyclerViewAdapter()
        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).layoutManager = LinearLayoutManager(activity)
        return view
    }
    @SuppressLint("NotifyDataSetChanged")
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUIDList : ArrayList<String> = arrayListOf()
        var userUIDList : ArrayList<String> = arrayListOf()

        // 뉴스 피드를 초기화, 게시글의 시간순으로 정렬
        init {
            firestore?.collection("users")?.document(uid!!)!!.get().addOnSuccessListener {

                userDTO = it.toObject(UserDTO::class.java)

                firestore?.collection("images")?.orderBy("timestamp", Query.Direction.DESCENDING)
                    ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        contentDTOs.clear()
                        contentUIDList.clear()
                        for (snapshot in querySnapshot!!.documents) {
                            val item = snapshot.toObject(ContentDTO::class.java)
                            // 팔로우시에만 뉴스피드에 뜸
                            if(userDTO!!.followings.containsKey(item!!.uid) || item.uid == uid) {
                                contentDTOs.add(item!!)
                                contentUIDList.add(snapshot.id)
                                userUIDList.add(item.uid!!)
                            }
                        }
                        notifyDataSetChanged()
                    }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)


        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        // 게시글 렌더링 메소드
        // 프로필 이미지, 유저 이름, 게시글 사진, 게시글 내용, 좋아요 하트(토글), 댓글, 좋아요 수
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            viewholder.findViewById<TextView>(R.id.detailviewitem_profile_textview).text = contentDTOs!![position].username
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewholder.findViewById(R.id.detailviewitem_imageview_content))

            viewholder.findViewById<TextView>(R.id.detailviewitem_explain_textview).text = contentDTOs!![position].explain

            viewholder.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview).text =
                "좋아요 " + contentDTOs!![position].favoriteCount

            firestore?.collection("users")!!.document(userUIDList[position]).get()?.addOnSuccessListener {
                if(it.get("profileImageUrl") != null)
                    Glide.with(holder.itemView.context).load(it.get("profileImageUrl").toString())
                        .apply(RequestOptions().circleCrop()).into(viewholder.findViewById(R.id.detailviewitem_profile_image))
                else
                    viewholder.findViewById<ImageView>(R.id.detailviewitem_profile_image)
                        .setImageResource(R.drawable.ic_account)
            }

            viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setOnClickListener {
                favoriteEvent(position)
            }
            if(contentDTOs!![position].favorites.containsKey(uid)) {
                viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setImageResource(R.drawable.ic_favorite)

            }else {
                viewholder.findViewById<ImageView>(R.id.detailviewitem_favorite_imageview).setImageResource(R.drawable.ic_favorite_border)
            }

            // 게시글의 댓글을 볼 수 있는 activity
            viewholder.detailviewitem_comment_imageview.setOnClickListener {  v ->
                var intent = Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid",contentUIDList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid.toString())
                startActivity(intent)
            }
        }

        // 게시글 좋아요
        fun favoriteEvent(position: Int) {
            var tsDoc = firestore?.collection("images")?.document(contentUIDList[position])
            firestore?.runTransaction {
                transition ->

                var uid = FirebaseAuth.getInstance().currentUser?.uid
                var contentDTO = transition.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)) {
                    contentDTO.favoriteCount = contentDTO.favoriteCount - 1
                    contentDTO.favorites.remove(uid)
                } else {
                    contentDTO.favoriteCount = contentDTO.favoriteCount + 1
                    contentDTO.favorites[uid!!] = true

                    //알림 보내기
                    favoriteAlarm(contentDTOs[position].uid.toString())
                }
                transition.set(tsDoc,contentDTO)
            }



        }
        // 좋아요 알림을 파이어스토어에 저장
        private fun favoriteAlarm(destinationUid: String) {
            // 알람 데이터 클래스 세팅
            var alarmDTO = UserDTO.AlarmDTO()
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            if(uid == destinationUid) // 자신의 알림은 저장 X
                return
            firestore?.collection("users")?.document(uid.toString())?.get()
                ?.addOnSuccessListener {
                    alarmDTO.uid = uid
                    alarmDTO.destinationUid = destinationUid
                    alarmDTO.username = it.get("username").toString()
                    alarmDTO.kind = 0 // 알람종류: 좋아요
                    alarmDTO.timestamp = System.currentTimeMillis()
                    // FirebaseFirestore 알람 세팅
                    FirebaseFirestore.getInstance().collection("users").document(destinationUid)
                        .collection("alarms").document().set(alarmDTO)
                }
        }

    }

}