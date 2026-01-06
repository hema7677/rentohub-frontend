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

class EquipmentAdapter(
    private var fullList: List<Equipment>,
    private val onItemClick: (Equipment) -> Unit
) : RecyclerView.Adapter<EquipmentAdapter.ViewHolder>() {

    private var displayList: List<Equipment> = fullList

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

    // 1️⃣ CREATE VIEW HOLDER (FIXED)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_equipment, parent, false)
        return ViewHolder(view)
    }

    // 2️⃣ VIEW HOLDER
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.itemImage)
        val name: TextView = view.findViewById(R.id.itemName)
        val brand: TextView = view.findViewById(R.id.itemBrand)
        val price: TextView = view.findViewById(R.id.itemPrice)
    }

    // 3️⃣ BIND DATA
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = displayList[position]

        holder.name.text = item.name
        holder.brand.text = item.brand
        holder.price.text = "₹${item.price_per_day} / day"

        // Fix: Ensure image URL is full.
        val imageUrl = if (item.image.startsWith("http")) {
            item.image
        } else {
            RetrofitClient.BASE_URL + item.image
        }

        val glideUrl = GlideUrl(imageUrl, LazyHeaders.Builder()
            .addHeader("X-Tunnel-Skip-Anti-Phishing-Page", "true")
            .build())

        Glide.with(holder.itemView.context)
            .load(glideUrl)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.img)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    // 4️⃣ ITEM COUNT (FIXED)
    override fun getItemCount(): Int {
        return displayList.size
    }
}
