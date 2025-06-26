package com.example.crudApp.service

import com.example.crudApp.dto.UserRequestDTO
import com.example.crudApp.dto.UserResponseDTO
import com.example.crudApp.mapper.UserMapper
import com.example.crudApp.repository.UserRepository
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {

    fun createUser(userRequestDTO: UserRequestDTO): UserResponseDTO {
        val user = userMapper.toEntity(userRequestDTO)
        val savedUser = userRepository.save(user)

        return userMapper.toDto(savedUser)
    }

    fun getUserById(id: Long): UserResponseDTO {
        val user = userRepository.findById(id)
            .orElseThrow { NotFoundException() }

        return userMapper.toDto(user)
    }

    fun getAllUsers(): List<UserResponseDTO> {
        return userRepository.findAll()
            .stream()
            .map { user -> userMapper.toDto(user) }
            .collect(Collectors.toList())
    }

    fun updateUser(id: Long, userRequestDTO: UserRequestDTO): UserResponseDTO {
        val existingUser = userRepository.findById(id)
            .orElseThrow { NotFoundException() }

        val updatedUser = existingUser.copy(
            email = userRequestDTO.email,
            firstName = userRequestDTO.firstName,
            lastName = userRequestDTO.lastName,
            isActive = userRequestDTO.isActive
        )
        val savedProduct = userRepository.save(updatedUser)
        return userMapper.toDto(savedProduct)
    }

    fun deleteUser(id: Long) {
        userRepository.deleteById(id)
    }
}