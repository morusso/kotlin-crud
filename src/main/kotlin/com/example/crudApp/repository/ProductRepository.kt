package com.example.crudApp.repository

import com.example.crudApp.entity.Product
import com.example.crudApp.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByName(name: String): Optional<Product>
    fun existsByName(name: String): Boolean

}