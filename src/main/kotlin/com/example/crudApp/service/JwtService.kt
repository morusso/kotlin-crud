package com.example.crudApp.service


import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Service
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import jakarta.annotation.PostConstruct

@Service
class JwtService {

    private lateinit var algorithm: Algorithm
    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey

    @PostConstruct
    fun init() {
        generateRSAKeys()
        algorithm = Algorithm.RSA256(publicKey, privateKey)
    }

    // generate rsa keys
    private fun generateRSAKeys() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair: KeyPair = keyPairGenerator.generateKeyPair()

        privateKey = keyPair.private as RSAPrivateKey
        publicKey = keyPair.public as RSAPublicKey

        println("RSA keys:")
        println("Public Key: ${Base64.getEncoder().encodeToString(publicKey.encoded)}")
        println("Private Key: ${Base64.getEncoder().encodeToString(privateKey.encoded)}")
    }

    // generate JWT token
    fun generateToken(
        userId: String,
        username: String,
        email: String,
        expirationMinutes: Long = 60
    ): String {
        return try {
            val expirationTime = Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)

            JWT.create()
                .withIssuer("kotlin-jwt-rsa-app")
                .withSubject(userId)
                .withClaim("username", username)
                .withClaim("email", email)
                .withIssuedAt(Date.from(Instant.now()))
                .withExpiresAt(Date.from(expirationTime))
                .withJWTId(UUID.randomUUID().toString())
                .sign(algorithm)
        } catch (exception: JWTCreationException) {
            throw RuntimeException("Błąd podczas tworzenia tokenu JWT", exception)
        }
    }

    // verify JWT token
    fun verifyToken(token: String): DecodedJWT {
        return try {
            val verifier = JWT.require(algorithm).build()
            verifier.verify(token)
        } catch (exception: JWTVerificationException) {
            throw RuntimeException("Nieprawidłowy token JWT: ${exception.message}", exception)
        }
    }

    // check token is valid
    fun isTokenValid(token: String): Boolean {
        return try {
            verifyToken(token)
            true
        } catch (exception: RuntimeException) {
            false
        }
    }

    // get user ID from JWT
    fun getUserIdFromToken(token: String): String? {
        return try {
            val decodedJWT = verifyToken(token)
            decodedJWT.subject
        } catch (exception: RuntimeException) {
            null
        }
    }

    // get username from JWT
    fun getUsernameFromToken(token: String): String? {
        return try {
            val decodedJWT = verifyToken(token)
            decodedJWT.getClaim("username").asString()
        } catch (exception: RuntimeException) {
            null
        }
    }

    // get email from JWT
    fun getEmailFromToken(token: String): String? {
        return try {
            val decodedJWT = verifyToken(token)
            decodedJWT.getClaim("email").asString()
        } catch (exception: RuntimeException) {
            null
        }
    }

    // check token expired
    fun isTokenExpired(token: String): Boolean {
        return try {
            val decodedJWT = verifyToken(token)
            decodedJWT.expiresAt.before(Date())
        } catch (exception: RuntimeException) {
            true
        }
    }

    // get all data from JWT
    fun getTokenInfo(token: String): TokenInfo? {
        return try {
            val decodedJWT = verifyToken(token)
            TokenInfo(
                userId = decodedJWT.subject,
                username = decodedJWT.getClaim("username").asString(),
                email = decodedJWT.getClaim("email").asString(),
                issuedAt = decodedJWT.issuedAt,
                expiresAt = decodedJWT.expiresAt,
                issuer = decodedJWT.issuer,
                jwtId = decodedJWT.id
            )
        } catch (exception: RuntimeException) {
            null
        }
    }

    // get public key
    fun getPublicKeyBase64(): String {
        return Base64.getEncoder().encodeToString(publicKey.encoded)
    }
}

// JWT data class
data class TokenInfo(
    val userId: String,
    val username: String?,
    val email: String?,
    val issuedAt: Date?,
    val expiresAt: Date?,
    val issuer: String?,
    val jwtId: String?
)