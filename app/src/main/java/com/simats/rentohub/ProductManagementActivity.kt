package com.simats.rentohub

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProductManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_management)

        recyclerView = findViewById(R.id.recyclerProducts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadProducts()
    }

    private fun loadProducts() {
        RetrofitClient.api.getProducts()
            .enqueue(object : Callback<ProductResponse> {
                override fun onResponse(
                    call: Call<ProductResponse>,
                    response: Response<ProductResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val products = response.body()!!.data
                        adapter = ProductAdapter(
                            products,
                            onEditClick = { product ->
                                val intent = Intent(this@ProductManagementActivity, UpdateProductActivity::class.java)
                                intent.putExtra("id", product.id.toString())
                                intent.putExtra("name", product.name)
                                intent.putExtra("brand", product.brand)
                                intent.putExtra("category", product.category)
                                intent.putExtra("daily_rate", product.price_per_day)
                                intent.putExtra("deposit", product.deposit)
                                intent.putExtra("description", product.description)
                                intent.putExtra("image",product.imgProduct)
                                intent.putExtra("status",product.status)
                                startActivity(intent)
                            },
                            onDeleteClick = { product ->
                                RetrofitClient.api.deleteProduct(product.id)
                                    .enqueue(object : Callback<DeleteResponse> {
                                        override fun onResponse(
                                            call: Call<DeleteResponse>,
                                            response: Response<DeleteResponse>
                                        ) {
                                            if (response.isSuccessful && response.body()?.status == "success") {
                                                Toast.makeText(
                                                    this@ProductManagementActivity,
                                                    "Deleted ${product.name}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                loadProducts() // Refresh list
                                            } else {
                                                Toast.makeText(
                                                    this@ProductManagementActivity,
                                                    "Delete failed: ${response.body()?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                                            Toast.makeText(
                                                this@ProductManagementActivity,
                                                "Delete failed: ${t.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })
                            }
                        )
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(
                            this@ProductManagementActivity,
                            "No products found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ProductManagementActivity,
                        "Failed: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
