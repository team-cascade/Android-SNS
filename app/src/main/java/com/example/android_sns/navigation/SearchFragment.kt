package com.example.android_sns.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.android_sns.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSearchBinding.bind(view)
    }
}