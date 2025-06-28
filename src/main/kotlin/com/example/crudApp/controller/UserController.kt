package com.example.crudApp.controller

import com.example.crudApp.dto.ErrorResponseDTO
import com.example.crudApp.dto.UserRequestDTO
import com.example.crudApp.service.UserService
import com.example.crudApp.util.extractTokenFromHeader
import jakarta.validation.Valid
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
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
}