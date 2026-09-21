package com.example.prog7314p2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import RetrofitClient
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        rvProducts = view.findViewById(R.id.rvProducts)
        
        // Start with an empty list
        productAdapter = ProductAdapter(emptyList())
        rvProducts.adapter = productAdapter

        // Fetch Data from DB (Spring Boot API)
        fetchData()
    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Fetch Products
                val productResponse = RetrofitClient.instance.getProducts()
                val products = productResponse.products
                
                // Update the adapter with the fresh products!
                productAdapter.updateData(products)

            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}