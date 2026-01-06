package com.simats.rentohub

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var equipmentAdapter: EquipmentAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    
    private lateinit var tvLocation: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 200

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCategories = view.findViewById(R.id.rvCategories)
        rvProducts = view.findViewById(R.id.rvProducts)
        val helpCard: View = view.findViewById(R.id.helpCard)
        val fabChat: View = view.findViewById(R.id.fab_chat)
        val btnNotification: View = view.findViewById(R.id.btnNotification)
        val btnFilter: View = view.findViewById(R.id.btnFilter)
        val tvWelcome: android.widget.TextView = view.findViewById(R.id.tvWelcome)
        tvLocation = view.findViewById(R.id.tvLocation)
        val profileImage: View = view.findViewById(R.id.profileImage)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Load name from session
        val sharedPreferences = context?.getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val userName = sharedPreferences?.getString("user_name", "User")
        tvWelcome.text = "Hey $userName!" 
        
        // Fetch Live Location
        checkLocationPermissionAndFetch()

        btnNotification.setOnClickListener {
            context?.let {
                val intent = android.content.Intent(it, NotificationsActivity::class.java)
                startActivity(intent)
            }
        }

        btnFilter.setOnClickListener {
            context?.let {
                val intent = android.content.Intent(it, FilterActivity::class.java)
                startActivity(intent)
            }
        }

        val chatClickListener = View.OnClickListener {
            context?.let {
                val intent = android.content.Intent(it, ChatActivity::class.java)
                startActivity(intent)
            }
        }

        helpCard.setOnClickListener(chatClickListener)
        fabChat.setOnClickListener(chatClickListener)

        setupCategories()
        setupProducts()

        val etSearch: android.widget.EditText = view.findViewById(R.id.etSearch)
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                updateSearchUI(query)
                if (::equipmentAdapter.isInitialized) {
                    equipmentAdapter.filter(query)
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupCategories() {
        val categories = listOf(
            CategoryItem("Cameras", R.drawable.cat_camera, "#F1F5F9", "#4CAF50"), 
            CategoryItem("Tripods", R.drawable.cat_tripod, "#F1F5F9", "#FF9800"),
            CategoryItem("Lens", R.drawable.cat_lens, "#F1F5F9", "#2196F3"),
            CategoryItem("Ring Light", R.drawable.cat_ringlight, "#F1F5F9", "#E91E63")
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            context?.let {
                val intent = android.content.Intent(it, CategoryProductsActivity::class.java)
                intent.putExtra("category_name", category.title)
                startActivity(intent)
            }
        }
        rvCategories.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupProducts() {
        rvProducts.apply {
            layoutManager = GridLayoutManager(context, 2)
            setHasFixedSize(true)
        }
        fetchEquipment()
    }

    private fun fetchEquipment() {
        RetrofitClient.api.getEquipment()
            .enqueue(object : Callback<EquipmentResponse> {
                override fun onResponse(
                    call: Call<EquipmentResponse>,
                    response: Response<EquipmentResponse>
                ) {
                    if (isAdded && response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success" && body.data.isNotEmpty()) {
                            equipmentAdapter = EquipmentAdapter(body.data) { item ->
                                context?.let {
                                    val intent = android.content.Intent(it, DetailsActivity::class.java)
                                    intent.putExtra("pid", item.id)
                                    startActivity(intent)
                                }
                            }
                            rvProducts.adapter = equipmentAdapter

                            // RE-APPLY SEARCH FILTER IF USER ALREADY TYPED
                            val etSearch: android.widget.EditText? = view?.findViewById(R.id.etSearch)
                            val currentQuery = etSearch?.text?.toString() ?: ""
                            if (currentQuery.isNotEmpty()) {
                                equipmentAdapter.filter(currentQuery)
                                updateSearchUI(currentQuery)
                            }
                        } else {
                            // Show specific message if DB is empty
                            context?.let { Toast.makeText(it, "Database is connected but EMPTY.", Toast.LENGTH_LONG).show() }
                        }
                    } else if (isAdded) {
                        context?.let { Toast.makeText(it, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show() }
                    }
                }

                override fun onFailure(call: Call<EquipmentResponse>, t: Throwable) {
                    if (isAdded) {
                        val errorMsg = "Database Connection Failed!\nCheck your Dev Tunnel URL."
                        context?.let { Toast.makeText(it, errorMsg, Toast.LENGTH_LONG).show() }
                    }
                }
            })
    }

    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fetchCurrentLocation()
        }
    }

    private fun fetchCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val cityName = addresses[0].locality ?: addresses[0].subAdminArea ?: "Chennai"
                            val countryName = addresses[0].countryName ?: "India"
                            tvLocation.text = "Live: $cityName, $countryName"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation()
        }
    }

    private fun updateSearchUI(query: String) {
        val view = view ?: return
        val exploreTitle: android.widget.TextView = view.findViewById(R.id.exploreTitle)
        val sectionCategories: View? = view.findViewById(R.id.sectionCategories)
        val helpCard: View = view.findViewById(R.id.helpCard)

        if (query.isEmpty()) {
            exploreTitle.text = "Explore Equipment"
            sectionCategories?.visibility = View.VISIBLE
            helpCard.visibility = View.VISIBLE
        } else {
            exploreTitle.text = "Search Results for '$query'"
            sectionCategories?.visibility = View.GONE
            helpCard.visibility = View.GONE
        }
    }
}
