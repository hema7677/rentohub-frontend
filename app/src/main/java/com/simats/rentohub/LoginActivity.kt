package com.simats.rentohub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var btnGoogleSignIn: LinearLayout

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 🔹 Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // 🔹 Google Sign-In configuration (IMPORTANT)
        // 🔹 Google Sign-In configuration (IMPORTANT)
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Toast.makeText(this, "Google Sign-In config missing!", Toast.LENGTH_LONG).show()
        }

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password required", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    // 🔹 Google Sign-In launcher
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Debugging: Show result code
//            Toast.makeText(this, "Result Code: ${result.resultCode}", Toast.LENGTH_SHORT).show()

            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    // Verified Google Login, now Firebase
//                    Toast.makeText(this, "Google OK. Auth with Firebase...", Toast.LENGTH_SHORT).show()
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google Sign-In Error: ${e.statusCode}", Toast.LENGTH_LONG).show()
                    android.util.Log.e("LoginActivity", "Google Sign-In Intent Failed: ${e.statusCode}", e)
                }
            } else {
                Toast.makeText(this, "Sign-In Canceled/Failed (Code: ${result.resultCode})", Toast.LENGTH_LONG).show()
            }
        }

    private fun signInWithGoogle() {
        if (::googleSignInClient.isInitialized) {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        } else {
            Toast.makeText(this, "Google Sign-In not configured", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔹 Firebase authentication
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = firebaseAuth.currentUser
                    val email = user?.email ?: ""
                    val name = user?.displayName ?: "Google User"

                    Toast.makeText(
                        this,
                        "Welcome $name",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Sync with backend database
                    RetrofitClient.api.syncGoogleUser(name, email)
                        .enqueue(object : Callback<LoginResponse> {
                            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                if (response.isSuccessful && response.body()?.status == "success") {
                                    val dbUser = response.body()?.data
                                    
                                    // Save DB session
                                    val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
                                    sharedPreferences.edit()
                                        .putString("user_id", dbUser?.id)
                                        .putString("user_name", dbUser?.name)
                                        .putString("user_email", dbUser?.email)
                                        .putString("usertype", dbUser?.usertype ?: "user")
                                        .apply()

                                    startActivity(Intent(this@LoginActivity, UserMainActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Database sync failed: ${response.body()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                Toast.makeText(this@LoginActivity, "Network error during sync: ${t.message}", Toast.LENGTH_LONG).show()
                            }
                        })

                } else {
                    val errorMsg = task.exception?.message ?: "Unknown error"
                    Toast.makeText(this, "Auth Failed: $errorMsg", Toast.LENGTH_LONG).show()
                    android.util.Log.e("LoginActivity", "Firebase Auth Failed: ", task.exception)
                }
            }
    }

    // 🔹 Normal login (API)
    private fun loginUser(email: String, password: String) {

        RetrofitClient.api.loginUser(email, password)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {

                        val role = response.body()?.data?.usertype

                        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putString("user_id", response.body()?.data?.id)
                            .putString("user_name", response.body()?.data?.name)
                            .putString("user_email", response.body()?.data?.email)
                            .putString("usertype", role)
                            .apply()

                        if (role == "admin") {
                            startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        } else {
                            startActivity(Intent(this@LoginActivity, UserMainActivity::class.java))
                        }

                        finish()

                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid login", Toast.LENGTH_SHORT).show()
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
