package com.simats.rentohub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val rvOrders = findViewById<RecyclerView>(R.id.rvOrders)

        btnBack.setOnClickListener { finish() }

        val fakeOrders = listOf(
            FakeOrder("Sony Alpha A7 III", "25 Dec 2023", "Confirmed", "₹1,500"),
            FakeOrder("Canon EOS R5", "22 Dec 2023", "Delivered", "₹2,200"),
            FakeOrder("DJI Mavic 3 Pro", "18 Dec 2023", "Returned", "₹3,000"),
            FakeOrder("GoPro Hero 12", "15 Dec 2023", "Cancelled", "₹800"),
            FakeOrder("Zhiyun Crane 3S", "10 Dec 2023", "Delivered", "₹1,200"),
            FakeOrder("Nikon Z9", "05 Dec 2023", "Returned", "₹2,500"),
            FakeOrder("Ring Light 18\"", "01 Dec 2023", "Delivered", "₹400"),
            FakeOrder("Manfrotto Tripod", "28 Nov 2023", "Confirmed", "₹600"),
            FakeOrder("Rode Wireless Go II", "20 Nov 2023", "Delivered", "₹900"),
            FakeOrder("Blackmagic Pocket 6K", "15 Nov 2023", "Returned", "₹2,800")
        )

        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = OrdersAdapter(fakeOrders)
    }

    data class FakeOrder(val name: String, val date: String, val status: String, val price: String)

    class OrdersAdapter(private val orders: List<FakeOrder>) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvOrderName)
            val tvDate: TextView = view.findViewById(R.id.tvOrderDate)
            val tvStatus: TextView = view.findViewById(R.id.tvOrderStatus)
            val tvPrice: TextView = view.findViewById(R.id.tvOrderPrice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fake_order, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = orders[position]
            holder.tvName.text = order.name
            holder.tvDate.text = "Booked on: ${order.date}"
            holder.tvStatus.text = order.status
            holder.tvPrice.text = order.price
            
            // Coloring status
            when(order.status) {
                "Confirmed" -> holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#1976D2"))
                "Delivered" -> holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
                "Returned" -> holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#7B1FA2"))
                "Cancelled" -> holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
            }
        }

        override fun getItemCount() = orders.size
    }
}
