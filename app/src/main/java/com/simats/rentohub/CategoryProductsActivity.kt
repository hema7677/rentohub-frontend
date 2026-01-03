package com.simats.rentohub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryProductsActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var tvNoData: TextView
    private var categoryName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_products)

        categoryName = intent.getStringExtra("category_name") ?: ""
        
        rvProducts = findViewById(R.id.rvCategoryProducts)
        tvTitle = findViewById(R.id.tvCategoryTitle)
        btnBack = findViewById(R.id.btnBack)
        tvNoData = findViewById(R.id.tvNoData)

        tvTitle.text = categoryName

        btnBack.setOnClickListener {
            finish()
        }

        rvProducts.layoutManager = LinearLayoutManager(this)
        
        fetchAndFilterProducts()
    }

    private fun fetchAndFilterProducts() {
        RetrofitClient.api.getEquipment()
            .enqueue(object : Callback<EquipmentResponse> {
                override fun onResponse(
                    call: Call<EquipmentResponse>,
                    response: Response<EquipmentResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val allEquipment = body?.data ?: emptyList()
                        
                        // Debug log for developer (visible in Logcat)
                        android.util.Log.d("CategoryDebug", "Fetched ${allEquipment.size} items. Searching for: $categoryName")

                        // 1. IMPROVED ULTRA-FLEXIBLE FILTERING
                        val filteredList = allEquipment.filter { equipment ->
                            val dbCat = equipment.category.lowercase().trim()
                            val dbName = equipment.name.lowercase().trim()
                            val searchCat = categoryName.lowercase().trim()
                            
                            // Get singular version (e.g., "Cameras" -> "camera", "Lens" -> "len")
                            val singularSearch = when {
                                searchCat.endsWith("s") -> searchCat.dropLast(1)
                                searchCat.endsWith("ies") -> searchCat.dropLast(3) + "y"
                                else -> searchCat
                            }

                            // Match if search term is in Category OR Name OR Brand
                            dbCat.contains(singularSearch) || 
                            dbName.contains(singularSearch) ||
                            equipment.brand.lowercase().contains(singularSearch) ||
                            // Special case for Lens/Lenses
                            (singularSearch == "camera" && dbCat.contains("camer")) ||
                            (singularSearch == "len" && dbCat.contains("lense"))
                        }

                        if (filteredList.isNotEmpty()) {
                            val adapter = EquipmentAdapter(filteredList) { item ->
                                val intent = Intent(this@CategoryProductsActivity, DetailsActivity::class.java)
                                intent.putExtra("pid", item.id)
                                startActivity(intent)
                            }
                            rvProducts.adapter = adapter
                            tvNoData.visibility = View.GONE
                        } else {
                            tvNoData.visibility = View.VISIBLE
                            tvNoData.text = "No products found for '$categoryName'.\nTry searching for something else!"
                            
                            // If DB is totally empty, show a different message
                            if (allEquipment.isEmpty()) {
                                tvNoData.text = "Database is empty.\nPlease add some equipment first!"
                            }
                        }
                    } else {
                        val errorStr = "HTTP Error: ${response.code()}"
                        Toast.makeText(this@CategoryProductsActivity, errorStr, Toast.LENGTH_SHORT).show()
                        tvNoData.visibility = View.VISIBLE
                        tvNoData.text = errorStr
                    }
                }

                override fun onFailure(call: Call<EquipmentResponse>, t: Throwable) {
                    val errorStr = "Connection Failed!\nPlease check your internet or Dev Tunnel."
                    Toast.makeText(this@CategoryProductsActivity, errorStr, Toast.LENGTH_LONG).show()
                    tvNoData.visibility = View.VISIBLE
                    tvNoData.text = errorStr
                }
            })
    }
}
