package com.example.crudApp.mapper

import com.example.crudApp.dto.UserRequestDTO
import com.example.crudApp.dto.UserResponseDTO
import com.example.crudApp.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toEntity(dto: UserRequestDTO): User {
        return User(
            email = dto.email,
            password = dto.password,
            isActive = dto.isActive,
            firstName = dto.firstName,
            lastName = dto.lastName
        )
    }

    fun toDto(entity: User): UserResponseDTO {
        return UserResponseDTO(
            id = entity.id,
            email = entity.email,
            isActive = entity.isActive,
            firstName = entity.firstName,
            lastName = entity.lastName
        )
    }
}