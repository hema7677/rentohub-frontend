package com.simats.rentohub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EquipmentAdapter(
    private val list: List<Equipment>,
    private val onItemClick: (Equipment) -> Unit
) : RecyclerView.Adapter<EquipmentAdapter.ViewHolder>() {

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
        val item = list[position]

        holder.name.text = item.name
        holder.brand.text = item.brand
        holder.price.text = "₹${item.price_per_day} / day"

        android.util.Log.d("IMAGE_URL", item.image)

        Glide.with(holder.itemView.context)
            .load(item.image)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.img)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    // 4️⃣ ITEM COUNT (FIXED)
    override fun getItemCount(): Int {
        return list.size
    }
}
