package com.example.prog7314p2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import java.util.Locale

class OrderSucessFull : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_sucess_full, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find Views
        val tvOrderId = view.findViewById<TextView>(R.id.tvSuccessOrderId)
        val tvDate = view.findViewById<TextView>(R.id.tvSuccessDate)
        val tvEta = view.findViewById<TextView>(R.id.tvSuccessEta)
        val tvTotal = view.findViewById<TextView>(R.id.tvSuccessTotal)
        val btnViewOrders = view.findViewById<Button>(R.id.ViewOrders)

        // Unpack Order Bundle
        val orderId = arguments?.getString("ORDER_ID") ?: ""
        val datePlaced = arguments?.getString("DATE_PLACED") ?: ""
        val totalCost = arguments?.getDouble("TOTAL_COST") ?: 0.0
        val eta = arguments?.getString("ETA") ?: "3-5 Business Days"

        if (orderId.isNotEmpty()) {
            tvOrderId.text = "Order #$orderId"
            tvDate.text = "Placed on: $datePlaced"
            tvEta.text = "Est. Delivery: $eta"
            tvTotal.text = String.format(Locale.getDefault(), "Total Paid: R %.2f", totalCost)
        }

        btnViewOrders.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, orderHistory())
                .commit()
        }
    }
}