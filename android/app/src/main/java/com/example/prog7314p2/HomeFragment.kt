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
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.widget.NestedScrollView
import com.example.prog7314p2.Models.Product
import com.example.prog7314p2.Models.ProductResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    private var currentSearchQuery: String? = null
    private var currentPage = 0
    private var isLoading = false
    private var searchJob: Job? = null

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
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        
        // Search Input Listener with Debounce & Job Cancellation
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel() // Cancel previous pending search
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300) // Wait 300ms after user stops typing
                    val query = s?.toString()?.trim()
                    currentSearchQuery = if (query.isNullOrEmpty()) null else query
                    currentPage = 0
                    fetchData(page = 0)
                }
            }
        })

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
        val prefs = context?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) ?: return products.shuffled()
        val topCategory = prefs.getString("TOP_CATEGORY", null)

        // If user is new / hasn't expressed a preference, randomize the discovery feed!
        if (topCategory == null) {
            return products.shuffled()
        }

        // PERSONALIZATION ALGORITHM:
        // 1. Filter items matching the user's top clicked category (shuffled among themselves)
        val preferredItems = products.filter { it.category?.name == topCategory }.shuffled()
        
        // 2. Filter remaining discovery items (shuffled so the infinite loop always feels fresh!)
        val otherItems = products.filter { it.category?.name != topCategory }.shuffled()

        // Return preferred products first, followed by randomized discovery products!
        return preferredItems + otherItems
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
                
                // Fetch real products from the API with optional Search filter
                val productResponse = RetrofitClient.instance.getProducts(search = currentSearchQuery, page = page)
                
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
        
        // IF SEARCHING: Fetch matching search results without looping
        if (!currentSearchQuery.isNullOrEmpty()) {
            isLoading = true
            currentPage++
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val productResponse = RetrofitClient.instance.getProducts(search = currentSearchQuery, page = currentPage)
                    if (productResponse.products.isNotEmpty()) {
                        val personalizedProducts = applyRecommendationAlgorithm(productResponse.products)
                        productAdapter.appendProducts(personalizedProducts)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
            return
        }

        // DEFAULT DISCOVERY FEED: Endless loop when NOT searching
        isLoading = true
        currentPage++

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Fetch next page from API with optional Search filter
                var targetPage = currentPage
                var productResponse = try {
                    RetrofitClient.instance.getProducts(search = currentSearchQuery, page = targetPage)
                } catch (ex: Exception) {
                    null
                }

                // Endless Discovery Loop: If we reached the end of the database, loop back to page 0!
                if (productResponse == null || productResponse.products.isEmpty() || targetPage >= responseTotalPages(productResponse) + 1) {
                    currentPage = 0
                    targetPage = 0
                    productResponse = RetrofitClient.instance.getProducts(search = currentSearchQuery, page = 0)
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