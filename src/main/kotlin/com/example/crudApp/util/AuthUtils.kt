package com.example.crudApp.util

fun extractTokenFromHeader(authHeader: String): String? {
    return if (authHeader.startsWith("Bearer ")) {
        authHeader.substring(7)
    } else {
        null
    }
}