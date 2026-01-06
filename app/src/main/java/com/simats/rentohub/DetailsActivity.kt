package com.simats.rentohub

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders

class DetailsActivity : AppCompatActivity() {

    private var quantity = 1
    private var durationDays = 1
    private var basePrice = 0.0

    private lateinit var tvQty: TextView
    private lateinit var tvDays: TextView
    private lateinit var txtTotalAmount: TextView
    private lateinit var txtCalculation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Initialize UI Components
        val imgProperty: ImageView = findViewById(R.id.imgProperty)
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val txtName: TextView = findViewById(R.id.txtName)
        val txtBrand: TextView = findViewById(R.id.txtBrand)
        val txtPrice: TextView = findViewById(R.id.txtPrice)
        val txtCategory: TextView = findViewById(R.id.txtCategory)
        val txtDescription: TextView = findViewById(R.id.txtDescription)
        
        tvQty = findViewById(R.id.tvQty)
        tvDays = findViewById(R.id.tvDays)
        txtTotalAmount = findViewById(R.id.txtTotalAmount)
        txtCalculation = findViewById(R.id.txtCalculation)

        val btnQtyMinus: ImageButton = findViewById(R.id.btnQtyMinus)
        val btnQtyPlus: ImageButton = findViewById(R.id.btnQtyPlus)
        val btnDaysMinus: ImageButton = findViewById(R.id.btnDaysMinus)
        val btnDaysPlus: ImageButton = findViewById(R.id.btnDaysPlus)
        
        val btnBook: Button = findViewById(R.id.btnBook)
        val btnAddToCart: Button = findViewById(R.id.btnAddToCart)

        val pid = intent.getIntExtra("pid", -1)

        btnBack.setOnClickListener { finish() }

        if (pid != -1) {
            fetchDetails(pid, imgProperty, txtName, txtBrand, txtPrice, txtCategory, txtDescription)
        } else {
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_SHORT).show()
        }

        // Logic for Quantity
        btnQtyPlus.setOnClickListener {
            quantity++
            updateUI()
        }
        btnQtyMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                updateUI()
            }
        }

        // Logic for Duration
        btnDaysPlus.setOnClickListener {
            durationDays++
            updateUI()
        }
        btnDaysMinus.setOnClickListener {
            if (durationDays > 1) {
                durationDays--
                updateUI()
            }
        }

        btnBook.setOnClickListener {
            val intent = android.content.Intent(this, BookingActivity::class.java)
            intent.putExtra("pid", pid)
            intent.putExtra("name", txtName.text.toString())
            intent.putExtra("price", basePrice.toString())
            intent.putExtra("qty", quantity)
            intent.putExtra("days", durationDays)
            startActivity(intent)
        }

        btnAddToCart.setOnClickListener {
            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        tvQty.text = quantity.toString()
        tvDays.text = durationDays.toString()
        
        val total = basePrice * quantity * durationDays
        txtTotalAmount.text = "₹${total.toInt()}"
        txtCalculation.text = "$quantity item(s) × $durationDays day(s)"
    }

    private fun fetchDetails(
        id: Int,
        img: ImageView,
        name: TextView,
        brand: TextView,
        price: TextView,
        category: TextView,
        description: TextView
    ) {
        RetrofitClient.api.getEquipmentDetails(id)
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
                                brand.text = item.brand ?: "N/A"
                                
                                basePrice = item.price_per_day?.toDoubleOrNull() ?: 0.0
                                price.text = "₹${basePrice.toInt()}"
                                
                                category.text = item.category ?: "N/A"
                                description.text = item.description ?: "No description available."
                                
                                updateUI()

                                val imageUrl = if (item.image?.startsWith("http") == true) {
                                    item.image
                                } else {
                                    RetrofitClient.BASE_URL + (item.image ?: "")
                                }

                                val glideUrl = GlideUrl(imageUrl, LazyHeaders.Builder()
                                    .addHeader("X-Tunnel-Skip-Anti-Phishing-Page", "true")
                                    .build())

                                Glide.with(this@DetailsActivity)
                                    .load(glideUrl)
                                    .placeholder(R.drawable.placeholder)
                                    .error(R.drawable.placeholder)
                                    .into(img)
                            }
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<EquipmentDetailResponse>, t: Throwable) {
                    Toast.makeText(this@DetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
