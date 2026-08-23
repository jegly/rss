package com.jegly.rss.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.crypto.Cipher
import javax.inject.Inject

class BiometricAuthManager @Inject constructor(@ApplicationContext private val context: Context) {

    fun isBiometricAvailable(): Boolean {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        return BiometricManager.from(context).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /** Plain UI prompt — no crypto binding. Use only when biometric crypto-mode is off. */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = launch(
        activity = activity,
        cryptoObject = null,
        onSuccess = { onSuccess() },
        onError = onError
    )

    /**
     * Crypto-bound prompt. The supplied [cipher] must be initialised in either ENCRYPT_MODE
     * (during enrolment) or DECRYPT_MODE (during unlock). On success, the same cipher object
     * is returned via [onSuccess] and can perform exactly one doFinal() while the auth window
     * is open.
     */
    fun showCryptoPrompt(
        activity: FragmentActivity,
        cipher: Cipher,
        title: String = "Unlock RSS",
        subtitle: String = "Authenticate to decrypt your feeds",
        onSuccess: (Cipher) -> Unit,
        onError: (String) -> Unit
    ) = launch(
        activity = activity,
        cryptoObject = BiometricPrompt.CryptoObject(cipher),
        onSuccess = { result -> result.cryptoObject?.cipher?.let(onSuccess) ?: onError("Cipher missing") },
        onError = onError,
        title = title,
        subtitle = subtitle
    )

    private fun launch(
        activity: FragmentActivity,
        cryptoObject: BiometricPrompt.CryptoObject?,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit,
        title: String = "Unlock RSS",
        subtitle: String = "Authenticate using strong biometrics"
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .build()
        if (cryptoObject != null) prompt.authenticate(info, cryptoObject) else prompt.authenticate(info)
    }
}
