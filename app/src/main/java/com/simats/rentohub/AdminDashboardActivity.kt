package com.simats.rentohub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Initial Fragment
        loadFragment(AdminHomeFragment())

        // 🔹 Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(AdminHomeFragment())
                    true
                }
                R.id.nav_add_product -> {
                    // You could either navigate to activity or fragment. 
                    // Keeping it as fragment if you have AddProductFragment, 
                    // but for now, navigating to activity as before or swapping fragment.
                    // For consistency with user side, let's load fragments.
                    // If no AddProductFragment, we can keep the activity start but it might break the "bottom nav" feel.
                    // Let's stick to Profile for now.
                    true
                }
                R.id.nav_booking -> {
                    loadFragment(BookingsFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
    }
}
