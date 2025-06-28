package com.example.crudApp.dto

import jakarta.validation.constraints.NotBlank

class ErrorResponseDTO(
    @field:NotBlank(message = "message cannot be blank")
    val message: String,

    @field:NotBlank(message = "timestamp cannot be blank")
    val timestamp: String = java.time.LocalDateTime.now().toString(),

    @field:NotBlank(message = "details cannot be blank")
    val details: List<String> = emptyList()
)