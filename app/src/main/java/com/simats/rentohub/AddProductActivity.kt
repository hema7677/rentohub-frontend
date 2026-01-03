package com.simats.rentohub

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddProductActivity : AppCompatActivity() {

    private lateinit var imgUpload: ImageView
    private var selectedImageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        val etProductName = findViewById<EditText>(R.id.etProductName)
        val etBrand = findViewById<EditText>(R.id.etBrand)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val etDailyRate = findViewById<EditText>(R.id.etDailyRate)
        val etDeposit = findViewById<EditText>(R.id.etDeposit)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        imgUpload = findViewById(R.id.imgUpload)

        // Setup Category Spinner
        val categories = arrayOf("Cameras", "Tripods", "Lens", "Ring Light", "Others")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spCategory.adapter = adapter

        // Pick image
        imgUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        // Submit product
        btnSubmit.setOnClickListener {
            if (selectedImageFile == null) {
                Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadProduct(
                etProductName.text.toString(),
                etBrand.text.toString(),
                spCategory.selectedItem.toString(),
                etDailyRate.text.toString(),
                etDeposit.text.toString(),
                etDescription.text.toString()
            )
        }
    }

    // Image picker result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data!!
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imgUpload.setImageBitmap(bitmap)

            val file = File(cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos)
            fos.close()

            selectedImageFile = file
        }
    }

    // 🔥 Upload product using Retrofit
    private fun uploadProduct(
        name: String,
        brand: String,
        category: String,
        rate: String,
        deposit: String,
        description: String
    ) {

        fun text(value: String) =
            value.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = MultipartBody.Part.createFormData(
            "image",
            selectedImageFile!!.name,
            selectedImageFile!!
                .asRequestBody("image/*".toMediaTypeOrNull())
        )

        RetrofitClient.api.addProduct(
            text(name),
            text(brand),
            text(category),
            text(rate),
            text(deposit),
            text(description),
            imagePart
        ).enqueue(object : Callback<AddProductResponse> {

            override fun onResponse(
                call: Call<AddProductResponse>,
                response: Response<AddProductResponse>
            ) {
                if (response.isSuccessful) {

                    val body = response.body()

                    if (body != null && body.status == "success") {
                        Toast.makeText(
                            this@AddProductActivity,
                            body.message,
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@AddProductActivity,
                            body?.message ?: "Server validation failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        this@AddProductActivity,
                        "HTTP Error: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }


            override fun onFailure(call: Call<AddProductResponse>, t: Throwable) {
                Toast.makeText(
                    this@AddProductActivity,
                    t.localizedMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
