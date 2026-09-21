package com.example.prog7314p2.Models

import retrofit2.http.GET

interface ApiService {
    @GET("api/products")
    suspend fun getProducts(): ProductResponse

    @GET("api/categories")
    suspend fun getCategories(): List<Category>
}

