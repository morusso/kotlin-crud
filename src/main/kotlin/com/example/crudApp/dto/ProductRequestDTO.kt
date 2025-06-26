package com.example.crudApp.dto
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ProductRequestDTO(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    val description: String?,
    @field:NotNull(message = "Price cannot be null")
    @field:Min(value = 0, message = "Price must be non-negative")
    val price: Double,

    @field:NotNull(message = "Quantity cannot be null")
    @field:Min(value = 0, message = "Quantity must be non-negative")
    val quantity: Int
)