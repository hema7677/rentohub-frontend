package com.simats.rentohub

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputLayout
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BookingActivity : AppCompatActivity(), PaymentResultListener {

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var pricePerDay: Double = 0.0
    private var totalAmount: Double = 0.0
    private var equipmentId: Int = -1
    private lateinit var txtTotalDays: TextView
    private lateinit var etAddress: EditText
    private lateinit var layoutAddress: TextInputLayout
    
    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)
        
        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Pre-load Checkout
        Checkout.preload(applicationContext)

        // UI Components
        val txtProductName: TextView = findViewById(R.id.txtProductName)
        val txtRate: TextView = findViewById(R.id.txtRate)
        val btnFromDate: View = findViewById(R.id.btnFromDate)
        val btnToDate: View = findViewById(R.id.btnToDate)
        val tvFromDateText: TextView = findViewById(R.id.tvFromDateText)
        val tvToDateText: TextView = findViewById(R.id.tvToDateText)
        txtTotalDays = findViewById(R.id.txtTotalDays)
        val txtTotalAmount: TextView = findViewById(R.id.txtTotalAmount)
        etAddress = findViewById(R.id.etAddress)
//        layoutAddress = findViewById(R.id.layoutAddress)
        val btnPayNow: View = findViewById(R.id.btnPayNow)
        val btnBack: View = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        // Setup end icon click listener for location
//        layoutAddress.setEndIconOnClickListener {
//            checkLocationPermissionAndFetch()
//        }

        // Feature: Automatically offer to fetch location if empty and clicked
        etAddress.setOnClickListener {
            if (etAddress.text.isEmpty()) {
                checkLocationPermissionAndFetch()
            }
        }

        // Get Data from Intent
        equipmentId = intent.getIntExtra("pid", -1)
        val name = intent.getStringExtra("name") ?: "Item"
        val priceString = intent.getStringExtra("price") ?: "0"

        // Parse price
        pricePerDay = priceString.toDoubleOrNull() ?: 0.0

        txtProductName.text = name
        txtRate.text = "Rate: ₹$pricePerDay / day"

        // Date Pickers
        btnFromDate.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                tvFromDateText.text = formatDate(date)
                calculateTotal(txtTotalDays, txtTotalAmount)
            }
        }

        btnToDate.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                tvToDateText.text = formatDate(date)
                calculateTotal(txtTotalDays, txtTotalAmount)
            }
        }

        // Pay Now Button
        btnPayNow.setOnClickListener {
            val address = etAddress.text.toString().trim()
            if (startDate != null && endDate != null && (startDate!!.before(endDate) || startDate == endDate)) {
                if (address.isEmpty()) {
                    Toast.makeText(this, "Please enter shipping address", Toast.LENGTH_SHORT).show()
                } else {
                    startPayment(address)
                }
            } else {
                Toast.makeText(this, "Please select valid dates", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            fetchCurrentLocation()
        }
    }

    private fun fetchCurrentLocation() {
        try {
            Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show()
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    // Fix for API level >= 33 vs < 33 if needed, but simple version usually works for most
                    try {
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0].getAddressLine(0)
                            etAddress.setText(address)
                            Toast.makeText(this, "Location detected!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error finding address. Please type manually.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Location unavailable. Please check GPS.", Toast.LENGTH_LONG).show()
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
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1 

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
    
    private var isProcessing = false

    override fun onBackPressed() {
        if (!isProcessing) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Processing payment, please wait...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPayment(address: String) {
        if (totalAmount <= 0) {
            Toast.makeText(this, "Invalid Amount", Toast.LENGTH_SHORT).show()
            return
        }
        
        isProcessing = true
        findViewById<View>(R.id.btnPayNow).isEnabled = false

        val checkout = Checkout()
        checkout.setKeyID("rzp_test_DrASf34mihEAtB")
        
        try {
            val options = JSONObject()
            options.put("name", "Rentohub")
            options.put("description", "Equipment Rental Charge")
            options.put("theme.color", "#3399cc")
            options.put("currency", "INR")
            options.put("amount", (totalAmount * 100).toInt()) // Actual amount in paise
            options.put("prefill.email", "customer@rentohub.com")
            options.put("prefill.contact", "9876543210")
            
            val notes = JSONObject()
            notes.put("address", address)
            options.put("notes", notes)
            
            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        isProcessing = true
        findViewById<View>(R.id.loadingOverlay).visibility = View.VISIBLE
        val address = etAddress.text.toString().trim()
        saveBookingToBackend(razorpayPaymentID, address)
    }

    private fun saveBookingToBackend(paymentId: String?, address: String) {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        val days = txtTotalDays.text.toString()

        RetrofitClient.api.placeBooking(userId, equipmentId.toString(), days, address)
            .enqueue(object : retrofit2.Callback<BookingResponse> {
                override fun onResponse(
                    call: retrofit2.Call<BookingResponse>,
                    response: retrofit2.Response<BookingResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            Toast.makeText(this@BookingActivity, "Booking Successful!", Toast.LENGTH_SHORT).show()
                            showPaymentSuccessDialog(paymentId)
                        } else {
                            isProcessing = false
                            findViewById<View>(R.id.loadingOverlay).visibility = View.GONE
                            androidx.appcompat.app.AlertDialog.Builder(this@BookingActivity)
                                .setTitle("Booking Status")
                                .setMessage("Payment was received, but we couldn't record your booking: ${body?.message}\n\nPlease contact support with Txn ID: $paymentId")
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    } else {
                        isProcessing = false
                        findViewById<View>(R.id.loadingOverlay).visibility = View.GONE
                        Toast.makeText(this@BookingActivity, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<BookingResponse>, t: Throwable) {
                    isProcessing = false
                    findViewById<View>(R.id.loadingOverlay).visibility = View.GONE
                    androidx.appcompat.app.AlertDialog.Builder(this@BookingActivity)
                        .setTitle("Network Error")
                        .setMessage("Payment received, but backend sync failed. We will retry automatically. Txn ID: $paymentId")
                        .setPositiveButton("OK", null)
                        .show()
                }
            })
    }

    override fun onPaymentError(code: Int, response: String?) {
        isProcessing = false
        findViewById<View>(R.id.btnPayNow).isEnabled = true
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
    }

    private fun showPaymentSuccessDialog(paymentId: String?) {
        val nextIntent = Intent(this, PaymentSuccessActivity::class.java)
        nextIntent.putExtra("txn_id", paymentId)
        nextIntent.putExtra("amount", "₹$totalAmount")
        nextIntent.putExtra("address", etAddress.text.toString().trim())
        nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(nextIntent)
        finish()
    }
}
