//package com.example.crudApp
//import com.example.crudApp.controller.AuthController
//import com.example.crudApp.dto.*
//import com.example.crudApp.mapper.UserMapper
//import com.example.crudApp.repository.UserRepository
//import com.example.crudApp.service.JwtService
//import com.example.crudApp.service.UserService
//import org.junit.jupiter.api.Test
//import org.mockito.kotlin.whenever
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.http.MediaType
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.post
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import org.springframework.security.crypto.password.PasswordEncoder
//
//@WebMvcTest(AuthController::class)
//class AuthControllerTest {
//
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @MockBean
//    lateinit var userService: UserService
//
//    @MockBean
//    lateinit var jwtService: JwtService
//
//    @MockBean
//    lateinit var userMapper: UserMapper
//
//    @MockBean
//    lateinit var passwordEncoder: PasswordEncoder
//
//    @MockBean
//    lateinit var userRepository: UserRepository
//
//    private val objectMapper = jacksonObjectMapper()
//
//    @Test
//    fun `should register user successfully`() {
//        // given
//        val request = UserRequestDTO(
//            username = "testuser",
//            email = "test@example.com",
//            password = "securePassword123",
//            isActive = true,
//            firstName = "Test",
//            lastName = "User"
//        )
//
//        val expectedResponse = AuthResponseDTO(
//            token = "jwt-token",
//            expiresIn = 86400,
//            user = UserResponseDTO(
//                id = 1L,
//                username = "testuser",
//                email = "test@example.com",
//                firstName = "Test",
//                lastName = "User",
//                isActive = true
//            )
//        )
//
//        whenever(userService.createUser(request)).thenReturn(expectedResponse)
//
//        // when / then
//        mockMvc.post("/register") {
//            contentType = MediaType.APPLICATION_JSON
//            content = objectMapper.writeValueAsString(request)
//        }.andExpect {
//            status { isCreated() }
//            jsonPath("$.token") { value("jwt-token") }
//            jsonPath("$.user.username") { value("testuser") }
//        }
//    }
//}