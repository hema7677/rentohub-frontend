package com.simats.rentohub

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class BookingActivity : AppCompatActivity(), PaymentResultListener {

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var pricePerDay: Double = 0.0
    private var totalAmount: Double = 0.0
    private var equipmentId: Int = -1
    private lateinit var txtTotalDays: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)
        
        // Pre-load Checkout
        Checkout.preload(applicationContext)

        // UI Components
        val txtProductName: TextView = findViewById(R.id.txtProductName)
        val txtRate: TextView = findViewById(R.id.txtRate)
        val btnFromDate: TextView = findViewById(R.id.btnFromDate)
        val btnToDate: TextView = findViewById(R.id.btnToDate)
        txtTotalDays = findViewById(R.id.txtTotalDays) // Initialized property
        val txtTotalAmount: TextView = findViewById(R.id.txtTotalAmount)
        val btnPayNow: Button = findViewById(R.id.btnPayNow)

        // Get Data from Intent
        equipmentId = intent.getIntExtra("pid", -1)
        val name = intent.getStringExtra("name") ?: "Item"
        val priceString = intent.getStringExtra("price") ?: "0"

        // Parse price (assuming format "30000" or similar strings)
        pricePerDay = priceString.toDoubleOrNull() ?: 0.0

        txtProductName.text = name
        txtRate.text = "Rate: ₹$pricePerDay / day"

        // Date Pickers
        btnFromDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                btnFromDate.text = formatDate(date)
                calculateTotal(txtTotalDays, txtTotalAmount)
            }
        }

        btnToDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                btnToDate.text = formatDate(date)
                calculateTotal(txtTotalDays, txtTotalAmount)
            }
        }

        // Pay Now Button
        btnPayNow.setOnClickListener {
            if (startDate != null && endDate != null && (startDate!!.before(endDate) || startDate == endDate)) {
                startPayment()
            } else {
                Toast.makeText(this, "Please select valid dates", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = Calendar.getInstance()
                date.set(year, month, day)
                onDateSelected(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatDate(calendar: Calendar): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(calendar.time)
    }

    private fun calculateTotal(txtDays: TextView, txtAmount: TextView) {
        if (startDate != null && endDate != null) {
            val diffInMillis = endDate!!.timeInMillis - startDate!!.timeInMillis
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1 // Inclusive of start day

            if (days > 0) {
                txtDays.text = "$days"
                totalAmount = days * pricePerDay
                txtAmount.text = "₹$totalAmount"
            } else {
                txtDays.text = "0"
                totalAmount = 0.0
                txtAmount.text = "₹0"
                Toast.makeText(this, "End date must be after Start date", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startPayment() {
        if (totalAmount <= 0) {
            Toast.makeText(this, "Invalid Amount", Toast.LENGTH_SHORT).show()
            return
        }

        val checkout = Checkout()
        checkout.setKeyID("rzp_test_DrASf34mihEAtB") // Replace with your Test API Key
        
        try {
            val options = JSONObject()
            options.put("name", "Rentohub")
            options.put("description", "Equipment Rental Charge")
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")
            // TEST MODE: Fixed amount of ₹1 (100 paise) to avoid limit errors during testing
            // Real amount logic: options.put("amount", (totalAmount * 100).toInt()) 
            options.put("amount", 100)
            options.put("prefill.email", "test@rentohub.com")
            options.put("prefill.contact", "9876543210")
            
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        // Payment successful, now save booking to backend
        saveBookingToBackend(razorpayPaymentID)
    }

    private fun saveBookingToBackend(paymentId: String?) {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val days = txtTotalDays.text.toString()

        RetrofitClient.api.placeBooking(userId, equipmentId.toString(), days)
            .enqueue(object : retrofit2.Callback<BookingResponse> {
                override fun onResponse(
                    call: retrofit2.Call<BookingResponse>,
                    response: retrofit2.Response<BookingResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            showPaymentSuccessDialog(paymentId)
                        } else {
                            Toast.makeText(this@BookingActivity, "Booking Failed: ${body?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@BookingActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<BookingResponse>, t: Throwable) {
                    Toast.makeText(this@BookingActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
    }

    private fun showPaymentSuccessDialog(paymentId: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Payment Successful")
        builder.setMessage("Your booking has been confirmed!\nPayment ID: $paymentId")
        builder.setIcon(android.R.drawable.ic_dialog_info)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            finish() // Close booking screen
        }
        builder.show()
    }
}
