package com.simats.rentohub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class ProductAdapter(
    private val productList: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

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
        val product = productList[position]

        holder.txtName.text = product.name
        holder.txtCategory.text = product.category
        holder.txtPrice.text = "₹${product.price_per_day}"
        holder.txtStatus.text = product.status
// Load image using Glide
        Glide.with(holder.itemView.context)
            .load(product.imgProduct) // product.image should be the URL or relative path
            .placeholder(R.drawable.placeholder) // optional placeholder
            .error(R.drawable.error_image)       // optional error image
            .into(holder.imgProduct)
        holder.btnEdit.setOnClickListener { onEditClick(product) }
        holder.btnDelete.setOnClickListener { onDeleteClick(product) }
    }

    override fun getItemCount(): Int = productList.size
}
