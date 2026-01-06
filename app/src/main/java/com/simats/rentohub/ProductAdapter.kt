package com.simats.rentohub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.google.android.material.button.MaterialButton

class ProductAdapter(
    private var fullList: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var displayList: List<Product> = fullList

    fun filter(query: String) {
        displayList = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)

        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct) // ✅ image

        val btnEdit: MaterialButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: MaterialButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = displayList[position]

        holder.txtName.text = product.name
        holder.txtCategory.text = product.category
        holder.txtPrice.text = "₹${product.price_per_day}"
        holder.txtStatus.text = product.status
        // Load image using Glide with full URL
        val imgPath = product.image ?: ""
        val imageUrl = if (imgPath.startsWith("http")) {
            imgPath
        } else if (imgPath.isNotEmpty()) {
            RetrofitClient.BASE_URL + imgPath
        } else {
            "" // Empty URL will trigger the error/placeholder
        }

        val glideUrl = if (imageUrl.isNotEmpty()) {
            GlideUrl(imageUrl, LazyHeaders.Builder()
                .addHeader("X-Tunnel-Skip-Anti-Phishing-Page", "true")
                .build())
        } else {
            null
        }

        Glide.with(holder.itemView.context)
            .load(glideUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.camera) // use camera as fallback
            .into(holder.imgProduct)
        holder.btnEdit.setOnClickListener { onEditClick(product) }
        holder.btnDelete.setOnClickListener { onDeleteClick(product) }
    }

    override fun getItemCount(): Int = displayList.size
}
