package com.example.prog7314p2.Models

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("page") page: Int = 0
    ): ProductResponse

    @GET("api/categories")
    suspend fun getCategories(): List<Category>

    @GET("api/products/{id}")
    suspend fun getProductDetails(@Path("id") id: Int): Product

    @POST("api/orders")
    suspend fun placeOrder(@Body orderRequest: OrderRequest): OrderResponse
}

