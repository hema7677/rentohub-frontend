package com.simats.rentohub

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_chats -> ChatFragment()
                R.id.nav_booking -> BookingsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> null
            }
            
            fragment?.let {
                loadFragment(it)
                true
            } ?: false
        }

        // Only load if this is the first time (savedInstanceState is null)
        if (savedInstanceState == null) {
            val navigateTo = intent.getStringExtra("NAVIGATE_TO")
            if (navigateTo == "BOOKINGS") {
                bottomNav.selectedItemId = R.id.nav_booking
            } else {
                bottomNav.selectedItemId = R.id.nav_home
            }
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
