package com.example.crudApp.dto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class LoginRequestDTO(
    @field:NotBlank(message = "username cannot be blank")
    val username: String,

    @field:NotNull(message = "password cannot be null")
    val password: String,
)