//package com.example.crudApp.service
//
//import com.example.crudApp.dto.AuthResponseDTO
//import com.example.crudApp.dto.LoginRequestDTO
//import com.example.crudApp.dto.ProductRequestDTO
//import com.example.crudApp.dto.UserRequestDTO
//import com.example.crudApp.dto.ProductResponseDTO
//import com.example.crudApp.mapper.ProductMapper
//import com.example.crudApp.entity.Product
//import com.example.crudApp.entity.User
//import com.example.crudApp.mapper.UserMapper
//import com.example.crudApp.repository.ProductRepository
//import com.example.crudApp.repository.UserRepository
//import org.assertj.core.api.Assertions.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.mockito.Mockito.mock
//import org.mockito.kotlin.*
//import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
//import java.util.*
//
//class ProductServiceTest {
//
//    private lateinit var productRepository: ProductRepository
//    private lateinit var productMapper: ProductMapper
//    private lateinit var productService: ProductService
//
//
//    @BeforeEach
//    fun setUp() {
//        productRepository = mock<ProductRepository>()
//        productMapper = mock<ProductMapper>()
//        productService = ProductService(productRepository, productMapper)
//    }
//
//    // Test data
//    private fun createProductRequestDTO() = ProductRequestDTO(
//        name = "product 1",
//        description = "product description",
//        price = 1.30,
//        quantity = 5,
//
//        )
//
//    private fun createProduct(id: Long = 1L) = Product(
//        id = id,
//        name = "product 1",
//        description = "product description",
//        price = 1.30,
//        quantity = 5,
//
//        )
//
//    private fun createProductResponseDTO(id: Long = 1L) = ProductResponseDTO(
//        id = id,
//        name = "product 1",
//        description = "product description",
//        price = 1.30,
//        quantity = 5,
//    )
//
//    // createProduct tests
//    @Test
//    fun `createProduct should create product successfully when valid data provided`() {
//        // Given
//        val productRequestDTO = createProductRequestDTO()
//        val product = createProduct()
//        val savedProduct = createProduct()
//        val productResponseDTO = createProductResponseDTO()
//
//        whenever(productRepository.existsByName(productRequestDTO.name)).thenReturn(false)
//        whenever(productMapper.toEntity(productRequestDTO)).thenReturn(product)
//        whenever(productRepository.save(any())).thenReturn(savedProduct)
//
//        // When
//        val result = productService.createProduct(productRequestDTO)
//
//        // Then
//        assertThat(result.name).isEqualTo(savedProduct.name)
//
//
//        verify(productMapper).toEntity(productRequestDTO)
//        verify(productRepository).save(any<Product>())
//    }
//}