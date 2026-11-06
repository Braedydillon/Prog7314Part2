package com.example.prog7314p2

import RetrofitClient
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.prog7314p2.Models.OrderResponse
import com.example.prog7314p2.Models.UpdateOrderStatusRequest
import com.example.prog7314p2.util.serverMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class AdminOrdersFragment : Fragment() {

    private lateinit var rvAdminOrders: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var layoutEmptyState: View
    private lateinit var layoutErrorState: View
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnRetry: Button

    private val ordersList = mutableListOf<OrderResponse>()
    private lateinit var adapter: AdminOrdersAdapter

    private val availableStatuses = arrayOf("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAdminOrders = view.findViewById(R.id.rvAdminOrders)
        swipeRefresh = view.findViewById(R.id.swipeRefreshAdminOrders)
        progressIndicator = view.findViewById(R.id.progressAdminOrders)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
        layoutErrorState = view.findViewById(R.id.layoutErrorState)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        btnRetry = view.findViewById(R.id.btnRetryAdminOrders)

        adapter = AdminOrdersAdapter(ordersList) { order ->
            showStatusSelectionDialog(order)
        }
        rvAdminOrders.adapter = adapter

        swipeRefresh.setOnRefreshListener {
            fetchOrders(isPullToRefresh = true)
        }

        btnRetry.setOnClickListener {
            fetchOrders(isPullToRefresh = false)
        }

        fetchOrders(isPullToRefresh = false)
    }

    private fun fetchOrders(isPullToRefresh: Boolean) {
        if (!isPullToRefresh) {
            progressIndicator.visibility = View.VISIBLE
        }
        layoutEmptyState.visibility = View.GONE
        layoutErrorState.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val fetchedOrders = try {
                    RetrofitClient.instance.getAllAdminOrders()
                } catch (e: HttpException) {
                    if (e.code() == 404) {
                        RetrofitClient.instance.getOrders()
                    } else {
                        throw e
                    }
                }

                if (!isAdded || view == null) return@launch

                progressIndicator.visibility = View.GONE
                swipeRefresh.isRefreshing = false

                if (fetchedOrders.isEmpty()) {
                    ordersList.clear()
                    adapter.updateData(emptyList())
                    layoutEmptyState.visibility = View.VISIBLE
                } else {
                    ordersList.clear()
                    ordersList.addAll(fetchedOrders)
                    adapter.updateData(fetchedOrders)
                    layoutEmptyState.visibility = View.GONE
                    rvAdminOrders.visibility = View.VISIBLE
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!isAdded || view == null) return@launch

                Log.e("AdminOrders", "Failed to fetch orders", e)
                progressIndicator.visibility = View.GONE
                swipeRefresh.isRefreshing = false

                val errorText = e.serverMessage() ?: getString(R.string.admin_orders_failed)
                tvErrorMessage.text = errorText
                layoutErrorState.visibility = View.VISIBLE
            }
        }
    }

    private fun showStatusSelectionDialog(order: OrderResponse) {
        val currentStatusUpper = (order.status ?: "PENDING").uppercase()
        val currentIndex = availableStatuses.indexOfFirst { it == currentStatusUpper }.takeIf { it >= 0 } ?: 0
        var selectedIndex = currentIndex

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.admin_select_status))
            .setSingleChoiceItems(availableStatuses, currentIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Next") { dialog, _ ->
                dialog.dismiss()
                val selectedStatus = availableStatuses[selectedIndex]
                if (selectedStatus != order.status) {
                    showConfirmationDialog(order, selectedStatus)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showConfirmationDialog(order: OrderResponse, newStatus: String) {
        val confirmMsg = getString(R.string.admin_confirm_status_change, order.id.toString(), newStatus)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.admin_change_status))
            .setMessage(confirmMsg)
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                performStatusUpdate(order.id, newStatus)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performStatusUpdate(orderId: Int, newStatus: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val updatedOrder = try {
                    // Try 1: All-fields JSON object uppercase ("SHIPPED")
                    RetrofitClient.instance.updateOrderStatus(
                        id = orderId,
                        request = UpdateOrderStatusRequest(status = newStatus)
                    )
                } catch (e1: HttpException) {
                    if (e1.code() == 400) {
                        try {
                            // Try 2: All-fields JSON object lowercase ("shipped")
                            RetrofitClient.instance.updateOrderStatus(
                                id = orderId,
                                request = UpdateOrderStatusRequest(status = newStatus.lowercase())
                            )
                        } catch (e2: HttpException) {
                            if (e2.code() == 400) {
                                // Try 3: All-fields JSON object capitalized ("Shipped")
                                val capitalized = newStatus.lowercase().replaceFirstChar { it.uppercase() }
                                RetrofitClient.instance.updateOrderStatus(
                                    id = orderId,
                                    request = UpdateOrderStatusRequest(status = capitalized)
                                )
                            } else throw e2
                        }
                    } else throw e1
                }

                if (!isAdded || view == null) return@launch

                val finalStatus = updatedOrder.status ?: newStatus
                adapter.updateOrderStatus(orderId, finalStatus)

                val successMsg = getString(R.string.admin_status_updated, orderId.toString(), finalStatus)
                Toast.makeText(requireContext(), successMsg, Toast.LENGTH_SHORT).show()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!isAdded || view == null) return@launch
                Log.e("AdminOrders", "Status update failed", e)
                val errorMsg = e.serverMessage() ?: getString(R.string.admin_status_update_failed)
                
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Status Update Error")
                    .setMessage(errorMsg)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}
