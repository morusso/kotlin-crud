package com.example.crudApp.entity

import jakarta.persistence.*

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    var name: String,

    var description: String?,
    var price: Double,
    var quantity: Int
)