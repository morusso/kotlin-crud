package com.example.crudApp.controller

import com.example.crudApp.dto.AuthResponseDTO
import com.example.crudApp.dto.ErrorResponseDTO
import com.example.crudApp.dto.LoginRequestDTO
import com.example.crudApp.dto.ProductRequestDTO
import com.example.crudApp.dto.ProductResponseDTO
import com.example.crudApp.dto.UserRequestDTO
import com.example.crudApp.dto.UserResponseDTO
import com.example.crudApp.entity.Product
import com.example.crudApp.service.JwtService
import com.example.crudApp.service.ProductService
import com.example.crudApp.service.UserService
import jakarta.validation.Valid
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val jwtService: JwtService
) {

    @GetMapping // GET /api/users
    fun getAllUsers(): ResponseEntity<List<UserResponseDTO>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{id}") // GET /api/users/{id}
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponseDTO> {
        val userDTO = userService.getUserById(id)
        return ResponseEntity(userDTO, HttpStatus.OK)
    }

    @PostMapping // POST /api/users
    fun createUser(@Valid @RequestBody userRequestDTO: UserRequestDTO): ResponseEntity<AuthResponseDTO> {
        val createdUserDTO = userService.createUser(userRequestDTO)
        return ResponseEntity(createdUserDTO, HttpStatus.CREATED)
    }

    @PutMapping("/{id}") // PUT /api/users/{id}
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody userRequestDTO: UserRequestDTO
    ): ResponseEntity<UserResponseDTO> {
        return try {
            val updatedUserDTO = userService.updateUser(id, userRequestDTO)
            ResponseEntity.ok(updatedUserDTO)
        } catch (ex: ChangeSetPersister.NotFoundException) {
            ResponseEntity.notFound().build() // 404
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build() // 500
        }
    }

    @DeleteMapping("/{id}") // DELETE /api/users/{id}
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            userService.deleteUser(id)
            ResponseEntity.noContent().build() // 204
        } catch (ex: ChangeSetPersister.NotFoundException) {
            ResponseEntity.notFound().build() // 404
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build() // 500
        }
    }

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody registerRequest: UserRequestDTO,
        bindingResult: BindingResult
    ): ResponseEntity<Any> {
        // Sprawdź błędy walidacji
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(
                ErrorResponseDTO(
                    message = "Błędy walidacji",
                    details = errors
                )
            )
        }

        return try {
            val authResponse = userService.createUser(registerRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(authResponse)
        } catch (e: RuntimeException) {
            ResponseEntity.badRequest().body(
                ErrorResponseDTO(message = e.message ?: "Błąd podczas rejestracji")
            )
        }
    }

    /**
     * Endpoint do logowania użytkownika
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody loginRequest: LoginRequestDTO,
        bindingResult: BindingResult
    ): ResponseEntity<Any> {
        // Sprawdź błędy walidacji
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
            return ResponseEntity.badRequest().body(
                ErrorResponseDTO(
                    message = "Błędy walidacji",
                    details = errors
                )
            )
        }

        return try {
            val authResponse = userService.loginUser(loginRequest)
            ResponseEntity.ok(authResponse)
        } catch (e: RuntimeException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = e.message ?: "Błąd logowania")
            )
        }
    }

    /**
     * Endpoint do weryfikacji tokenu
     */
    @PostMapping("/verify")
    fun verifyToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        val token = extractTokenFromHeader(authHeader)

        return if (token != null && jwtService.isTokenValid(token)) {
            val tokenInfo = jwtService.getTokenInfo(token)
            ResponseEntity.ok(mapOf(
                "valid" to true,
                "tokenInfo" to tokenInfo
            ))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "Nieprawidłowy token")
            )
        }
    }

    /**
     * Endpoint do pobierania informacji o aktualnym użytkowniku
     */
    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                ResponseEntity.ok(userInfo)
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ErrorResponseDTO(message = "Nieprawidłowy token")
                )
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "Brak tokenu autoryzacji")
            )
        }
    }

    /**
     * Endpoint do odświeżania tokenu
     */
    @PostMapping("/refresh")
    fun refreshToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Any> {
        val token = extractTokenFromHeader(authHeader)

        return if (token != null && jwtService.isTokenValid(token)) {
            val userInfo = userService.getUserFromToken(token)
            if (userInfo != null) {
                val newToken = jwtService.generateToken(
                    userId = userInfo.id.toString(),
                    username = userInfo.username,
                    email = userInfo.email,
                    expirationMinutes = 60 * 24 // 24 godziny
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
                    ErrorResponseDTO(message = "Nie można odświeżyć tokenu")
                )
            }
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponseDTO(message = "Nieprawidłowy token")
            )
        }
    }

    /**
     * Endpoint do wylogowania (invalidacja tokenu po stronie klienta)
     */
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Any> {
        return ResponseEntity.ok(mapOf("message" to "Pomyślnie wylogowano"))
    }

    /**
     * Endpoint do pobierania klucza publicznego RSA
     */
    @GetMapping("/public-key")
    fun getPublicKey(): ResponseEntity<Map<String, String>> {
        val publicKey = jwtService.getPublicKeyBase64()
        return ResponseEntity.ok(mapOf("publicKey" to publicKey))
    }

    /**
     * Wyciąga token z nagłówka Authorization
     */
    private fun extractTokenFromHeader(authHeader: String): String? {
        return if (authHeader.startsWith("Bearer ")) {
            authHeader.substring(7)
        } else {
            null
        }
    }
}