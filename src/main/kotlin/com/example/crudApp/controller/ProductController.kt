package com.example.crudApp.controller

import com.example.crudApp.dto.ErrorResponseDTO
import com.example.crudApp.dto.ProductRequestDTO
import com.example.crudApp.service.JwtService
import com.example.crudApp.service.ProductService
import com.example.crudApp.service.UserService
import com.example.crudApp.util.extractTokenFromHeader
import jakarta.validation.Valid
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val userService: UserService,
    private val productService: ProductService,
    private val jwtService: JwtService
) {

    @GetMapping // GET /api/products
    fun getAllProducts(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {

        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                val products = productService.getAllProducts()
                return ResponseEntity.ok(products)
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

    @GetMapping("/{id}") // GET /api/products/{id}
    fun getProductById(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable id: Long
    ): ResponseEntity<Any> {


        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                val productDTO = productService.getProductById(id)
                return ResponseEntity(productDTO, HttpStatus.OK)

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

    @PostMapping // POST /api/products
    fun createProduct(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody productRequestDTO: ProductRequestDTO
    ): ResponseEntity<Any> {

        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                val createdProductDTO = productService.createProduct(productRequestDTO)
                return ResponseEntity(createdProductDTO, HttpStatus.CREATED)


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

    @PutMapping("/{id}") // PUT /api/products/{id}
    fun updateProduct(
        @RequestHeader("Authorization") authHeader: String,

        @PathVariable id: Long,
        @Valid @RequestBody productRequestDTO: ProductRequestDTO
    ): ResponseEntity<Any> {


        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                return try {
                    val updatedProductDTO = productService.updateProduct(id, productRequestDTO)
                    ResponseEntity.ok(updatedProductDTO)
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

    @DeleteMapping("/{id}") // DELETE /api/products/{id}
    fun deleteProduct(
        @RequestHeader("Authorization") authHeader: String,

        @PathVariable id: Long
    ): ResponseEntity<Any?> {


        val token = extractTokenFromHeader(authHeader)

        return if (token != null) {
            val userInfo = userService.validateTokenAndGetUser(token)
            if (userInfo != null) {
                return try {
                    productService.deleteProduct(id)
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
}