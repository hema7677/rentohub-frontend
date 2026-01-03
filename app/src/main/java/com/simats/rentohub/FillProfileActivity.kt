package com.simats.rentohub

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FillProfileActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etDOB: EditText
    private lateinit var etGender: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnContinue: AppCompatButton
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_profile)

        etFullName = findViewById(R.id.etFullName)
        etDOB = findViewById(R.id.etDOB)
        etGender = findViewById(R.id.etGender)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        btnContinue = findViewById(R.id.btnContinue)

        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val isEdit = intent.getBooleanExtra("is_edit", false)

        if (isEdit) {
            etFullName.setText(sharedPreferences.getString("user_name", ""))
            etDOB.setText(sharedPreferences.getString("user_dob", ""))
            etGender.setText(sharedPreferences.getString("user_gender", ""))
            etPhoneNumber.setText(sharedPreferences.getString("user_phone", ""))
            btnContinue.text = "Save Profile"
        }

        // Date Picker for DOB
        etDOB.setOnClickListener {
            showDatePicker()
        }

        btnContinue.setOnClickListener {
            validateAndContinue()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLabel()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        etDOB.setText(sdf.format(calendar.time))
    }

    private fun validateAndContinue() {
        val name = etFullName.text.toString().trim()
        val dob = etDOB.text.toString().trim()
        val gender = etGender.text.toString().trim()
        val phone = etPhoneNumber.text.toString().trim()

        if (name.isEmpty() || dob.isEmpty() || gender.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Save details to SharedPreferences so ProfileFragment can show them
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("user_name", name)
        editor.putString("user_dob", dob)
        editor.putString("user_gender", gender)
        editor.putString("user_phone", phone)
        editor.apply()

        val usertype = sharedPreferences.getString("usertype", "user")
        val isEdit = intent.getBooleanExtra("is_edit", false)

        Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()

        if (isEdit) {
            // If editing, just go back
            finish()
        } else {
            // If first time, go to dashboard
            if (usertype == "admin") {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
            } else {
                startActivity(Intent(this, UserMainActivity::class.java))
            }
            finish()
        }
    }
}
