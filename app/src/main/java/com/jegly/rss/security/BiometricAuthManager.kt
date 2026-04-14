package com.jegly.rss.security

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BiometricAuthManager @Inject constructor(@ApplicationContext private val context: Context) {
    
    fun isBiometricAvailable(): Boolean {
        // Require BIOMETRIC_STRONG (Hardware backed)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        return BiometricManager.from(context).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showBiometricPrompt(
        activity: FragmentActivity, 
        onSuccess: () -> Unit, 
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock RSS")
            .setSubtitle("Authenticate using strong biometrics")
            // Explicitly require strong biometrics (often tied to TEE/StrongBox)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(promptInfo)
    }
}
