package com.example.prog7314p2.Models

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") categoryId: Int? = null,
        @Query("page") page: Int = 0
    ): ProductResponse

    @GET("api/categories")
    suspend fun getCategories(): List<Category>

    @GET("api/products/{id}")
    suspend fun getProductDetails(@Path("id") id: Int): Product

    @POST("api/orders")
    suspend fun placeOrder(@Body orderRequest: OrderRequest): OrderResponse

    @GET("api/orders")
    suspend fun getOrders(): List<OrderResponse>

    @GET("api/addresses")
    suspend fun getAddresses(): List<AddressResponse>

    @POST("api/addresses")
    suspend fun createAddress(@Body request: CreateAddressRequest): AddressResponse

    @PUT("api/addresses/{id}")
    suspend fun updateAddress(@Path("id") id: Int, @Body request: CreateAddressRequest): AddressResponse

    @DELETE("api/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: Int)

    @PUT("api/orders/{id}/status")
    suspend fun updateOrderStatus(@Path("id") id: Int, @Body request: UpdateOrderStatusRequest): OrderResponse

}
