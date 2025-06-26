package com.example.crudApp.dto

data class ProductResponseDTO(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Double,
    val quantity: Int
)