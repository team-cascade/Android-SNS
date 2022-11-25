package com.example.android_sns

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import model.ContentDTO
import model.UserDTO

class CommentActivity : AppCompatActivity() {
    var contentUid : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        contentUid = intent.getStringExtra("contentUid")

        var auth = FirebaseAuth.getInstance()
        var firestore = FirebaseFirestore.getInstance()

        comment_recyclerview.adapter = CommentRecyclerviewAdapter()
        comment_recyclerview.layoutManager = LinearLayoutManager(this)

        val comment_btn_send = findViewById<Button>(R.id.comment_btn_send)
        comment_btn_send.setOnClickListener {
            var uid = auth.currentUser?.uid
            var comment = ContentDTO.CommentDTO()
            firestore?.collection("users")?.document(uid!!)?.get()?.addOnSuccessListener {
                comment.uid = uid
                comment.username = it.get("username").toString()
                comment.comment = comment_edit_message.text.toString()
                Log.w("debug1", comment_edit_message.text.toString())
                comment.timestamp = System.currentTimeMillis()
                FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)
                comment_edit_message.setText("")
            }
        }
    }
    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
       var comments : ArrayList<ContentDTO.CommentDTO> = arrayListOf()
        init {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapShot, firebaseFirestoreException ->
                    comments.clear()
                    if(querySnapShot == null)
                        return@addSnapshotListener

                    for(snapshot in querySnapShot.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.CommentDTO::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

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
                .addOnCompleteListener {  task ->
                    if(task.isSuccessful) {
                        val url = task.result!!["profileImageUrl"]
                        Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.commentviewitem_imageview_profile)
                    }
                }
        }



    }

}