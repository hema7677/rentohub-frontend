package com.simats.rentohub

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class EditProfileActivity : AppCompatActivity() {

    private lateinit var ivProfile: ImageView
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            if (uri != null) {
                selectedImageUri = uri
                // Take persistable permission to avoid SecurityException
                try {
                    val contentResolver = contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.profile)
                    .into(ivProfile)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        ivProfile = findViewById(R.id.ivEditProfilePic)
        val etName = findViewById<EditText>(R.id.etEditName)
        val etEmail = findViewById<EditText>(R.id.etEditEmail)
        val etPhone = findViewById<EditText>(R.id.etEditPhone)
        val btnSave = findViewById<TextView>(R.id.btnSave)
        val btnBack = findViewById<View>(R.id.btnBack)
        val btnChangePic = findViewById<View>(R.id.btnChangePic)

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        
        // Load current data
        etName.setText(sharedPreferences.getString("user_name", "Pooja Patel"))
        etEmail.setText(sharedPreferences.getString("user_email", "poojabhaipooja@gmail.com"))
        etPhone.setText(sharedPreferences.getString("user_phone", "+91 8448484848"))
        
        val savedImageUri = sharedPreferences.getString("user_image", null)
        if (savedImageUri != null) {
            Glide.with(this)
                .load(Uri.parse(savedImageUri))
                .placeholder(R.drawable.profile)
                .into(ivProfile)
        }

        btnBack.setOnClickListener { finish() }

        btnChangePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to SharedPreferences
            sharedPreferences.edit().apply {
                putString("user_name", name)
                putString("user_email", email)
                putString("user_phone", phone)
                if (selectedImageUri != null) {
                    putString("user_image", selectedImageUri.toString())
                }
                apply()
            }

            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
