package com.unify.api.admin

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal

data class ValidProduct(
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val discountPrice: BigDecimal?,
    val stock: Int,
    val brand: String?,
    val categoryId: Int
)

object ProductValidator {

    const val MAX_NAME = 200
    const val MAX_BRAND = 100
    private val MAX_PRICE = BigDecimal("99999999.99")

    fun validate(r: CreateProductRequest): ValidProduct {
        val errors = mutableListOf<String>()

        val name = r.name?.trim().orEmpty()
        if (name.isEmpty()) errors += "Name is required"
        else if (name.length > MAX_NAME) errors += "Name must be $MAX_NAME characters or less"

        val price = r.price
        if (price == null) errors += "Price is required"
        else if (price <= BigDecimal.ZERO) errors += "Price must be greater than 0"
        else if (price > MAX_PRICE) errors += "Price is too large"
        else if (hasMoreThanTwoDecimals(price)) errors += "Price can have at most 2 decimal places"

        val discount = r.discountPrice
        if (discount != null) {
            when {
                discount <= BigDecimal.ZERO -> errors += "Sale price must be greater than 0"
                hasMoreThanTwoDecimals(discount) -> errors += "Sale price can have at most 2 decimal places"
                price != null && discount >= price -> errors += "Sale price must be lower than the price"
            }
        }

        val stock = r.stock
        if (stock == null) errors += "Stock is required"
        else if (stock < 0) errors += "Stock can't be negative"

        val brand = r.brand?.trim()?.ifEmpty { null }
        if (brand != null && brand.length > MAX_BRAND) errors += "Brand must be $MAX_BRAND characters or less"

        if (r.categoryId == null) errors += "Category is required"

        if (errors.isNotEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, errors.joinToString("; "))
        }

        return ValidProduct(
            name = name,
            description = r.description?.trim()?.ifEmpty { null },
            price = price!!,
            discountPrice = discount,
            stock = stock!!,
            brand = brand,
            categoryId = r.categoryId!!
        )
    }

    private fun hasMoreThanTwoDecimals(value: BigDecimal) = value.stripTrailingZeros().scale() > 2
}
