package com.simats.rentohub

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

class AdminPaymentsFragment : Fragment(R.layout.fragment_admin_payments) {
    private lateinit var rvPayments: androidx.recyclerview.widget.RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        rvPayments = view.findViewById(R.id.rvPayments)
        rvPayments.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        fetchPayments()
    }

    private fun fetchPayments() {
        RetrofitClient.api.getAllBookings().enqueue(object : retrofit2.Callback<UserBookingsResponse> {
            override fun onResponse(
                call: retrofit2.Call<UserBookingsResponse>,
                response: retrofit2.Response<UserBookingsResponse>
            ) {
                if (!isAdded) return
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val payments = response.body()?.data ?: emptyList()
                    rvPayments.adapter = PaymentAdapter(payments)
                } else {
                    context?.let { Toast.makeText(it, "Failed to fetch payments", Toast.LENGTH_SHORT).show() }
                }
            }

            override fun onFailure(call: retrofit2.Call<UserBookingsResponse>, t: Throwable) {
                if (!isAdded) return
                context?.let { Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show() }
            }
        })
    }
}
