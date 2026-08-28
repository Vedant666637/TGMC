package com.tgm.tgmc.core.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Handles hardware-backed AES-GCM encryption and decryption using Android Keystore.
 * Used to encrypt sensitive data (JWT tokens) before persisting to DataStore.
 *
 * IMPORTANT: Uses ':' as IV/ciphertext separator to avoid collision with JWT dot notation.
 */
object CryptoManager {

    private const val TAG = "CryptoManager"
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "TGMC_SecureKey"
    private const val GCM_TAG_LENGTH = 128

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts a plaintext string.
     * Returns: [IV_base64]:[Ciphertext_base64]
     * Uses ':' as separator — safe for JWTs which use '.'.
     */
    fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherBase64 = Base64.encodeToString(cipherText, Base64.NO_WRAP)
            "$ivBase64:$cipherBase64"
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed: ${e.message}", e)
            ""
        }
    }

    /**
     * Decrypts a combined Base64 string.
     * Returns null on any failure — caller should treat null as "no token available".
     */
    fun decrypt(encryptedData: String): String? {
        if (encryptedData.isEmpty()) return null
        return try {
            // New format uses ':' as separator; old buggy format used '.'
            // Old format stored with '.' will fail because JWT itself contains dots
            val parts = if (encryptedData.contains(':')) {
                encryptedData.split(':', limit = 2)
            } else {
                // Old '.' format — will return null if more than 2 parts (raw JWT stored by mistake)
                encryptedData.split('.', limit = 2)
            }
            if (parts.size != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
                Log.w(TAG, "Corrupted or legacy token format — forcing re-login")
                return null
            }
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed (stale token after reinstall?): ${e.message}")
            null
        }
    }
}
