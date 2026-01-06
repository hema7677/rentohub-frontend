package com.simats.rentohub

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

data class CategoryItem(
    val title: String,
    val iconResId: Int,
    val backgroundColor: String,
    val iconTint: String
)

class CategoryAdapter(
    private val list: List<CategoryItem>,
    private val onItemClick: (CategoryItem) -> Unit
) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v.findViewById(R.id.cardCategory)
        val img: ImageView = v.findViewById(R.id.img)
        val title: TextView = v.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.title
        holder.img.setImageResource(item.iconResId)
        
        // Set dynamic card color but keep image as-is (realistic)
        holder.card.setCardBackgroundColor(Color.parseColor(item.backgroundColor))
        // holder.img.imageTintList = ColorStateList.valueOf(Color.parseColor(item.iconTint)) // Removed tint for realistic photos

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = list.size
}
