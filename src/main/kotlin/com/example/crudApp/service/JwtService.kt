package com.example.crudApp.service
import org.springframework.stereotype.Service
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.crypto.RSADecrypter
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import jakarta.annotation.PostConstruct
import com.example.crudApp.util.TokenInfo

@Service
class JwtService {

    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey

    /* --------------------------- RSA keys --------------------------- */

    @PostConstruct
    fun init() = generateRSAKeys()

    private fun generateRSAKeys() {
        val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA")
            .apply { initialize(2048) }
            .generateKeyPair()

        privateKey = keyPair.private as RSAPrivateKey
        publicKey  = keyPair.public  as RSAPublicKey
    }

    /* -------------------------- generate JWE ----------------------- */

    fun generateToken(
        userId: String,
        username: String,
        email: String,
        expirationMinutes: Long = 60
    ): String {
        val now        = Instant.now()
        val expiresAt  = now.plus(expirationMinutes, ChronoUnit.MINUTES)

        val claims = JWTClaimsSet.Builder()
            .subject(userId)
            .issuer("kotlin-jwe-app")
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expiresAt))
            .claim("username", username)
            .claim("email",    email)
            .jwtID(UUID.randomUUID().toString())
            .build()

        val header = JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
            .contentType("JWT")
            .build()

        return EncryptedJWT(header, claims).apply {
            encrypt(RSAEncrypter(publicKey))
        }.serialize()
    }

    /* ------------------------- decrypt ------------------------ */

    /** Zwraca odszyfrowany obiekt `EncryptedJWT` lub rzuca `RuntimeException`. */
    private fun decryptToken(token: String): EncryptedJWT =
        EncryptedJWT.parse(token).apply {
            decrypt(RSADecrypter(privateKey))
        }

    /* --------------------------- utils ----------------------------- */

    fun getTokenInfo(token: String): TokenInfo? {
        return try {
            val decryptedJWT = decryptToken(token)
            val claims = decryptedJWT.jwtClaimsSet
            TokenInfo(
                userId = claims.subject,
                username = claims.getStringClaim("username"),
                email = claims.getStringClaim("email"),
                issuedAt = claims.issueTime,
                expiresAt = claims.expirationTime,
                issuer = claims.issuer,
                jwtId = claims.getStringClaim("jti")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun isTokenValid(token: String): Boolean = runCatching {
        val decryptedJWT = decryptToken(token)
        val exp = decryptedJWT.jwtClaimsSet.expirationTime
        exp.after(Date())
    }.getOrElse { false }

    fun isTokenExpired(token: String): Boolean = !isTokenValid(token)

    fun getUserIdFromToken(token: String): String? = runCatching {
        decryptToken(token).jwtClaimsSet.subject
    }.getOrNull()

    fun getUsernameFromToken(token: String): String? = runCatching {
        decryptToken(token).jwtClaimsSet.getStringClaim("username")
    }.getOrNull()

    fun getEmailFromToken(token: String): String? = runCatching {
        decryptToken(token).jwtClaimsSet.getStringClaim("email")
    }.getOrNull()

    fun getJwtIdFromToken(token: String): String? = runCatching {
        decryptToken(token).jwtClaimsSet.getStringClaim("jti")
    }.getOrNull()

    fun getPublicKeyBase64(): String =
        Base64.getEncoder().encodeToString(publicKey.encoded)
}