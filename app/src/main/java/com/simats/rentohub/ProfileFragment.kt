package com.simats.rentohub

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var ivProfile: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName = view.findViewById(R.id.tvDisplayName)
        tvEmail = view.findViewById(R.id.tvDisplayEmail)
        tvPhone = view.findViewById(R.id.tvDisplayPhone)
        ivProfile = view.findViewById(R.id.ivProfilePic)
        val btnBack = view.findViewById<View>(R.id.btnBack)
        val btnEdit = view.findViewById<View>(R.id.btnEdit)

        updateProfileUI()

        // In the image provided, the back button IS visible.
        // if (requireActivity() is UserMainActivity || requireActivity() is AdminDashboardActivity || requireActivity() is UserActivity) {
        //    btnBack.visibility = View.GONE
        // }

        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        btnEdit.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        // Booked Order - Shows fake order history
        setupOption(view.findViewById(R.id.optionBookedOrder), R.drawable.ic_booking_check, "Booked Order") {
            startActivity(Intent(requireContext(), OrdersActivity::class.java))
        }

        // About Us
        setupOption(view.findViewById(R.id.optionAboutUs), R.drawable.ic_info, "About Us") {
            val intent = Intent(requireContext(), InfoPageActivity::class.java)
            intent.putExtra("type", "about")
            startActivity(intent)
        }

        // Language Select
        setupOption(view.findViewById(R.id.optionLanguage), R.drawable.ic_globe, "Language") {
            showLanguageDialog()
        }

        setupOption(view.findViewById(R.id.optionHelpCentre), R.drawable.ic_help, "Help Centre") {
            startActivity(Intent(requireContext(), HelpCenterActivity::class.java))
        }

        // Privacy Policy
        setupOption(view.findViewById(R.id.optionPrivacyPolicy), R.drawable.ic_privacy, "Privacy Policy") {
            val intent = Intent(requireContext(), InfoPageActivity::class.java)
            intent.putExtra("type", "privacy")
            startActivity(intent)
        }

        setupOption(view.findViewById(R.id.optionLogout), R.drawable.ic_logout, "Log out") {
            startActivity(Intent(requireContext(), LogoutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateProfileUI() // Refresh UI when coming back from Edit
    }

    private fun updateProfileUI() {
        if (!isAdded) return
        try {
            val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            
            val name = sharedPreferences.getString("user_name", "Pooja Patel")
            val email = sharedPreferences.getString("user_email", "poojabhaipooja@gmail.com")
            val phone = sharedPreferences.getString("user_phone", "+91 8448484848")
            val imageUri = sharedPreferences.getString("user_image", null)

            tvName.text = name
            tvEmail.text = email
            tvPhone.text = phone
            
            if (imageUri != null && imageUri.isNotEmpty()) {
                Glide.with(this)
                    .load(android.net.Uri.parse(imageUri))
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .into(ivProfile)
            } else {
                ivProfile.setImageResource(R.drawable.profile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Telugu (తెలుగు)", "Hindi (हिन्दी)")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                Toast.makeText(requireContext(), "Selected: ${languages[which]}", Toast.LENGTH_SHORT).show()
                // In a real app, you would change the Locale here.
            }
            .show()
    }

    private fun showFakeOrders() {
        val orders = "1. Sony A7 III - Confirmed\n2. Tripod Stand - Delivered\n3. 85mm Lens - Returned\n4. Ring Light - Processing"
        AlertDialog.Builder(requireContext())
            .setTitle("My Bookings")
            .setMessage(orders)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun setupOption(view: View, iconRes: Int, title: String, onClick: () -> Unit) {
        val ivIcon = view.findViewById<ImageView>(R.id.ivOptionIcon)
        val tvTitle = view.findViewById<TextView>(R.id.tvOptionTitle)
        ivIcon.setImageResource(iconRes)
        tvTitle.text = title
        view.setOnClickListener { onClick() }
    }

    private fun logout() {
        val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        val intent = Intent(requireContext(), WelcomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
