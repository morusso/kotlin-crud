package com.example.crudApp.dto
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UserRequestDTO(
    @field:NotBlank(message = "E-mail cannot be blank")
    @field:Email(message = "Incorrect email address format")
    val email: String,

    @field:NotNull(message = "password cannot be null")
    val password: String,

    @field:NotNull(message = "password cannot be null")
    val isActive: Boolean,

    @field:NotBlank(message = "First Name cannot be blank")
    val firstName: String,

    @field:NotNull(message = "Last Name cannot be null")
    val lastName: String,
)