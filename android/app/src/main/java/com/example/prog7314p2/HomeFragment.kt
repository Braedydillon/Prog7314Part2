package com.example.prog7314p2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import RetrofitClient
import androidx.core.widget.NestedScrollView
import com.example.prog7314p2.Models.Product
import com.example.prog7314p2.Models.ProductResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    private var currentPage = 0
    private var isLoading = false
    private var isLastPage = false

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
            // 1. Track User Interaction for the Recommendation Algorithm!
            trackCategoryClick(clickedProduct)

            val detailsFragment = ProductDetailsFragment()
            val bundle = Bundle()
            
            // Pack the product into the bundle
            bundle.putSerializable("PRODUCT_DATA", clickedProduct)
            detailsFragment.arguments = bundle
            
            // Navigate to the Details Fragment and add to backstack
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailsFragment)
                .addToBackStack(null)
                .commit()
        }
        rvProducts.adapter = productAdapter

        // Enable Infinite Scrolling (Native View Recycling)
        setupInfiniteScroll()

        // Fetch Data from DB (Spring Boot API)
        fetchData(page = 0)
    }

    private fun trackCategoryClick(product: Product) {
        val categoryName = product.category?.name ?: return
        val prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        
        // Track how many times this category was clicked
        val currentCount = prefs.getInt("CAT_COUNT_$categoryName", 0)
        prefs.edit().putInt("CAT_COUNT_$categoryName", currentCount + 1).apply()
        
        // Save as Top Preferred Category
        prefs.edit().putString("TOP_CATEGORY", categoryName).apply()
    }

    private fun applyRecommendationAlgorithm(products: List<Product>): List<Product> {
        val prefs = context?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) ?: return products
        val topCategory = prefs.getString("TOP_CATEGORY", null) ?: return products

        // Personalization Algorithm: Sort so items matching user's top category appear first!
        return products.sortedByDescending { it.category?.name == topCategory }
    }

    private fun setupInfiniteScroll() {
        rvProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as? GridLayoutManager ?: return
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Trigger pre-fetch when user is within 4 items of the bottom
                if (!isLoading && totalItemCount <= (lastVisibleItem + 4)) {
                    loadMoreProducts()
                }
            }
        })
    }

    private fun fetchData(page: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                isLoading = true
                
                // Fetch real products from the API
                val productResponse = RetrofitClient.instance.getProducts(page = page)
                
                // Apply Recommendation/Personalization Algorithm
                val personalizedProducts = applyRecommendationAlgorithm(productResponse.products)
                
                // Update the adapter instantly
                productAdapter.updateData(personalizedProducts)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun loadMoreProducts() {
        if (isLoading) return
        
        isLoading = true
        currentPage++

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Fetch next page from API
                var targetPage = currentPage
                var productResponse = try {
                    RetrofitClient.instance.getProducts(page = targetPage)
                } catch (ex: Exception) {
                    null
                }

                // Endless Discovery Loop: If we reached the end of the database, loop back to page 0!
                if (productResponse == null || productResponse.products.isEmpty() || targetPage >= responseTotalPages(productResponse) + 1) {
                    currentPage = 0
                    targetPage = 0
                    productResponse = RetrofitClient.instance.getProducts(page = 0)
                }

                if (productResponse != null && productResponse.products.isNotEmpty()) {
                    val personalizedProducts = applyRecommendationAlgorithm(productResponse.products)
                    productAdapter.appendProducts(personalizedProducts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun responseTotalPages(response: ProductResponse): Int {
        return response.totalPages - 1
    }
}