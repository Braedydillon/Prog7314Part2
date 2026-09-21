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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
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
        productAdapter = ProductAdapter(emptyList()) { clickedProduct ->
            val detailsFragment = ProductDetailsFragment()
            val bundle = Bundle()
            
            // Pack the product into the bundle
            bundle.putSerializable("PRODUCT_DATA", clickedProduct)
            detailsFragment.arguments = bundle
            
            // Navigate to the Details Fragment and add to backstack
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailsFragment)
                .addToBackStack(null) // This lets the user press 'back' to go to home
                .commit()
        }
        rvProducts.adapter = productAdapter

        // Fetch Data from DB (Spring Boot API)
        fetchData()
    }

    private fun fetchData() {
        // Use a standard CoroutineScope attached to the ViewLifecycle, but without strict cancellation
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Let's add a tiny delay to ensure the view is fully attached before making the call
                delay(100)
                
                // Fetch Products
                val productResponse = RetrofitClient.instance.getProducts()
                val products = productResponse.products
                
                // Update the adapter with the fresh products!
                productAdapter.updateData(products)

            } catch (e: CancellationException) {
                // Ignore this. This happens naturally when you switch tabs quickly!
                throw e
            } catch (e: Exception) {
                // If it fails, print the exact error to the console so we can see what really happened
                e.printStackTrace()
                Toast.makeText(context, "Failed to load data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}