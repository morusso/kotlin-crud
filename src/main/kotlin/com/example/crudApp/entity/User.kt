package com.example.crudApp.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var username: String,
    var email: String,
    var password: String,
    var isActive: Boolean,
    var firstName: String,
    var lastName: String
)