package com.unify.api.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductRepository : JpaRepository<Product, Int> {
        @Query("""
        SELECT p FROM Product p 
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId) 
        AND (:search IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
    """)

    fun findWithFilters(@Param("categoryId") categoryId: Int?,
                        @Param("search") search: String?,
                        pageable: Pageable): Page<Product>
}