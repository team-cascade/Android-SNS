package com.example.android_sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.item_comment.view.*
import model.ContentDTO
import model.UserDTO

class CommentActivity : AppCompatActivity() {
    var contentUid: String? = null
    var destinationUid: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")

        var auth = FirebaseAuth.getInstance()
        var firestore = FirebaseFirestore.getInstance()

        comment_recyclerview.adapter = CommentRecyclerviewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        val comment_btn_send = findViewById<Button>(R.id.comment_btn_send)
        comment_btn_send.setOnClickListener {
            var uid = auth.currentUser?.uid
            var comment = ContentDTO.CommentDTO()
            firestore?.collection("users")?.document(uid!!)?.get()?.addOnSuccessListener {
                comment.uid = uid
                comment.username = it.get("username").toString()
                comment.comment = comment_edit_message.text.toString()
                comment.timestamp = System.currentTimeMillis()
                FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                    .collection("comments").document().set(comment)

                commentAlarm(destinationUid!!, comment_edit_message.text.toString())

                comment_edit_message.setText("")
            }
        }

        val comment_btn_back = findViewById<Button>(R.id.comment_btn_back)
        comment_btn_back.setOnClickListener {
            finish()
        }

    }
    fun commentAlarm(destinationUid: String, message: String) {
        // 알람 데이터 클래스 세팅
        var alarmDTO = UserDTO.AlarmDTO()
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        var firestore = FirebaseFirestore.getInstance()
        if(uid == destinationUid)
            return
        firestore?.collection("users")?.document(uid.toString())?.get()
            ?.addOnSuccessListener {
                alarmDTO.uid = uid
                alarmDTO.destinationUid = destinationUid
                alarmDTO.username = it.get("username").toString()
                alarmDTO.kind = 1 // 알람종류: 댓글
                alarmDTO.message = message // 댓글 내용
                alarmDTO.timestamp = System.currentTimeMillis()
                // FirebaseFirestore 알람 세팅
                FirebaseFirestore.getInstance().collection("users").document(destinationUid)
                    .collection("alarms").document().set(alarmDTO)
            }
    }
        inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            var comments: ArrayList<ContentDTO.CommentDTO> = arrayListOf()

            init {
                FirebaseFirestore.getInstance()
                    .collection("images")
                    .document(contentUid!!)
                    .collection("comments")
                    .orderBy("timestamp")
                    .addSnapshotListener { querySnapShot, firebaseFirestoreException ->
                        comments.clear()
                        if (querySnapShot == null)
                            return@addSnapshotListener

                        for (snapshot in querySnapShot.documents!!) {
                            comments.add(snapshot.toObject(ContentDTO.CommentDTO::class.java)!!)
                        }
                        notifyDataSetChanged()
                    }
            }
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                var view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_comment, parent, false)
                return CustomViewHolder(view)
            }

            private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

            override fun getItemCount(): Int {
                return comments.size
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                var view = holder.itemView
                view.commentviewitem_textview_comment.text = comments[position].comment
                view.commentviewitem_textview_profile.text = comments[position].username

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(comments[position].uid!!)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val url = task.result!!["profileImageUrl"]
                            if (url != null)
                                Glide.with(holder.itemView.context).load(url)
                                    .apply(RequestOptions().circleCrop())
                                    .into(view.commentviewitem_imageview_profile)
                        }
                    }
            }
        }
    }

