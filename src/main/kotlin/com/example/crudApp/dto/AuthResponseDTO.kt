package com.example.crudApp.dto

import jakarta.validation.constraints.NotBlank

data class AuthResponseDTO(
    @field:NotBlank(message = "token cannot be blank")
    val token: String,

    @field:NotBlank(message = "tokenType cannot be blank")
    val tokenType: String = "Bearer",

    @field:NotBlank(message = "expiresIn cannot be blank")
    val expiresIn: Long,

    @field:NotBlank(message = "user cannot be blank")
    val user: UserResponseDTO
)