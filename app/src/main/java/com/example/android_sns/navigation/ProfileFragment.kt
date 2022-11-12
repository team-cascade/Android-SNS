package com.example.android_sns.navigation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.android_sns.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentProfileBinding.bind(view)
    }
}