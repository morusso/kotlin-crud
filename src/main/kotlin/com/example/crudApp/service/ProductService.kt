package com.example.crudApp.service

import com.example.crudApp.dto.ProductRequestDTO
import com.example.crudApp.dto.ProductResponseDTO
import com.example.crudApp.mapper.ProductMapper
import com.example.crudApp.repository.ProductRepository
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper
) {

    fun createProduct(productRequestDTO: ProductRequestDTO): ProductResponseDTO {
        val product = productMapper.toEntity(productRequestDTO)
        val savedProduct = productRepository.save(product)

        return productMapper.toDto(savedProduct)
    }

    fun getProductById(id: Long): ProductResponseDTO {
        val product = productRepository.findById(id)
            .orElseThrow { NotFoundException() }

        return productMapper.toDto(product)
    }

    fun getAllProducts(): List<ProductResponseDTO> {
        return productRepository.findAll()
            .stream()
            .map { product -> productMapper.toDto(product) }
            .collect(Collectors.toList())
    }

    fun updateProduct(id: Long, productRequestDTO: ProductRequestDTO): ProductResponseDTO {
        val existingProduct = productRepository.findById(id)
            .orElseThrow { NotFoundException() }

        val updatedProduct = existingProduct.copy(
            name = productRequestDTO.name,
            description = productRequestDTO.description,
            price = productRequestDTO.price,
            quantity = productRequestDTO.quantity
        )
        val savedProduct = productRepository.save(updatedProduct)
        return productMapper.toDto(savedProduct)
    }

    fun deleteProduct(id: Long) {
        productRepository.deleteById(id)
    }
}