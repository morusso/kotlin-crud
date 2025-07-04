package com.example.crudApp.controller

import com.example.crudApp.dto.AuthResponseDTO
import com.example.crudApp.dto.ErrorResponseDTO
import com.example.crudApp.dto.LoginRequestDTO
import com.example.crudApp.dto.UserRequestDTO
import com.example.crudApp.service.JwtService
import com.example.crudApp.service.UserService
import com.example.crudApp.util.extractTokenFromHeader
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtService: JwtService
) {

    // register endpoint
    @PostMapping("/register") // POST /api/auth/register
    fun register(
        @Valid @RequestBody registerRequest: UserRequestDTO,
        bindingResult: BindingResult
    ): ResponseEntity<Any> {
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(
                ErrorResponseDTO(
                    message = "validation error",
                    details = errors
                )
            )
        }

        return try {
            val authResponse = userService.createUser(registerRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(authResponse)
        } catch (e: RuntimeException) {
            ResponseEntity.badRequest().body(
                ErrorResponseDTO(message = e.message ?: "registration error")
            )
        }
    }

    // login
    @PostMapping("/login")  // POST /api/auth/login
    fun login(
        @Valid @RequestBody loginRequest: LoginRequestDTO,
        bindingResult: BindingResult
    ): ResponseEntity<Any> {

        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(
                ErrorResponseDTO(
                    message = "validation error",
                    details = errors
                )
            )
        }

        return try {
            val authResponse = userService.loginUser(loginRequest)
            ResponseEntity.ok(authResponse)
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = e.message ?: "login error")
            )
        }
    }

    // validate token
    @PostMapping("/verify")  // POST /api/auth/verify
    fun verifyToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        val token = extractTokenFromHeader(authHeader)

        return if (token != null && jwtService.isTokenValid(token)) {
            val tokenInfo = jwtService.getTokenInfo(token)
            ResponseEntity.ok(
                mapOf(
                    "valid" to true,
                    "tokenInfo" to tokenInfo
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "invalid token")
            )
        }
    }

    // refresh token endpoint
    @PostMapping("/refresh")  // POST /api/auth/refresh
    fun refreshToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        val token = extractTokenFromHeader(authHeader)

        return if (token != null && jwtService.isTokenValid(token)) {
            val userInfo = userService.getUserFromToken(token)
            if (userInfo != null) {
                val newToken = jwtService.generateToken(
                    userId = userInfo.id.toString(),
                    username = userInfo.username,
                    email = userInfo.email,
                    expirationMinutes = 60 * 24 // 24 h
                )

                ResponseEntity.ok(
                    AuthResponseDTO(
                        token = newToken,
                        expiresIn = 60 * 60 * 24,
                        user = userInfo
                    )
                )
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponseDTO(message = "refresh token error")
                )
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "invalid token")
            )
        }
    }

    // logout endpoint
    @PostMapping("/logout")  // POST /api/auth/logout
    fun logout(): ResponseEntity<Any> {
        return ResponseEntity.ok(mapOf("message" to "logout successful"))
    }

    // get public key
    @GetMapping("/public-key")  // GET /api/auth/public-key
    fun getPublicKey(): ResponseEntity<Map<String, String>> {
        val publicKey = jwtService.getPublicKeyBase64()
        return ResponseEntity.ok(mapOf("publicKey" to publicKey))
    }

}