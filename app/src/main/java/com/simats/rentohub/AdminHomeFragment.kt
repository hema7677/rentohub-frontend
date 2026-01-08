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
        val rvRecentBookings = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRecentBookings)
        val progressBar = view.findViewById<android.widget.ProgressBar>(R.id.progressBarRecent)

        rvRecentBookings.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        btnManageProducts.setOnClickListener {
            startActivity(Intent(requireContext(), ProductManagementActivity::class.java))
        }

        btnUserPayments.setOnClickListener {
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.nav_payments
        }


        btnTotalProducts.setOnClickListener {
            startActivity(Intent(requireContext(), ProductManagementActivity::class.java))
        }

        btnTotalRevenue.setOnClickListener {
            // Future: Open revenue reports
        }

        // Fetch recent bookings
        fetchRecentBookings(rvRecentBookings, progressBar)
    }

    private fun fetchRecentBookings(rv: androidx.recyclerview.widget.RecyclerView, pb: android.widget.ProgressBar) {
        pb.visibility = View.VISIBLE
        RetrofitClient.api.getAllBookings().enqueue(object : retrofit2.Callback<UserBookingsResponse> {
            override fun onResponse(
                call: retrofit2.Call<UserBookingsResponse>,
                response: retrofit2.Response<UserBookingsResponse>
            ) {
                if (!isAdded) return
                pb.visibility = View.GONE

                if (response.isSuccessful && response.body()?.status == "success") {
                    val remoteBookings = response.body()?.data ?: emptyList()
                    val bookings = remoteBookings.take(5).map {
                        BookingItem(
                            id = (it.booking_id ?: 0).toString(),
                            name = it.equipment_name ?: "Unknown Item",
                            date = it.booking_date ?: "N/A",
                            price = "₹${it.total_amount ?: "0"}",
                            status = it.status ?: "Pending",
                            imageRes = 0,
                            location = it.location ?: "",
                            imageUrl = it.image
                        )
                    }
                    rv.adapter = BookingAdapter(bookings, false) { booking, newStatus ->
                        updateStatus(booking.id, newStatus)
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<UserBookingsResponse>, t: Throwable) {
                if (!isAdded) return
                pb.visibility = View.GONE
            }
        })
    }

    private fun updateStatus(id: String, status: String) {
        RetrofitClient.api.updateBookingStatus(id, status).enqueue(object : retrofit2.Callback<UpdateProductResponse> {
            override fun onResponse(call: retrofit2.Call<UpdateProductResponse>, response: retrofit2.Response<UpdateProductResponse>) {
                if (isAdded && response.isSuccessful) {
                    android.widget.Toast.makeText(requireContext(), "Status Updated", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<UpdateProductResponse>, t: Throwable) {}
        })
    }
}
