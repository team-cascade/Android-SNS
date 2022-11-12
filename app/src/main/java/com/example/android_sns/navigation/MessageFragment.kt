package com.example.android_sns.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.android_sns.databinding.FragmentMessageBinding

class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMessageBinding.bind(view)
    }
}