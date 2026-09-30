package com.example

import com.example.crypto.CryptoEngine
import com.example.data.model.DeviceType
import com.example.data.model.TransferPair
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAllCrossPlatformPairingsExist() {
        val pairs = TransferPair.getAllSupportedPairs()
        assertEquals(6, pairs.size)

        val pairIds = pairs.map { it.id }.toSet()
        assertTrue(pairIds.contains("ios_android"))
        assertTrue(pairIds.contains("mac_android"))
        assertTrue(pairIds.contains("mac_ios"))
        assertTrue(pairIds.contains("ios_windows"))
        assertTrue(pairIds.contains("windows_android"))
        assertTrue(pairIds.contains("mac_windows"))
    }

    @Test
    fun testAes256GcmEncryptionAndDecryptionRoundtrip() {
        val originalText = "Top-Secret-OmniDrop-Payload-2026"
        val password = "SuperSecretPassword123!"
        val fileName = "confidential_document.pdf"

        val encryptedContainer = CryptoEngine.encryptFile(
            plainBytes = originalText.toByteArray(Charsets.UTF_8),
            passphrase = password,
            originalFileName = fileName
        )

        assertNotNull(encryptedContainer)
        assertTrue(encryptedContainer.size > originalText.length)

        val result = CryptoEngine.decryptFile(encryptedContainer, password)
        assertEquals(fileName, result.originalFileName)
        assertEquals(originalText, String(result.decryptedBytes, Charsets.UTF_8))
    }

    @Test
    fun testDecryptionFailsWithWrongPassword() {
        val originalText = "Sensitive Data"
        val password = "CorrectPassword123"
        val wrongPassword = "WrongPassword999"

        val encryptedContainer = CryptoEngine.encryptFile(
            plainBytes = originalText.toByteArray(Charsets.UTF_8),
            passphrase = password,
            originalFileName = "test.txt"
        )

        try {
            CryptoEngine.decryptFile(encryptedContainer, wrongPassword)
            fail("Expected exception due to AEAD authentication tag failure")
        } catch (e: Exception) {
            // Expected: AEADBadTagException or GeneralSecurityException
            assertTrue(true)
        }
    }

    @Test
    fun testSha256IntegrityComputation() {
        val sample = "OmniDrop Security Engine"
        val hash1 = CryptoEngine.computeSha256(sample.toByteArray(Charsets.UTF_8))
        val hash2 = CryptoEngine.computeSha256(sample.toByteArray(Charsets.UTF_8))

        assertEquals(64, hash1.length)
        assertEquals(hash1, hash2)
    }

    @Test
    fun testSafetyNumberGeneration() {
        val safetyNumber = CryptoEngine.generateSafetyNumber("Android-Device", "iPhone-Device", "Session-Seed-42")
        assertNotNull(safetyNumber)
        assertTrue(safetyNumber.matches(Regex("\\d{4}-\\d{4}-\\d{4}")))
    }

    @Test
    fun testPairingPinLength() {
        val pin = CryptoEngine.generatePairingPin()
        assertEquals(4, pin.length)
        assertTrue(pin.toInt() in 1000..9999)
    }
}
