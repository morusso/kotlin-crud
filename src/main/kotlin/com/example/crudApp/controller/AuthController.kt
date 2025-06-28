package com.example.crudApp.controller

import com.example.crudApp.dto.AuthResponseDTO
import com.example.crudApp.dto.ErrorResponseDTO
import com.example.crudApp.dto.LoginRequestDTO
import com.example.crudApp.dto.UserRequestDTO
import com.example.crudApp.service.JwtService
import com.example.crudApp.service.UserService
import com.example.crudApp.util.extractTokenFromHeader
import jakarta.validation.Valid
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class AuthController(
    private val userService: UserService,
    private val jwtService: JwtService
) {

    @GetMapping // GET /api/users
    fun getAllUsers(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {
        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                val users = userService.getAllUsers()
                ResponseEntity.ok(users)
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponseDTO(message = "invalid token")
                )
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "missing authorization token")
            )
        }
    }


    @GetMapping("/{id}") // GET /api/users/{id}
    fun getUserById(@RequestHeader("Authorization") authHeader: String, @PathVariable id: Long): ResponseEntity<Any> {
        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                val userDTO = userService.getUserById(id)
                return ResponseEntity(userDTO, HttpStatus.OK)
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponseDTO(message = "invalid token")
                )
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "missing authorization token")
            )
        }
    }


    @PutMapping("/{id}") // PUT /api/users/{id}
    fun updateUser(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable id: Long,
        @Valid @RequestBody userRequestDTO: UserRequestDTO
    ): ResponseEntity<Any> {

        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                return try {
                    val updatedUserDTO = userService.updateUser(id, userRequestDTO)
                    ResponseEntity.ok(updatedUserDTO)
                } catch (ex: ChangeSetPersister.NotFoundException) {
                    ResponseEntity.notFound().build() // 404
                } catch (ex: Exception) {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build() // 500
                }
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponseDTO(message = "invalid token")
                )
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "missing authorization token")
            )
        }


    }

    @DeleteMapping("/{id}") // DELETE /api/users/{id}
    fun deleteUser(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable id: Long
    ): ResponseEntity<Any?> {

        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                return try {
                    userService.deleteUser(id)
                    ResponseEntity.noContent().build() // 204
                } catch (ex: ChangeSetPersister.NotFoundException) {
                    ResponseEntity.notFound().build() // 404
                } catch (ex: Exception) {
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build() // 500
                }
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponseDTO(message = "invalid token")
                )
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "missing authorization token")
            )
        }
    }

    // register endpoint
    @PostMapping("/register") // POST /api/users/register
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
    @PostMapping("/login")  // POST /api/users/login
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
    @PostMapping("/verify")  // POST /api/users/verify
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

    // get user profile endpoint
    @GetMapping("/me")  // POST /api/users/me
    fun getCurrentUser(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                ResponseEntity.ok(userInfo)
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponseDTO(message = "invalid token")
                )
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "missing authorization token")
            )
        }
    }

    // refresh token endpoint
    @PostMapping("/refresh")  // POST /api/users/refresh
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
    @PostMapping("/logout")  // POST /api/users/logout
    fun logout(): ResponseEntity<Any> {
        return ResponseEntity.ok(mapOf("message" to "logout successful"))
    }

    // get public key
    @GetMapping("/public-key")  // GET /api/users/public-key
    fun getPublicKey(): ResponseEntity<Map<String, String>> {
        val publicKey = jwtService.getPublicKeyBase64()
        return ResponseEntity.ok(mapOf("publicKey" to publicKey))
    }

}