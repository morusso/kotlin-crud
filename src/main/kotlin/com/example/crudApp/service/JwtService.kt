package com.example.crudApp.service

import org.springframework.stereotype.Service
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
import java.io.File
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

@Service
class JwtService {

    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey

    /* --------------------------- RSA keys --------------------------- */

    @PostConstruct
    fun init() = getOrGenerateRSAKeys()

    private fun getOrGenerateRSAKeys() {
        val keysDir = File("src/main/resources/keys")
        if (!keysDir.exists()) keysDir.mkdirs()

        val privateKeyFile = File(keysDir, "private_key.pem")
        val publicKeyFile = File(keysDir, "public_key.pem")

        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            privateKey = readPrivateKeyFromPem(privateKeyFile)
            publicKey = readPublicKeyFromPem(publicKeyFile)
        } else {
            val keyPair = KeyPairGenerator.getInstance("RSA").apply {
                initialize(2048)
            }.generateKeyPair()

            privateKey = keyPair.private as RSAPrivateKey
            publicKey = keyPair.public as RSAPublicKey

            writeKeyToPemFile(privateKeyFile, "PRIVATE KEY", privateKey.encoded)
            writeKeyToPemFile(publicKeyFile, "PUBLIC KEY", publicKey.encoded)
        }
    }

    private fun writeKeyToPemFile(file: File, type: String, encoded: ByteArray) {
        val base64 = Base64.getMimeEncoder(64, "\n".toByteArray()).encodeToString(encoded)
        file.writeText("-----BEGIN $type-----\n$base64\n-----END $type-----\n")
    }

    private fun readPrivateKeyFromPem(file: File): RSAPrivateKey {
        val content = file.readText()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val decoded = Base64.getDecoder().decode(content)
        val keySpec = PKCS8EncodedKeySpec(decoded)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec) as RSAPrivateKey
    }

    private fun readPublicKeyFromPem(file: File): RSAPublicKey {
        val content = file.readText()
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val decoded = Base64.getDecoder().decode(content)
        val keySpec = X509EncodedKeySpec(decoded)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec) as RSAPublicKey
    }

    /* -------------------------- generate JWE ----------------------- */

    fun generateToken(
        userId: String,
        username: String,
        email: String,
        expirationMinutes: Long = 60
    ): String {
        val now = Instant.now()
        val expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES)

        val claims = JWTClaimsSet.Builder()
            .subject(userId)
            .issuer("kotlin-jwe-app")
            .issueTime(Date.from(now))
            .expirationTime(Date.from(expiresAt))
            .claim("username", username)
            .claim("email", email)
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

    /** return object `EncryptedJWT` or `RuntimeException`. */
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