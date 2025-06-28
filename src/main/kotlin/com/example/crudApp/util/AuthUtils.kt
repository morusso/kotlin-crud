package com.example.crudApp.util

import java.util.Date

fun extractTokenFromHeader(authHeader: String): String? {
    return if (authHeader.startsWith("Bearer ")) {
        authHeader.substring(7)
    } else {
        null
    }
}

// JWT data class
data class TokenInfo(
    val userId: String?,
    val username: String?,
    val email: String?,
    val issuedAt: Date?,
    val expiresAt: Date?,
    val issuer: String?,
    val jwtId: String?
)