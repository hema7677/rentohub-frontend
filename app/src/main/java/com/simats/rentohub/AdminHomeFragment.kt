package com.simats.rentohub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

class AdminHomeFragment : Fragment(R.layout.fragment_admin_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnManageProducts = view.findViewById<View>(R.id.btnManageProducts)
        val btnAddProduct = view.findViewById<View>(R.id.btnAddProduct)

        btnManageProducts.setOnClickListener {
            startActivity(Intent(requireContext(), ProductManagementActivity::class.java))
        }

        btnAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductActivity::class.java))
        }
    }
}
