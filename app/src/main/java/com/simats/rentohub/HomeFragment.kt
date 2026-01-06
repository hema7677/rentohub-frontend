package com.simats.rentohub

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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
        val helpCard: View = view.findViewById(R.id.helpCard)
        val fabChat: View = view.findViewById(R.id.fab_chat)
        val btnNotification: View = view.findViewById(R.id.btnNotification)
        val btnFilter: View = view.findViewById(R.id.btnFilter)
        val tvWelcome: android.widget.TextView = view.findViewById(R.id.tvWelcome)
        val profileImage: View = view.findViewById(R.id.profileImage)

        // Load name from session
        val sharedPreferences = requireContext().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", "User")
        tvWelcome.text = "Hey User!" 

        btnNotification.setOnClickListener {
            val intent = android.content.Intent(requireContext(), NotificationsActivity::class.java)
            startActivity(intent)
        }

        btnFilter.setOnClickListener {
            val intent = android.content.Intent(requireContext(), FilterActivity::class.java)
            startActivity(intent)
        }

        val chatClickListener = View.OnClickListener {
            val intent = android.content.Intent(requireContext(), ChatActivity::class.java)
            startActivity(intent)
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
            layoutManager = GridLayoutManager(requireContext(), 2)
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

                            // RE-APPLY SEARCH FILTER IF USER ALREADY TYPED
                            val etSearch: android.widget.EditText? = view?.findViewById(R.id.etSearch)
                            val currentQuery = etSearch?.text?.toString() ?: ""
                            if (currentQuery.isNotEmpty()) {
                                equipmentAdapter.filter(currentQuery)
                                updateSearchUI(currentQuery)
                            }
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
