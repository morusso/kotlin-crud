package com.example.crudApp.service

import com.example.crudApp.dto.AuthResponseDTO
import com.example.crudApp.dto.LoginRequestDTO
import com.example.crudApp.dto.UserRequestDTO
import com.example.crudApp.dto.UserResponseDTO
import com.example.crudApp.mapper.UserMapper
import com.example.crudApp.entity.User
import com.example.crudApp.repository.UserRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userMapper: UserMapper
    private lateinit var jwtService: JwtService
    private lateinit var userService: UserService

    private val passwordEncoder = BCryptPasswordEncoder()

    @BeforeEach
    fun setUp() {
        userRepository = mock<UserRepository>()
        userMapper = mock<UserMapper>()
        jwtService = mock<JwtService>()
        userService = UserService(userRepository, userMapper, jwtService)
    }

    // Test data
    private fun createUserRequestDTO() = UserRequestDTO(
        username = "testuser",
        email = "test@example.com",
        password = "password123",
        firstName = "John",
        lastName = "Doe",
        isActive = true
    )

    private fun createUser(id: Long = 1L) = User(
        id = id,
        username = "testuser",
        email = "test@example.com",
        password = "encodedPassword",
        firstName = "John",
        lastName = "Doe",
        isActive = true
    )

    private fun createUserResponseDTO(id: Long = 1L) = UserResponseDTO(
        id = id,
        username = "testuser",
        email = "test@example.com",
        firstName = "John",
        lastName = "Doe",
        isActive = true
    )

    // createUser tests
    @Test
    fun `createUser should create user successfully when valid data provided`() {
        // Given
        val userRequestDTO = createUserRequestDTO()
        val user = createUser()
        val savedUser = createUser()
        val userResponseDTO = createUserResponseDTO()
        val token = "jwt-token"

        whenever(userRepository.existsByUsername(userRequestDTO.username)).thenReturn(false)
        whenever(userRepository.existsByEmail(userRequestDTO.email)).thenReturn(false)
        whenever(userMapper.toEntity(userRequestDTO)).thenReturn(user)
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)
        whenever(
            jwtService.generateToken(
                userId = savedUser.id.toString(),
                username = savedUser.username,
                email = savedUser.email,
                expirationMinutes = 60 * 24
            )
        ).thenReturn(token)

        // When
        val result = userService.createUser(userRequestDTO)

        // Then
        assertThat(result.token).isEqualTo(token)
        assertThat(result.expiresIn).isEqualTo(60 * 60 * 24)
        assertThat(result.user.username).isEqualTo(savedUser.username)
        assertThat(result.user.email).isEqualTo(savedUser.email)

        verify(userRepository).existsByUsername(userRequestDTO.username)
        verify(userRepository).existsByEmail(userRequestDTO.email)
        verify(userMapper).toEntity(userRequestDTO)
        verify(userRepository).save(any<User>())
        verify(jwtService).generateToken(
            userId = savedUser.id.toString(),
            username = savedUser.username,
            email = savedUser.email,
            expirationMinutes = 60 * 24
        )
    }

    @Test
    fun `createUser should throw exception when username already exists`() {
        // Given
        val userRequestDTO = createUserRequestDTO()
        whenever(userRepository.existsByUsername(userRequestDTO.username)).thenReturn(true)

        // When & Then
        assertThatThrownBy { userService.createUser(userRequestDTO) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("A user with this username already exists")

        verify(userRepository).existsByUsername(userRequestDTO.username)
        verify(userRepository, never()).existsByEmail(any())
        verify(userRepository, never()).save(any<User>())
    }

    @Test
    fun `createUser should throw exception when email already exists`() {
        // Given
        val userRequestDTO = createUserRequestDTO()
        whenever(userRepository.existsByUsername(userRequestDTO.username)).thenReturn(false)
        whenever(userRepository.existsByEmail(userRequestDTO.email)).thenReturn(true)

        // When & Then
        assertThatThrownBy { userService.createUser(userRequestDTO) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("A user with this e-mail already exists")

        verify(userRepository).existsByUsername(userRequestDTO.username)
        verify(userRepository).existsByEmail(userRequestDTO.email)
        verify(userRepository, never()).save(any<User>())
    }

    // loginUser tests
    @Test
    fun `loginUser should login successfully with valid credentials`() {
        // Given
        val loginRequest = LoginRequestDTO(username = "testuser", password = "password123")
        val user = createUser().copy(password = passwordEncoder.encode("password123"))
        val token = "jwt-token"

        whenever(userRepository.findByUsername(loginRequest.username)).thenReturn(Optional.of(user))
        whenever(
            jwtService.generateToken(
                userId = user.id.toString(),
                username = user.username,
                email = user.email,
                expirationMinutes = 60 * 24
            )
        ).thenReturn(token)

        // When
        val result = userService.loginUser(loginRequest)

        // Then
        assertThat(result.token).isEqualTo(token)
        assertThat(result.expiresIn).isEqualTo(60 * 60 * 24)
        assertThat(result.user.username).isEqualTo(user.username)

        verify(userRepository).findByUsername(loginRequest.username)
        verify(jwtService).generateToken(
            userId = user.id.toString(),
            username = user.username,
            email = user.email,
            expirationMinutes = 60 * 24
        )
    }

    @Test
    fun `loginUser should throw exception when user not found`() {
        // Given
        val loginRequest = LoginRequestDTO(username = "nonexistent", password = "password123")
        whenever(userRepository.findByUsername(loginRequest.username)).thenReturn(Optional.empty())

        // When & Then
        assertThatThrownBy { userService.loginUser(loginRequest) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Invalid login or password")

        verify(userRepository).findByUsername(loginRequest.username)
        verify(jwtService, never()).generateToken(any(), any(), any(), any())
    }

    @Test
    fun `loginUser should throw exception when password is incorrect`() {
        // Given
        val loginRequest = LoginRequestDTO(username = "testuser", password = "wrongpassword")
        val user = createUser().copy(password = passwordEncoder.encode("correctpassword"))

        whenever(userRepository.findByUsername(loginRequest.username)).thenReturn(Optional.of(user))

        // When & Then
        assertThatThrownBy { userService.loginUser(loginRequest) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Invalid login or password")

        verify(userRepository).findByUsername(loginRequest.username)
        verify(jwtService, never()).generateToken(any(), any(), any(), any())
    }

    @Test
    fun `loginUser should throw exception when account is not active`() {
        // Given
        val loginRequest = LoginRequestDTO(username = "testuser", password = "password123")
        val user = createUser().copy(
            password = passwordEncoder.encode("password123"),
            isActive = false
        )

        whenever(userRepository.findByUsername(loginRequest.username)).thenReturn(Optional.of(user))

        // When & Then
        assertThatThrownBy { userService.loginUser(loginRequest) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessage("Account not active")

        verify(userRepository).findByUsername(loginRequest.username)
        verify(jwtService, never()).generateToken(any(), any(), any(), any())
    }

    // getUserFromToken tests
    @Test
    fun `getUserFromToken should return user when valid token provided`() {
        // Given
        val token = "valid-token"
        val userId = "1"
        val user = createUser()

        whenever(jwtService.getUserIdFromToken(token)).thenReturn(userId)
        whenever(userRepository.findById(userId.toLong())).thenReturn(Optional.of(user))

        // When
        val result = userService.getUserFromToken(token)

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.id).isEqualTo(user.id)
        assertThat(result.username).isEqualTo(user.username)

        verify(jwtService).getUserIdFromToken(token)
        verify(userRepository).findById(userId.toLong())
    }

    @Test
    fun `getUserFromToken should return null when token is invalid`() {
        // Given
        val token = "invalid-token"
        whenever(jwtService.getUserIdFromToken(token)).thenReturn(null)

        // When
        val result = userService.getUserFromToken(token)

        // Then
        assertThat(result).isNull()

        verify(jwtService).getUserIdFromToken(token)
        verify(userRepository, never()).findById(any())
    }

    @Test
    fun `getUserFromToken should return null when user not found`() {
        // Given
        val token = "valid-token"
        val userId = "999"

        whenever(jwtService.getUserIdFromToken(token)).thenReturn(userId)
        whenever(userRepository.findById(userId.toLong())).thenReturn(Optional.empty())

        // When
        val result = userService.getUserFromToken(token)

        // Then
        assertThat(result).isNull()

        verify(jwtService).getUserIdFromToken(token)
        verify(userRepository).findById(userId.toLong())
    }

    // validateTokenAndGetUser tests
    @Test
    fun `validateTokenAndGetUser should return user when token is valid`() {
        // Given
        val token = "valid-token"
        val userId = "1"
        val user = createUser()

        whenever(jwtService.isTokenValid(token)).thenReturn(true)
        whenever(jwtService.getUserIdFromToken(token)).thenReturn(userId)
        whenever(userRepository.findById(userId.toLong())).thenReturn(Optional.of(user))

        // When
        val result = userService.validateTokenAndGetUser(token)

        // Then
        assertThat(result).isNotNull
        assertThat(result!!.username).isEqualTo(user.username)

        verify(jwtService).isTokenValid(token)
        verify(jwtService).getUserIdFromToken(token)
        verify(userRepository).findById(userId.toLong())
    }

    @Test
    fun `validateTokenAndGetUser should return null when token is invalid`() {
        // Given
        val token = "invalid-token"
        whenever(jwtService.isTokenValid(token)).thenReturn(false)

        // When
        val result = userService.validateTokenAndGetUser(token)

        // Then
        assertThat(result).isNull()

        verify(jwtService).isTokenValid(token)
        verify(jwtService, never()).getUserIdFromToken(any())
    }

    // getUserById tests
    @Test
    fun `getUserById should return user when user exists`() {
        // Given
        val userId = 1L
        val user = createUser(userId)
        val userResponseDTO = createUserResponseDTO(userId)

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(userMapper.toDto(user)).thenReturn(userResponseDTO)

        // When
        val result = userService.getUserById(userId)

        // Then
        assertThat(result).isEqualTo(userResponseDTO)

        verify(userRepository).findById(userId)
        verify(userMapper).toDto(user)
    }

    @Test
    fun `getUserById should throw NotFoundException when user not exists`() {
        // Given
        val userId = 999L
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // When & Then
        assertThatThrownBy { userService.getUserById(userId) }
            .isInstanceOf(NotFoundException::class.java)

        verify(userRepository).findById(userId)
        verify(userMapper, never()).toDto(any())
    }

    // getAllUsers tests
    @Test
    fun `getAllUsers should return list of users`() {
        // Given
        val users = listOf(createUser(1L), createUser(2L))
        val userResponseDTOs = listOf(createUserResponseDTO(1L), createUserResponseDTO(2L))

        whenever(userRepository.findAll()).thenReturn(users)
        whenever(userMapper.toDto(users[0])).thenReturn(userResponseDTOs[0])
        whenever(userMapper.toDto(users[1])).thenReturn(userResponseDTOs[1])

        // When
        val result = userService.getAllUsers()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result).containsExactlyElementsOf(userResponseDTOs)

        verify(userRepository).findAll()
        verify(userMapper).toDto(users[0])
        verify(userMapper).toDto(users[1])
    }

    @Test
    fun `getAllUsers should return empty list when no users exist`() {
        // Given
        whenever(userRepository.findAll()).thenReturn(emptyList())

        // When
        val result = userService.getAllUsers()

        // Then
        assertThat(result).isEmpty()

        verify(userRepository).findAll()
        verify(userMapper, never()).toDto(any())
    }

    // updateUser tests
    @Test
    fun `updateUser should update user successfully`() {
        // Given
        val userId = 1L
        val userRequestDTO = createUserRequestDTO().copy(username = "updateduser")
        val existingUser = createUser(userId)
        val updatedUser = existingUser.copy(username = "updateduser")
        val userResponseDTO = createUserResponseDTO(userId).copy(username = "updateduser")

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(existingUser))
        whenever(userRepository.save(any<User>())).thenReturn(updatedUser)
        whenever(userMapper.toDto(updatedUser)).thenReturn(userResponseDTO)

        // When
        val result = userService.updateUser(userId, userRequestDTO)

        // Then
        assertThat(result.username).isEqualTo("updateduser")

        verify(userRepository).findById(userId)
        verify(userRepository).save(any<User>())
        verify(userMapper).toDto(updatedUser)
    }

    @Test
    fun `updateUser should throw NotFoundException when user not exists`() {
        // Given
        val userId = 999L
        val userRequestDTO = createUserRequestDTO()
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // When & Then
        assertThatThrownBy { userService.updateUser(userId, userRequestDTO) }
            .isInstanceOf(NotFoundException::class.java)

        verify(userRepository).findById(userId)
        verify(userRepository, never()).save(any<User>())
    }

    // deleteUser tests
    @Test
    fun `deleteUser should delete user successfully`() {
        // Given
        val userId = 1L

        // When
        userService.deleteUser(userId)

        // Then
        verify(userRepository).deleteById(userId)
    }
}