package com.simats.rentohub

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.content.Context
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookingsFragment : Fragment() {

    private lateinit var tabUpcoming: TextView
    private lateinit var tabActive: TextView
    private lateinit var tabCompleted: TextView
    private lateinit var rvBookings: RecyclerView
    
    private var allBookings: List<BookingItem> = mutableListOf()
    private var isAdmin: Boolean = false
    private var userId: String? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user info from SharedPreferences
        val sharedPreferences = context?.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        isAdmin = sharedPreferences?.getString("usertype", "") == "admin"
        userId = sharedPreferences?.getString("user_id", "")

        tabUpcoming = view.findViewById(R.id.tabUpcoming)
        tabActive = view.findViewById(R.id.tabActive)
        tabCompleted = view.findViewById(R.id.tabCompleted)
        val btnBack = view.findViewById<View>(R.id.btnBack)
        val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)

        tvHeaderTitle.text = if (isAdmin) "Manage Bookings" else "My Bookings"

        if (!isAdmin) {
            view.findViewById<View>(R.id.tabLayout)?.visibility = View.GONE
        }

        btnBack?.setOnClickListener {
            activity?.onBackPressed()
        }

        rvBookings = view.findViewById(R.id.rvBookings)
        rvBookings.layoutManager = LinearLayoutManager(context)

        // Fetch Bookings from API
        fetchBookings()

        // Tab Click Listeners
        tabUpcoming.setOnClickListener { 
            selectTab(tabUpcoming)
            currentSelectedTab = if (isAdmin) "Upcoming" else "Failed"
            filterBookings(currentSelectedTab)
        }
        tabActive.setOnClickListener { 
            if (isAdmin) {
                selectTab(tabActive)
                currentSelectedTab = "Active"
                filterBookings(currentSelectedTab)
            }
        }
        tabCompleted.setOnClickListener { 
            selectTab(tabCompleted)
            currentSelectedTab = "Completed"
            filterBookings(currentSelectedTab)
        }
    }

    private var currentSelectedTab: String = ""

    private fun fetchBookings() {
        val call = if (isAdmin) {
            RetrofitClient.api.getAllBookings()
        } else {
            RetrofitClient.api.getUserBookings(userId ?: "")
        }

        // Set initial tab if empty
        if (currentSelectedTab.isEmpty()) {
            currentSelectedTab = if (isAdmin) "Upcoming" else "Completed"
        }


        call.enqueue(object : Callback<UserBookingsResponse> {
            override fun onResponse(call: Call<UserBookingsResponse>, response: Response<UserBookingsResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "success") {
                        val remoteBookings = body.data ?: emptyList()
                        

                        if (remoteBookings.isEmpty()) {
                            allBookings = emptyList()
                            filterBookings(currentSelectedTab) 
                            return
                        }
                        // Map remote bookings to local BookingItem
                        allBookings = remoteBookings.map {
                            BookingItem(
                                id = (it.booking_id ?: 0).toString(),
                                name = it.equipment_name ?: "Unknown Item",
                                date = it.booking_date ?: "N/A",
                                price = "₹${it.total_amount ?: "0"}",
                                status = it.status ?: "Pending", 
                                imageRes = 0, 
                                location = it.location ?: "No location",
                                imageUrl = it.image
                            )
                        }
                        
                        filterBookings(currentSelectedTab)

                    } else {
                        val msg = body?.message ?: "Unknown server error"
                        context?.let { Toast.makeText(it, "Server: $msg", Toast.LENGTH_LONG).show() }
                        allBookings = emptyList()
                        filterBookings(currentSelectedTab)
                    }
                } else {
                    context?.let {
                        Toast.makeText(it, "Error Code: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                    allBookings = emptyList()
                    filterBookings(currentSelectedTab)
                }
            }

            override fun onFailure(call: Call<UserBookingsResponse>, t: Throwable) {
                allBookings = emptyList()
                filterBookings(currentSelectedTab)
                context?.let {
                    Toast.makeText(it, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }


    private fun updateBookingStatus(bookingId: String, newStatus: String) {
        RetrofitClient.api.updateBookingStatus(bookingId, newStatus)
            .enqueue(object : Callback<UpdateProductResponse> {
                override fun onResponse(call: Call<UpdateProductResponse>, response: Response<UpdateProductResponse>) {
                    if (response.isSuccessful) {
                        context?.let { Toast.makeText(it, "Status updated to $newStatus", Toast.LENGTH_SHORT).show() }
                        fetchBookings() // Refresh list
                    } else {
                        context?.let { Toast.makeText(it, "Update failed", Toast.LENGTH_SHORT).show() }
                    }
                }

                override fun onFailure(call: Call<UpdateProductResponse>, t: Throwable) {
                    context?.let { Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show() }
                }
            })
    }

    private fun filterBookings(tab: String) {
        val filtered = if (isAdmin) {
            when (tab) {
                "Upcoming" -> allBookings.filter { 
                    it.status.isNullOrEmpty() ||
                    it.status.equals("Confirmed", true) || 
                    it.status.equals("Pending", true) || 
                    it.status.equals("Upcoming", true) 
                }
                "Active" -> allBookings.filter { 
                    it.status.equals("Active", true) || 
                    it.status.equals("Delivered", true) 
                }
                "Completed" -> allBookings.filter { 
                    it.status.equals("Completed", true) || 
                    it.status.equals("Cancelled", true) 
                }
                else -> allBookings
            }
        } else {
            // User side filtering: Only show Completed
            when (tab) {
                "Completed" -> allBookings.filter { it.status.equals("Completed", true) }
                else -> allBookings.filter { it.status.equals("Completed", true) }
            }
        }
        
        rvBookings.adapter = BookingAdapter(filtered, isAdmin) { booking, newStatus ->
            updateBookingStatus(booking.id, newStatus)
        }
    }

    private fun selectTab(selectedTab: TextView) {
        val tabs = listOf(tabUpcoming, tabActive, tabCompleted)
        tabs.forEach { tab ->
            tab.setBackgroundResource(R.drawable.bg_booking_tab_unselected)
            tab.setTextColor(android.graphics.Color.parseColor("#BDBDBD"))
            tab.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        selectedTab.setBackgroundResource(R.drawable.bg_booking_tab_selected)
        selectedTab.setTextColor(android.graphics.Color.parseColor("#0A1931"))
        selectedTab.setTypeface(null, android.graphics.Typeface.BOLD)
    }
}

