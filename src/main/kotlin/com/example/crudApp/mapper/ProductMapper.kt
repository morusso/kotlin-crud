package com.example.crudApp.mapper
import com.example.crudApp.dto.ProductRequestDTO
import com.example.crudApp.dto.ProductResponseDTO
import com.example.crudApp.entity.Product
import org.springframework.stereotype.Component

@Component
class ProductMapper {

    fun toEntity(dto: ProductRequestDTO): Product {
        return Product(
            name = dto.name,
            price = dto.price,
            description = dto.description,
            quantity = dto.quantity
        )
    }

    fun toDto(entity: Product): ProductResponseDTO {
        return ProductResponseDTO(
            id = entity.id,
            name = entity.name,
            price = entity.price,
            description = entity.description,
            quantity = entity.quantity
        )
    }
}