package com.example.crudApp.controller

import com.example.crudApp.dto.ProductRequestDTO
import com.example.crudApp.dto.ProductResponseDTO
import com.example.crudApp.entity.Product
import com.example.crudApp.service.ProductService
import jakarta.validation.Valid
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(private val productService: ProductService) {

    @GetMapping // GET /api/products
    fun getAllProducts(): ResponseEntity<List<ProductResponseDTO>> {
        val products = productService.getAllProducts()
        return ResponseEntity.ok(products)
    }

    @GetMapping("/{id}") // GET /api/products/{id}
    fun getProductById(@PathVariable id: Long): ResponseEntity<ProductResponseDTO> {
        val productDTO = productService.getProductById(id)
        return ResponseEntity(productDTO, HttpStatus.OK)
    }

    @PostMapping // POST /api/products
    fun createProduct(@Valid @RequestBody productRequestDTO: ProductRequestDTO): ResponseEntity<ProductResponseDTO> {
        val createdProductDTO = productService.createProduct(productRequestDTO)
        return ResponseEntity(createdProductDTO, HttpStatus.CREATED)
    }

    @PutMapping("/{id}") // PUT /api/products/{id}
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody productRequestDTO: ProductRequestDTO
    ): ResponseEntity<ProductResponseDTO> {
        return try {
            val updatedProductDTO = productService.updateProduct(id, productRequestDTO)
            ResponseEntity.ok(updatedProductDTO)
        } catch (ex: ChangeSetPersister.NotFoundException) {
            ResponseEntity.notFound().build() // 404
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build() // 500
        }
    }

    @DeleteMapping("/{id}") // DELETE /api/products/{id}
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        return try {
            productService.deleteProduct(id)
            ResponseEntity.noContent().build() // 204
        } catch (ex: ChangeSetPersister.NotFoundException) {
            ResponseEntity.notFound().build() // 404
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build() // 500
        }
    }
}