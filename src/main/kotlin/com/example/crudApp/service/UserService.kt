package com.example.crudApp.service

import com.example.crudApp.dto.AuthResponseDTO
import com.example.crudApp.dto.LoginRequestDTO
import com.example.crudApp.dto.UserRequestDTO
import com.example.crudApp.dto.UserResponseDTO
import com.example.crudApp.mapper.UserMapper
import com.example.crudApp.repository.UserRepository
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val jwtService: JwtService
) {

    private val passwordEncoder = BCryptPasswordEncoder()

    fun createUser(userRequestDTO: UserRequestDTO): AuthResponseDTO {
        if (userRepository.existsByUsername(userRequestDTO.username)) {
            throw RuntimeException("A user with this username already exists")
        }

        if (userRepository.existsByEmail(userRequestDTO.email)) {
            throw RuntimeException("A user with this e-mail already exists")
        }

        val encodedPassword = passwordEncoder.encode(userRequestDTO.password)

        val user = userMapper.toEntity(userRequestDTO)
        val savedUser = userRepository.save(user)

        val token = jwtService.generateToken(
            userId = savedUser.id.toString(),
            username = savedUser.username,
            email = savedUser.email,
            expirationMinutes = 60 * 24 // 24 h
        )

        return AuthResponseDTO(
            token = token,
            expiresIn = 60 * 60 * 24, // 24 h
            user = UserResponseDTO(
                id = savedUser.id,
                username = savedUser.username,
                email = savedUser.email,
                firstName = savedUser.firstName,
                lastName = savedUser.lastName,
                isActive = savedUser.isActive,
            )
        )
    }

    fun loginUser(loginRequest: LoginRequestDTO): AuthResponseDTO {
        // Znajdź użytkownika po nazwie
        val user = userRepository.findByUsername(loginRequest.username)
            .orElseThrow { RuntimeException("Nieprawidłowa nazwa użytkownika lub hasło") }

        // Sprawdź hasło
        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            throw RuntimeException("Nieprawidłowa nazwa użytkownika lub hasło")
        }

        // Sprawdź czy konto jest aktywne
        if (!user.isActive) {
            throw RuntimeException("Konto użytkownika jest nieaktywne")
        }

        // Wygeneruj token JWT
        val token = jwtService.generateToken(
            userId = user.id.toString(),
            username = user.username,
            email = user.email,
            expirationMinutes = 60 * 24 // 24 godziny
        )

        return AuthResponseDTO(
            token = token,
            expiresIn = 60 * 60 * 24, // 24 godziny w sekundach
            user = UserResponseDTO(
                id = user.id,
                username = user.username,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                isActive = user.isActive
            )
        )
    }

    fun getUserFromToken(token: String): UserResponseDTO? {
        val userId = jwtService.getUserIdFromToken(token) ?: return null

        val user = userRepository.findById(userId.toLong())
            .orElse(null) ?: return null

        return UserResponseDTO(
            id = user.id,
            username = user.username,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            isActive = user.isActive
        )
    }

    fun validateTokenAndGetUser(token: String): UserResponseDTO? {
        return if (jwtService.isTokenValid(token)) {
            getUserFromToken(token)
        } else {
            null
        }
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
            username = userRequestDTO.username,
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