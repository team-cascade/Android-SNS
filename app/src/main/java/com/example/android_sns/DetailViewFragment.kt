package com.example.android_sns

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
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
        manager = LinearLayoutManager(activity)
        manager!!.reverseLayout = true
        manager!!.stackFromEnd = true
        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).layoutManager = manager
        return view
    }
    @SuppressLint("NotifyDataSetChanged")
    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUIDList : ArrayList<String> = arrayListOf()
        var userUIDList : ArrayList<String> = arrayListOf()
        init {
            firestore?.collection("users")?.document(uid!!)!!.get().addOnSuccessListener {

                userDTO = it.toObject(UserDTO::class.java)

                firestore?.collection("images")?.orderBy("timestamp")
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
            viewholder.detailviewitem_comment_imageview.setOnClickListener {  v ->
                var intent = Intent(v.context,CommentActivity::class.java)
                intent.putExtra("contentUid",contentUIDList[position])
                intent.putExtra("destinationUid", contentDTOs[position].uid.toString())
                startActivity(intent)
            }
        }
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
        // 좋아요 알람 이벤트 (전달받은 UID)
        private fun favoriteAlarm(destinationUid: String) {
            // 알람 데이터 클래스 세팅
            var alarmDTO = UserDTO.AlarmDTO()
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            if(uid == destinationUid)
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

