package com.simats.rentohub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import java.io.File

class UpdateProductActivity : AppCompatActivity() {

    private lateinit var imgProduct: ImageView
    private lateinit var etName: EditText
    private lateinit var etBrand: EditText
    private lateinit var etCategory: EditText
    private lateinit var etDailyRate: EditText
    private lateinit var etDeposit: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnUpdate: Button

    private var productId = ""
    private var selectedImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_product)

        imgProduct = findViewById(R.id.imgProduct)
        etName = findViewById(R.id.etName)
        etBrand = findViewById(R.id.etBrand)
        etCategory = findViewById(R.id.etCategory)
        etDailyRate = findViewById(R.id.etDailyRate)
        etDeposit = findViewById(R.id.etDeposit)
        etDescription = findViewById(R.id.etDescription)
        btnUpdate = findViewById(R.id.btnUpdate)

        // ✅ RECEIVE DATA
        productId = intent.getStringExtra("id") ?: ""

        if (productId.isEmpty()) {
            Toast.makeText(this, "Invalid Product ID", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        etName.setText(intent.getStringExtra("name"))
        etBrand.setText(intent.getStringExtra("brand"))
        etCategory.setText(intent.getStringExtra("category"))
        etDailyRate.setText(intent.getStringExtra("daily_rate"))
        etDeposit.setText(intent.getStringExtra("deposit"))
        etDescription.setText(intent.getStringExtra("description"))

        val existingImage = intent.getStringExtra("image") ?: ""
        if (existingImage.isNotEmpty()) {
            val imageUrl = if (existingImage.startsWith("http")) {
                existingImage
            } else {
                RetrofitClient.BASE_URL + existingImage
            }

            val glideUrl = GlideUrl(imageUrl, LazyHeaders.Builder()
                .addHeader("X-Tunnel-Skip-Anti-Phishing-Page", "true")
                .build())

            Glide.with(this)
                .load(glideUrl)
                .placeholder(R.drawable.placeholder)
                .into(imgProduct)
        }

        imgProduct.setOnClickListener { pickImage() }
        btnUpdate.setOnClickListener { updateProduct() }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK) {
            selectedImage = data?.data
            imgProduct.setImageURI(selectedImage)
        }
    }

    private fun updateProduct() {

        val name = etName.text.toString().trim()
        val brand = etBrand.text.toString().trim()
        val category = etCategory.text.toString().trim()
        val dailyRate = etDailyRate.text.toString().trim()
        val deposit = etDeposit.text.toString().trim()
        val description = etDescription.text.toString().trim()

        // ✅ LOG VALUES (CRITICAL)
        Log.d("UPDATE_PRODUCT", """
            ID=$productId
            Name=$name
            Brand=$brand
            Category=$category
            DailyRate=$dailyRate
            Deposit=$deposit
            Description=$description
        """.trimIndent())

        fun body(value: String): RequestBody =
            value.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = selectedImage?.let {
            val file = File(getRealPathFromURI(it))
            val req = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, req)
        }

        RetrofitClient.api.updateProduct(
            body(productId),
            body(name),
            body(brand),
            body(category),
            body(dailyRate),
            body(deposit),
            body(description),
            imagePart
        ).enqueue(object : Callback<UpdateProductResponse> {

            override fun onResponse(
                call: Call<UpdateProductResponse>,
                response: Response<UpdateProductResponse>
            ) {
                if (response.isSuccessful) {

                    val body = response.body()

                    if (body != null && body.status == "success") {
                        Toast.makeText(
                            this@UpdateProductActivity,
                            body.message ?: "Updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        setResult(RESULT_OK)   // ✅ TELL PREVIOUS PAGE TO REFRESH
                        finish()
                    }
                    else {
                        Toast.makeText(
                            this@UpdateProductActivity,
                            body?.message ?: "Server error",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        this@UpdateProductActivity,
                        "HTTP Error: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<UpdateProductResponse>, t: Throwable) {
                Toast.makeText(
                    this@UpdateProductActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val index = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA) ?: 0
        val path = cursor?.getString(index) ?: ""
        cursor?.close()
        return path
    }
}
