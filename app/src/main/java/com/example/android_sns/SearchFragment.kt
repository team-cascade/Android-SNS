package com.example.android_sns

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.ContentDTO
import model.UserDTO

class SearchFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var currentUserUID : String ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_search,container,false)

        firestore = FirebaseFirestore.getInstance()
        currentUserUID = FirebaseAuth.getInstance().currentUser?.uid

        val searchRecyclerViewAdapter = SearchRecyclerViewAdapter()

        view.findViewById<RecyclerView>(R.id.searchRecyclerView).adapter  = searchRecyclerViewAdapter
        view.findViewById<RecyclerView>(R.id.searchRecyclerView).layoutManager = LinearLayoutManager(activity)

        searchRecyclerViewAdapter.setActivity(activity as MainActivity)

        val searchName = view.findViewById<EditText>(R.id.searchName)
        val searchButton = view.findViewById<Button>(R.id.searchButton)

        searchButton.setOnClickListener {
            searchRecyclerViewAdapter.searchMethod(searchName.text.toString())
        }

        return view
    }
    @SuppressLint("NotifyDataSetChanged")
    inner class SearchRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var userDTOs : ArrayList<UserDTO> = arrayListOf()
        var userUIDList : ArrayList<String> = arrayListOf()
        lateinit var mainActivity: MainActivity

        fun setActivity(mainActivity: MainActivity) {
            this.mainActivity = mainActivity
        }
        init {
            firestore?.collection("users")?.orderBy("username")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    userDTOs.clear()
                    userUIDList.clear()
                    for(snapshot in querySnapshot!!.documents) {
                        val item = snapshot.toObject(UserDTO::class.java)
                        if(item!!.uid != currentUserUID) {
                            userDTOs.add(item!!)
                            userUIDList.add(item.uid!!)
                        }
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.search_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return userDTOs.size
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            viewholder.findViewById<TextView>(R.id.search_item_profile_textview).text = userDTOs!![position].username

            if(userDTOs[position].profileImageUrl != null)
                    Glide.with(holder.itemView.context).load(userDTOs[position].profileImageUrl)
                        .apply(RequestOptions().circleCrop()).into(viewholder.findViewById(R.id.search_item_profile_image))
            else
                viewholder.findViewById<ImageView>(R.id.search_item_profile_image)
                    .setImageResource(R.drawable.ic_account)

            viewholder.setOnClickListener { mainActivity.goProfileFragment(userDTOs!![position].uid) }
        }

        @SuppressLint("SuspiciousIndentation")
        fun searchMethod(searchName: String) {
            firestore?.collection("users")?.orderBy("username")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    userDTOs.clear()
                    userUIDList.clear()
                    for(snapshot in querySnapshot!!.documents) {
                        val item = snapshot.toObject(UserDTO::class.java)
                        if(item?.username!!.lowercase()?.contains(searchName.lowercase()) == true) {
                            if(item.uid != currentUserUID) {
                                userDTOs.add(item!!)
                                userUIDList.add(snapshot.id)
                            }
                        }
                    }
                    notifyDataSetChanged()
                }
        }

    }
}