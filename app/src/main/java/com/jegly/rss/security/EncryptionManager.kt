package com.jegly.rss.security

import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val masterKey: MasterKey by lazy {
        val builder = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        
        // Strongbox / TEE Logic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val hasStrongBox = context.packageManager.hasSystemFeature("android.hardware.strongbox_keystore")
            if (hasStrongBox) {
                builder.setRequestStrongBoxBacked(true)
            }
        }
        builder.build()
    }

    val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_rss_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private var tinkAead: Aead? = null

    init {
        AeadConfig.register()
        try {
            tinkAead = AndroidKeysetManager.Builder()
                .withSharedPref(context, "tink_keyset", "tink_prefs")
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri("android-keystore://tink_master_key")
                .build()
                .keysetHandle
                .getPrimitive(Aead::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun encrypt(plaintext: ByteArray, associatedData: ByteArray = ByteArray(0)): String? {
        return try {
            val ciphertext = tinkAead?.encrypt(plaintext, associatedData)
            Base64.encodeToString(ciphertext, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decrypt(ciphertextBase64: String, associatedData: ByteArray = ByteArray(0)): ByteArray? {
        return try {
            val ciphertext = Base64.decode(ciphertextBase64, Base64.DEFAULT)
            tinkAead?.decrypt(ciphertext, associatedData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getKeystoreSecurityLevel(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val hasStrongBox = context.packageManager.hasSystemFeature("android.hardware.strongbox_keystore")
            if (hasStrongBox) "StrongBox (Hardware Isolated)" else "TEE (Trusted Execution Environment)"
        } else {
            "TEE / Software (Legacy)"
        }
    }

    fun saveBoolean(key: String, value: Boolean) = securePrefs.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String, default: Boolean = false): Boolean = securePrefs.getBoolean(key, default)
    
    fun saveString(key: String, value: String) = securePrefs.edit().putString(key, value).apply()
    fun getString(key: String): String? = securePrefs.getString(key, null)

    fun saveFloat(key: String, value: Float) = securePrefs.edit().putFloat(key, value).apply()
    fun getFloat(key: String, default: Float = 0f): Float = securePrefs.getFloat(key, default)
}
