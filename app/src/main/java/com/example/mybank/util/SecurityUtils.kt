package com.example.mybank.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
    
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    
    /**
     * Hash password using PBKDF2
     * Returns: salt:hash format
     */
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = pbkdf2(password, salt)
        return "${bytesToHex(salt)}:${bytesToHex(hash)}"
    }
    
    /**
     * Verify password against stored hash
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false
            
            val salt = hexToBytes(parts[0])
            val hash = hexToBytes(parts[1])
            val testHash = pbkdf2(password, salt)
            
            hash.contentEquals(testHash)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Simple SHA-256 hash (for quick checks)
     */
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytesToHex(bytes)
    }
    
    // Private helper methods
    
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }
    
    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
    
    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
