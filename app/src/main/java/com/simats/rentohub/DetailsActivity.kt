package com.simats.rentohub

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val imgProperty: android.widget.ImageView = findViewById(R.id.imgProperty)
        val txtName: android.widget.TextView = findViewById(R.id.txtName)
        val txtBrand: android.widget.TextView = findViewById(R.id.txtBrand)
        val txtPrice: android.widget.TextView = findViewById(R.id.txtPrice)
        val txtDeposit: android.widget.TextView = findViewById(R.id.txtDeposit)
        val txtCategory: android.widget.TextView = findViewById(R.id.txtCategory)
        val txtDescription: android.widget.TextView = findViewById(R.id.txtDescription)
        val txtStatus: android.widget.TextView = findViewById(R.id.txtStatus)
        val btnBook: Button = findViewById(R.id.btnBook)

        val pid = intent.getIntExtra("pid", -1)

        if (pid != -1) {
            Toast.makeText(this, "Fetching details for ID: $pid", Toast.LENGTH_SHORT).show()
            fetchDetails(pid, imgProperty, txtName, txtBrand, txtPrice, txtDeposit, txtCategory, txtDescription, txtStatus)
        } else {
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_SHORT).show()
        }

        btnBook.setOnClickListener {
            // Toast.makeText(this@DetailsActivity, "Booking Clicked for ID: $pid", Toast.LENGTH_SHORT).show()
            
            // Navigate to Booking Activity
            val intent = android.content.Intent(this, BookingActivity::class.java)
            intent.putExtra("pid", pid)
            intent.putExtra("name", txtName.text.toString())
            // Pass raw price from the text view or better, fetch from the loaded item if accessible
            // For now, scraping from current state or we can rely on what we fetched.
            // A better approach is to store the fetched item in a variable. I'll rely on textviews for quick implementation as per user flow.
            // Need to clean "₹500 / day" to just "500"
            val priceClean = txtPrice.text.toString().replace(Regex("[^0-9.]"), "")
            intent.putExtra("price", priceClean)
            
            startActivity(intent)
        }
    }

    private fun fetchDetails(
        id: Int,
        img: android.widget.ImageView,
        name: android.widget.TextView,
        brand: android.widget.TextView,
        price: android.widget.TextView,
        deposit: android.widget.TextView,
        category: android.widget.TextView,
        description: android.widget.TextView,
        status: android.widget.TextView
    ) {

        com.simats.rentohub.RetrofitClient.api.getEquipmentDetails(id)
            .enqueue(object : retrofit2.Callback<EquipmentDetailResponse> {
                override fun onResponse(
                    call: retrofit2.Call<EquipmentDetailResponse>,
                    response: retrofit2.Response<EquipmentDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            val item = body.data
                            if (item != null) {
                                name.text = item.name ?: "N/A"
                                brand.text = "Brand: ${item.brand ?: "N/A"}"
                                price.text = "₹${item.price_per_day ?: "0"} / day"
                                deposit.text = "Deposit: ₹${item.deposit ?: "0"}"
                                category.text = "Category: ${item.category ?: "N/A"}"
                                description.text = item.description ?: "No description available."
                                status.text = "Status: ${item.status ?: "Unknown"}"

                                com.bumptech.glide.Glide.with(this@DetailsActivity)
                                    .load(item.image)
                                    .placeholder(R.drawable.placeholder)
                                    .error(R.drawable.placeholder)
                                    .into(img)
                            } else {
                                Toast.makeText(this@DetailsActivity, "Data is null", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@DetailsActivity, "API Error: ${body?.status}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@DetailsActivity, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<EquipmentDetailResponse>, t: Throwable) {
                    Toast.makeText(this@DetailsActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
