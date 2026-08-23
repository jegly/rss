package com.jegly.rss.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.CompletableDeferred
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the SQLCipher passphrase.
 *
 * Two modes:
 *  - Plain mode: passphrase lives in EncryptedSharedPreferences. Available immediately on app start.
 *  - Biometric mode: passphrase is encrypted under an Android-Keystore key marked
 *    `setUserAuthenticationRequired(true)`. Decryption requires a Cipher unlocked by
 *    BiometricPrompt's CryptoObject — there is no software path to the passphrase.
 *
 * AppModule's database provider blocks on [await] until the appropriate flow has supplied
 * the passphrase via [supply].
 */
@Singleton
class PassphraseGate @Inject constructor(
    private val encryptionManager: EncryptionManager
) {
    private val deferred = CompletableDeferred<ByteArray>()

    /** Block until the passphrase is available. Called by AppModule's provideDatabase. */
    suspend fun await(): ByteArray = deferred.await()

    /** Mark the gate as open with the given passphrase. Idempotent — second call is ignored. */
    fun supply(passphrase: ByteArray) {
        deferred.complete(passphrase)
    }

    /** True if the gate has already been opened. */
    fun isOpen(): Boolean = deferred.isCompleted

    // ─── Plain mode ──────────────────────────────────────────────────────────────────────────

    /** Reads (or creates on first use) the passphrase stored in EncryptedSharedPreferences. */
    fun loadOrCreatePlainPassphrase(): ByteArray {
        val hex = encryptionManager.getString(PREF_DB_KEY_HEX) ?: run {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            val key = bytes.joinToString("") { "%02x".format(it) }
            encryptionManager.saveString(PREF_DB_KEY_HEX, key)
            key
        }
        return hex.toByteArray(StandardCharsets.UTF_8)
    }

    // ─── Biometric mode ──────────────────────────────────────────────────────────────────────

    /** True if a biometric-wrapped passphrase blob exists. */
    fun hasBiometricWrappedPassphrase(): Boolean =
        encryptionManager.getString(PREF_DB_KEY_WRAPPED_CIPHERTEXT) != null

    /**
     * Builds an init'd encryption Cipher for the biometric-protected Keystore key.
     * Pass to BiometricPrompt.authenticate(CryptoObject(cipher)). On success the same
     * cipher object is usable for [wrapPlainPassphraseUnderBiometric].
     */
    fun makeEncryptionCipher(): Cipher {
        val key = getOrCreateBiometricKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher
    }

    /**
     * Builds a decryption Cipher pre-loaded with the stored IV. Pass to BiometricPrompt;
     * on success use [unwrapPlainPassphraseWithCipher] to retrieve the passphrase.
     */
    fun makeDecryptionCipher(): Cipher? {
        val ivB64 = encryptionManager.getString(PREF_DB_KEY_WRAPPED_IV) ?: return null
        val iv = Base64.decode(ivB64, Base64.NO_WRAP)
        val key = getOrCreateBiometricKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher
    }

    /** Encrypts a plaintext passphrase with a biometric-unlocked Cipher and persists the result. */
    fun wrapPlainPassphraseUnderBiometric(plaintext: ByteArray, biometricUnlockedCipher: Cipher) {
        val ciphertext = biometricUnlockedCipher.doFinal(plaintext)
        encryptionManager.saveString(
            PREF_DB_KEY_WRAPPED_CIPHERTEXT,
            Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        )
        encryptionManager.saveString(
            PREF_DB_KEY_WRAPPED_IV,
            Base64.encodeToString(biometricUnlockedCipher.iv, Base64.NO_WRAP)
        )
    }

    /** Decrypts the stored wrapped passphrase using a biometric-unlocked Cipher. */
    fun unwrapPlainPassphraseWithCipher(biometricUnlockedCipher: Cipher): ByteArray? {
        val ctB64 = encryptionManager.getString(PREF_DB_KEY_WRAPPED_CIPHERTEXT) ?: return null
        val ciphertext = Base64.decode(ctB64, Base64.NO_WRAP)
        return biometricUnlockedCipher.doFinal(ciphertext)
    }

    /** Forgets the biometric-wrapped passphrase. Plain-mode passphrase remains intact. */
    fun clearBiometricWrap() {
        encryptionManager.securePrefs.edit()
            .remove(PREF_DB_KEY_WRAPPED_CIPHERTEXT)
            .remove(PREF_DB_KEY_WRAPPED_IV)
            .apply()
        runCatching {
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                .deleteEntry(BIOMETRIC_KEY_ALIAS)
        }
    }

    private fun getOrCreateBiometricKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(BIOMETRIC_KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val spec = KeyGenParameterSpec.Builder(
            BIOMETRIC_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            // Invalidate the key if biometrics are added/removed — forces re-enrolment.
            .setInvalidatedByBiometricEnrollment(true)
            .build()
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        kg.init(spec)
        return kg.generateKey()
    }

    companion object {
        private const val PREF_DB_KEY_HEX = "db_key"                            // plain-mode passphrase
        private const val PREF_DB_KEY_WRAPPED_CIPHERTEXT = "db_key_wrapped"     // biometric-wrapped ciphertext
        private const val PREF_DB_KEY_WRAPPED_IV = "db_key_wrapped_iv"          // GCM IV (12 bytes)
        private const val BIOMETRIC_KEY_ALIAS = "rss_db_passphrase_biometric"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }
}
