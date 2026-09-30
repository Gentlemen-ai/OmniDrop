package com.example.crypto

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoEngine {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12 // 96 bits recommended for GCM
    private const val GCM_TAG_LENGTH = 128 // 128 bits authentication tag
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private val MAGIC_BYTES = "OMNI".toByteArray(Charsets.UTF_8)

    private val secureRandom = SecureRandom()

    /**
     * Derives a 256-bit AES secret key from a user passphrase and salt using PBKDF2.
     */
    fun deriveKey(passphrase: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Generates a random 16-byte cryptographic salt.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        return salt
    }

    /**
     * Generates a random 12-byte IV for AES-GCM.
     */
    fun generateIv(): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)
        return iv
    }

    /**
     * Encrypts raw plaintext bytes into an authenticated OMNI container.
     */
    fun encryptFile(
        plainBytes: ByteArray,
        passphrase: String,
        originalFileName: String
    ): ByteArray {
        val salt = generateSalt()
        val iv = generateIv()
        val secretKey = deriveKey(passphrase, salt)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherText = cipher.doFinal(plainBytes)

        val fileNameBytes = originalFileName.toByteArray(Charsets.UTF_8)
        val outputStream = ByteArrayOutputStream()

        // Write Magic Header
        outputStream.write(MAGIC_BYTES)
        // Write Salt
        outputStream.write(salt)
        // Write IV
        outputStream.write(iv)
        // Write Filename Length (2 bytes)
        outputStream.write((fileNameBytes.size shr 8) and 0xFF)
        outputStream.write(fileNameBytes.size and 0xFF)
        // Write Filename
        outputStream.write(fileNameBytes)
        // Write Ciphertext
        outputStream.write(cipherText)

        return outputStream.toByteArray()
    }

    data class DecryptedResult(
        val originalFileName: String,
        val decryptedBytes: ByteArray
    )

    /**
     * Decrypts an authenticated OMNI container back to original file bytes.
     */
    fun decryptFile(
        containerBytes: ByteArray,
        passphrase: String
    ): DecryptedResult {
        if (containerBytes.size < 4 + 16 + 12 + 2) {
            throw IllegalArgumentException("Invalid encrypted package format: payload too small")
        }

        val inputStream = ByteArrayInputStream(containerBytes)

        // Verify Magic
        val magic = ByteArray(4)
        inputStream.read(magic)
        if (!magic.contentEquals(MAGIC_BYTES)) {
            throw IllegalArgumentException("Invalid file format: not an OmniDrop encrypted file")
        }

        // Read Salt
        val salt = ByteArray(16)
        inputStream.read(salt)

        // Read IV
        val iv = ByteArray(GCM_IV_LENGTH)
        inputStream.read(iv)

        // Read Filename Length
        val nameLenHigh = inputStream.read()
        val nameLenLow = inputStream.read()
        val fileNameLength = (nameLenHigh shl 8) or nameLenLow

        val fileNameBytes = ByteArray(fileNameLength)
        inputStream.read(fileNameBytes)
        val originalFileName = String(fileNameBytes, Charsets.UTF_8)

        // Read Ciphertext
        val cipherText = inputStream.readBytes()

        val secretKey = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val plainBytes = cipher.doFinal(cipherText)
        return DecryptedResult(originalFileName, plainBytes)
    }

    /**
     * Computes the SHA-256 hex string of input bytes to verify integrity.
     */
    fun computeSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a 6-digit or 12-digit Safety Verification Code (like Signal / WhatsApp safety numbers)
     * from two peer identifiers and a shared session seed.
     */
    fun generateSafetyNumber(peerA: String, peerB: String, sessionSeed: String): String {
        val combined = "$peerA:$peerB:$sessionSeed"
        val hash = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray(Charsets.UTF_8))
        val num1 = ((hash[0].toInt() and 0xFF) shl 8) or (hash[1].toInt() and 0xFF)
        val num2 = ((hash[2].toInt() and 0xFF) shl 8) or (hash[3].toInt() and 0xFF)
        val num3 = ((hash[4].toInt() and 0xFF) shl 8) or (hash[5].toInt() and 0xFF)
        return "%04d-%04d-%04d".format(num1 % 10000, num2 % 10000, num3 % 10000)
    }

    /**
     * Generates a random 4-digit PIN for wireless web pairing
     */
    fun generatePairingPin(): String {
        val pin = secureRandom.nextInt(9000) + 1000
        return pin.toString()
    }
}
