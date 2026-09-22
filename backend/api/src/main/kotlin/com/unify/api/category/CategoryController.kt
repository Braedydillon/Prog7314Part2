package com.unify.api.category

import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/categories")
class CategoryController(val categoryRepository: CategoryRepository) {

    @GetMapping
    fun findAll(): List<CategoryResponse> = categoryRepository.findAll().map { it.toResponse() }

    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable("id") id: Int): ResponseEntity<CategoryResponse>? {
        return categoryRepository.findById(id)
            .map { ResponseEntity.ok(it.toResponse()) }
            .orElseThrow{ (ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")) }
    }
}
