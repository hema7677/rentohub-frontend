package com.simats.rentohub

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

class AdminPaymentsFragment : Fragment(R.layout.fragment_admin_payments) {
    private lateinit var rvPayments: androidx.recyclerview.widget.RecyclerView
    private lateinit var progressBar: android.widget.ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        rvPayments = view.findViewById(R.id.rvPayments)
        progressBar = view.findViewById(R.id.progressBar)
        rvPayments.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        fetchPayments()
    }

    private fun fetchPayments() {
        progressBar.visibility = View.VISIBLE
        RetrofitClient.api.getAllBookings().enqueue(object : retrofit2.Callback<UserBookingsResponse> {
            override fun onResponse(
                call: retrofit2.Call<UserBookingsResponse>,
                response: retrofit2.Response<UserBookingsResponse>
            ) {
                if (!isAdded) return
                progressBar.visibility = View.GONE
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val payments = response.body()?.data ?: emptyList()
                    rvPayments.adapter = PaymentAdapter(payments)
                } else {
                    context?.let { Toast.makeText(it, "Failed to fetch payments", Toast.LENGTH_SHORT).show() }
                }
            }

            override fun onFailure(call: retrofit2.Call<UserBookingsResponse>, t: Throwable) {
                if (!isAdded) return
                progressBar.visibility = View.GONE
                context?.let { Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show() }
            }
        })
    }
}
