package com.example.android_sns

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.ContentDTO

class SearchFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    var uid : String ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_search,container,false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

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
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUIDList : ArrayList<String> = arrayListOf()
        lateinit var mainActivity: MainActivity

        fun setActivity(mainActivity: MainActivity) {
            this.mainActivity = mainActivity
        }
        init {
            firestore?.collection("profiles")?.orderBy("username")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUIDList.clear()
                    for(snapshot in querySnapshot!!.documents) {
                        val item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUIDList.add(snapshot.id)
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
            return contentDTOs.size
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            viewholder.findViewById<TextView>(R.id.search_item_profile_textview).text = contentDTOs!![position].username

            Glide.with(holder.itemView.context).load(contentDTOs!![position].profileImageUrl)
                .into(viewholder.findViewById(R.id.search_item_profile_image))


//            viewholder.setOnClickListener { activity?.getSupportFragmentManager()?.beginTransaction()
//                ?.replace(R.id.fragment, ProfileFragment())?.commit() }
            viewholder.setOnClickListener { mainActivity.goProfileFragment(contentDTOs!![position].uid) }
        }

        @SuppressLint("SuspiciousIndentation")
        fun searchMethod(searchName: String) {
            firestore?.collection("profiles")?.orderBy("username")
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    contentDTOs.clear()
                    contentUIDList.clear()
                    for(snapshot in querySnapshot!!.documents) {
                        val item = snapshot.toObject(ContentDTO::class.java)
                        if(item?.username?.contains(searchName) == true)
                        contentDTOs.add(item!!)
                        contentUIDList.add(snapshot.id)
                    }
                    notifyDataSetChanged()
                }
        }

    }
}