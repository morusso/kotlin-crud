package com.example.crudApp.dto

data class UserResponseDTO(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isActive: Boolean
)