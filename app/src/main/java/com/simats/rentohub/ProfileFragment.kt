package com.simats.rentohub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tvDisplayName)
        val tvDOB = view.findViewById<TextView>(R.id.tvDisplayDOB)
        val tvGender = view.findViewById<TextView>(R.id.tvDisplayGender)
        val tvPhone = view.findViewById<TextView>(R.id.tvDisplayPhone)
        val btnLogout = view.findViewById<AppCompatButton>(R.id.btnLogout)
        val btnEditProfile = view.findViewById<View>(R.id.btnEditProfile)

        // Load data from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        
        tvName.text = sharedPref.getString("user_name", "John Doe")
        tvDOB.text = sharedPref.getString("user_dob", "01/01/1995")
        tvGender.text = sharedPref.getString("user_gender", "Male")
        tvPhone.text = sharedPref.getString("user_phone", "+91 9876543210")

        btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), FillProfileActivity::class.java)
            intent.putExtra("is_edit", true)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
