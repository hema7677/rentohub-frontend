package com.simats.rentohub

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.simats.rentohub.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var spUserType: Spinner
    private lateinit var btnSignup: Button
    private lateinit var tvLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)  // No databinding

        // Initialize views
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        spUserType = findViewById(R.id.spUserType)
        btnSignup = findViewById(R.id.btnSignup)
        tvLogin = findViewById(R.id.tvLogin)

        // Spinner values
        val userTypes = arrayOf("user", "admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, userTypes)
        spUserType.adapter = adapter

        btnSignup.setOnClickListener {
            registerUser()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val usertype = spUserType.selectedItem.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.api.register(name, email, password, usertype)
            .enqueue(object : Callback<RegisterResponse> {

                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registered Successfully!", Toast.LENGTH_SHORT).show()

                        // Save usertype for FillProfileActivity to know where to go next
                        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("usertype", usertype)
                        editor.putString("user_email", email)
                        editor.apply()

                        // Redirect to FillProfile screen
                        startActivity(Intent(this@RegisterActivity, FillProfileActivity::class.java))
                        finish()

                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Server Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
