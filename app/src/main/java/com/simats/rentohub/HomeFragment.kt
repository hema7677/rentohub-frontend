package com.simats.rentohub

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var rvCategories: RecyclerView
    private lateinit var rvProducts: RecyclerView
    private lateinit var equipmentAdapter: EquipmentAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCategories = view.findViewById(R.id.rvCategories)
        rvProducts = view.findViewById(R.id.rvProducts)

        setupCategories()
        setupProducts()
    }

    private fun setupCategories() {
        val categories = listOf(
            CategoryItem("Cameras", R.drawable.camera, "#E8F5E9", "#4CAF50"), // Green
            CategoryItem("Tripods", R.drawable.tripod, "#FFF3E0", "#FF9800"), // Orange
            CategoryItem("Lens", R.drawable.lens, "#E3F2FD", "#2196F3"),    // Blue
            CategoryItem("Ring Light", R.drawable.ic_ring_light, "#FCE4EC", "#E91E63") // Pink
        )

        categoryAdapter = CategoryAdapter(categories) { category ->
            val intent = android.content.Intent(requireContext(), CategoryProductsActivity::class.java)
            intent.putExtra("category_name", category.title)
            startActivity(intent)
        }
        rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupProducts() {
        rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
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
                                val intent = android.content.Intent(requireContext(), DetailsActivity::class.java)
                                intent.putExtra("pid", item.id)
                                startActivity(intent)
                            }
                            rvProducts.adapter = equipmentAdapter
                        } else {
                            // Show specific message if DB is empty
                            Toast.makeText(requireContext(), "Database is connected but EMPTY.", Toast.LENGTH_LONG).show()
                        }
                    } else if (isAdded) {
                        Toast.makeText(requireContext(), "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<EquipmentResponse>, t: Throwable) {
                    if (isAdded) {
                        val errorMsg = "Database Connection Failed!\nCheck your Dev Tunnel URL."
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            })
    }
}
