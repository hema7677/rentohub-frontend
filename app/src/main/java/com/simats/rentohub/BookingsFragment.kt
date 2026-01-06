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
        val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        isAdmin = sharedPreferences.getString("usertype", "") == "admin"
        userId = sharedPreferences.getString("user_id", "")

        tabUpcoming = view.findViewById(R.id.tabUpcoming)
        tabActive = view.findViewById(R.id.tabActive)
        tabCompleted = view.findViewById(R.id.tabCompleted)
        val btnBack = view.findViewById<View>(R.id.btnBack)
        val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)

        tvHeaderTitle.text = if (isAdmin) "Manage Bookings" else "My Bookings"

        btnBack?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        rvBookings = view.findViewById(R.id.rvBookings)
        rvBookings.layoutManager = LinearLayoutManager(requireContext())

        // Fetch Bookings from API
        fetchBookings()

        // Tab Click Listeners
        tabUpcoming.setOnClickListener { 
            selectTab(tabUpcoming)
            filterBookings("Upcoming")
        }
        tabActive.setOnClickListener { 
            selectTab(tabActive)
            filterBookings("Active")
        }
        tabCompleted.setOnClickListener { 
            selectTab(tabCompleted)
            filterBookings("Completed")
        }
    }

    private fun fetchBookings() {
        val call = if (isAdmin) {
            RetrofitClient.api.getAllBookings()
        } else {
            RetrofitClient.api.getUserBookings(userId ?: "")
        }

        call.enqueue(object : Callback<UserBookingsResponse> {
            override fun onResponse(call: Call<UserBookingsResponse>, response: Response<UserBookingsResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val remoteBookings = response.body()?.data ?: emptyList()
                    if (remoteBookings.isEmpty()) {
                        loadHardcodedData()
                        return
                    }
                    // Map remote bookings to local BookingItem
                    allBookings = remoteBookings.map {
                        BookingItem(
                            id = it.booking_id.toString(),
                            name = it.equipment_name,
                            date = it.booking_date,
                            price = "₹${it.total_amount}",
                            status = it.status,
                            imageRes = 0, 
                            address = "",
                            imageUrl = it.image
                        )
                    }
                    // Refresh current tab
                    val currentTab = when {
                        tabUpcoming.typeface?.isBold == true -> "Upcoming"
                        tabActive.typeface?.isBold == true -> "Active"
                        else -> "Completed"
                    }
                    filterBookings(currentTab)
                } else {
                    loadHardcodedData()
                }
            }

            override fun onFailure(call: Call<UserBookingsResponse>, t: Throwable) {
                // Fallback to hardcoded for demo
                loadHardcodedData()
            }
        })
    }

    private fun loadHardcodedData() {
        allBookings = listOf(
            BookingItem("ID: 1001", "Sony Alpha IV", "Jan 10, 2024", "₹3,500", "Pending", R.drawable.camera, "62 Beach Rd, Chennai"),
            BookingItem("ID: 1002", "Canon EOS R5", "Jan 11, 2024", "₹4,200", "Pending", R.drawable.camera, "12 Anna Salai, Chennai"),
            BookingItem("ID: 1003", "DJI Mavic 3 Drone", "Jan 12, 2024", "₹5,000", "Confirmed", R.drawable.camera, "45 OMR Road, Chennai"),
            BookingItem("ID: 1004", "Nikcon Z6 II", "Jan 05, 2024", "₹2,500", "Active", R.drawable.camera, "GST Road, St. Thomas Mount"),
            BookingItem("ID: 1005", "Sony 85mm f/1.8", "Jan 04, 2024", "₹1,200", "Delivered", R.drawable.lens, "T-Nagar Market, Chennai"),
            BookingItem("ID: 1006", "Godox Ring Light", "Jan 15, 2024", "₹800", "Upcoming", R.drawable.ic_ring_light, "Velachery Main Rd"),
            BookingItem("ID: 1007", "Heavy Duty Tripod", "Jan 02, 2024", "₹500", "Completed", R.drawable.tripod, "Adyar Flyover, Chennai"),
            BookingItem("ID: 1008", "Rode VideoMic", "Jan 01, 2024", "₹600", "Cancelled", R.drawable.camera, "Mount Road, Chennai")
        )
        // Refresh display based on current selection
        val currentTab = when {
            tabUpcoming.typeface?.isBold == true -> "Upcoming"
            tabActive.typeface?.isBold == true -> "Active"
            else -> "Completed"
        }
        filterBookings(currentTab)
    }

    private fun updateBookingStatus(bookingId: String, newStatus: String) {
        RetrofitClient.api.updateBookingStatus(bookingId, newStatus)
            .enqueue(object : Callback<UpdateProductResponse> {
                override fun onResponse(call: Call<UpdateProductResponse>, response: Response<UpdateProductResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                        fetchBookings() // Refresh list
                    } else {
                        Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UpdateProductResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterBookings(tab: String) {
        val filtered = when (tab) {
            "Upcoming" -> allBookings.filter { 
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

