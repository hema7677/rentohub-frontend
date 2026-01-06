package com.simats.rentohub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class AdminHomeFragment : Fragment(R.layout.fragment_admin_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnManageProducts = view.findViewById<View>(R.id.btnManageProducts)
        val btnUserPayments = view.findViewById<View>(R.id.btnUserPayments)
        val btnTotalRevenue = view.findViewById<View>(R.id.btnTotalRevenue)
        val btnTotalProducts = view.findViewById<View>(R.id.btnTotalProducts)

        btnManageProducts.setOnClickListener {
            startActivity(Intent(requireContext(), ProductManagementActivity::class.java))
        }

        btnUserPayments.setOnClickListener {
            // Navigate to Payments tab in DashboardActivity or show a fragment
            // For simplicity, let's just show a toast or navigate if possible
            // In many apps, dashboard cards can trigger bottom nav changes
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.nav_payments
        }


        btnTotalProducts.setOnClickListener {
            // Can open the same management activity
            startActivity(Intent(requireContext(), ProductManagementActivity::class.java))
        }

        btnTotalRevenue.setOnClickListener {
            // Future: Open revenue reports
        }
    }
}
