package com.example.crudApp.controller

import com.example.crudApp.dto.ProductRequestDTO
import com.example.crudApp.dto.ProductResponseDTO
import com.example.crudApp.dto.UserRequestDTO
import com.example.crudApp.dto.UserResponseDTO
import com.example.crudApp.entity.Product
import com.example.crudApp.service.ProductService
import com.example.crudApp.service.UserService
import jakarta.validation.Valid
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

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
    fun createUser(@Valid @RequestBody userRequestDTO: UserRequestDTO): ResponseEntity<UserResponseDTO> {
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
}