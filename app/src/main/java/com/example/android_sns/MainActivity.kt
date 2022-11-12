package com.example.android_sns

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.android_sns.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        bottomNavigationView = binding.bottomNavigation

        supportFragmentManager.beginTransaction().add(R.id.fragment, DetailViewFragment())
            .commit()

        bottomNavigationView?.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.detailViewFragment -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, DetailViewFragment()).commit()
                R.id.searchFragment -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, SearchFragment()).commit()
                R.id.messageFragment -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, MessageFragment()).commit()
                R.id.profileFragment -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, ProfileFragment()).commit()
            }
            true
        } )

        if (Firebase.auth.currentUser == null) {
            startActivity(
                Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
