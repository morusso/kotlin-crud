package com.example.crudApp.dto
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UserRequestDTO(
    @field:NotBlank(message = "username cannot be blank")
    val username: String,

    @field:NotBlank(message = "e-mail cannot be blank")
    @field:Email(message = "incorrect email address format")
    val email: String,

    @field:NotNull(message = "password cannot be null")
    val password: String,

    @field:NotNull(message = "password cannot be null")
    val isActive: Boolean,

    @field:NotBlank(message = "first Name cannot be blank")
    val firstName: String,

    @field:NotNull(message = "last Name cannot be null")
    val lastName: String,
)