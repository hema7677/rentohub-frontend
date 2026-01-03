package com.simats.rentohub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Email and password required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loginUser(email, password)
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {

        RetrofitClient.api.loginUser(email, password)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {

                        val loginResponse = response.body()

                        if (loginResponse?.status == "success") {

                            val role = loginResponse.data?.usertype

                            Toast.makeText(
                                this@LoginActivity,
                                loginResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()

                            when (role) {

                                "admin" -> {
                                    val intent = Intent(
                                        this@LoginActivity,
                                        AdminDashboardActivity::class.java
                                    )
                                    startActivity(intent)
                                }

                                "user" -> {
                                    val intent = Intent(
                                        this@LoginActivity,
                                        UserMainActivity::class.java
                                    )
                                    startActivity(intent)
                                }

                                else -> {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Unknown user role",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            // Save User Session
                            val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("user_id", loginResponse.data?.id)
                            editor.putString("usertype", role)
                            editor.apply()

                            finish()
                        }

                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login failed: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
