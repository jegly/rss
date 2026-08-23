package com.jegly.rss

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.os.SystemClock
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.jegly.rss.presentation.navigation.NavGraph
import com.jegly.rss.util.WebViewPool
import com.jegly.rss.presentation.theme.SecureRSSTheme
import com.jegly.rss.security.BiometricAuthManager
import com.jegly.rss.security.EncryptionManager
import com.jegly.rss.security.IntegrityChecker
import com.jegly.rss.security.PassphraseGate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var biometricAuthManager: BiometricAuthManager
    @Inject lateinit var encryptionManager: EncryptionManager
    @Inject lateinit var passphraseGate: PassphraseGate

    // elapsedRealtime: monotonic, immune to NTP / user clock changes, counts during sleep.
    private var lastBackgroundElapsed: Long = 0
    private var isAuthenticated = false

    private var suspiciousAccessibilityPackages by mutableStateOf<List<String>>(emptyList())
    private var integrityFailed by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        updateScreenshotProtection()
        window.decorView.filterTouchesWhenObscured = true

        suspiciousAccessibilityPackages = scanAccessibilityServices()
        integrityFailed = !IntegrityChecker.verifySignature(this)
    }

    private fun updateScreenshotProtection() {
        val isProtected = encryptionManager.getBoolean("screenshot_protection", true)
        if (isProtected) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun scanAccessibilityServices(): List<String> {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .map { it.resolveInfo.serviceInfo.packageName }
            .filterNot { it.startsWith("com.google.") || it.startsWith("com.android.") }
    }

    override fun onPause() {
        super.onPause()
        lastBackgroundElapsed = SystemClock.elapsedRealtime()
    }

    override fun onResume() {
        super.onResume()
        updateScreenshotProtection()

        val useBiometrics = encryptionManager.getBoolean("use_biometrics", false)
        val now = SystemClock.elapsedRealtime()
        val shouldReauth = lastBackgroundElapsed != 0L && (now - lastBackgroundElapsed > REAUTH_GRACE_MS)

        if (useBiometrics && (shouldReauth || !isAuthenticated)) {
            isAuthenticated = false
            authenticateUser()
        } else {
            // Non-biometric path: supply plain passphrase to the gate (idempotent) and render.
            if (!passphraseGate.isOpen()) {
                passphraseGate.supply(passphraseGate.loadOrCreatePlainPassphrase())
            }
            isAuthenticated = true
            renderApp()
        }
    }

    private fun authenticateUser() {
        if (!biometricAuthManager.isBiometricAvailable()) {
            // Biometric requested but unavailable — fall back to plain (better than locking out).
            if (!passphraseGate.isOpen()) {
                passphraseGate.supply(passphraseGate.loadOrCreatePlainPassphrase())
            }
            isAuthenticated = true
            renderApp()
            return
        }

        // Crypto-bound flow: only if a biometric-wrapped passphrase has been enrolled.
        if (passphraseGate.hasBiometricWrappedPassphrase()) {
            val cipher = try {
                passphraseGate.makeDecryptionCipher()
            } catch (e: KeyPermanentlyInvalidatedException) {
                // The Keystore key was permanently invalidated (biometric enrolment changed).
                // Roll back to plain mode so the user isn't locked out, but require an explicit
                // tap acknowledging the reset rather than silently granting access.
                passphraseGate.clearBiometricWrap()
                encryptionManager.saveBoolean("use_biometrics", false)
                showBiometricResetPrompt()
                return
            }
            if (cipher == null) {
                // Unexpected: a wrapped passphrase blob exists but its IV is missing/corrupt.
                // Fail closed rather than silently granting access with no auth factor.
                showAuthFailedPrompt("Could not verify biometric protection. Please restart the app.")
                return
            }
            biometricAuthManager.showCryptoPrompt(
                activity = this,
                cipher = cipher,
                onSuccess = { unlockedCipher ->
                    val passphrase = passphraseGate.unwrapPlainPassphraseWithCipher(unlockedCipher)
                    if (passphrase == null) { finish(); return@showCryptoPrompt }
                    if (!passphraseGate.isOpen()) passphraseGate.supply(passphrase)
                    isAuthenticated = true
                    renderApp()
                },
                onError = { finish() }
            )
        } else {
            // Biometric is enabled in settings but no wrapped passphrase exists yet — UI-only prompt
            // to enforce the user-presence gate, then load the plain passphrase.
            biometricAuthManager.showBiometricPrompt(this,
                onSuccess = {
                    if (!passphraseGate.isOpen()) {
                        passphraseGate.supply(passphraseGate.loadOrCreatePlainPassphrase())
                    }
                    isAuthenticated = true
                    renderApp()
                },
                onError = { finish() }
            )
        }
    }

    private fun showBiometricResetPrompt() {
        setContent {
            SecureRSSTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    BiometricResetDialog(
                        onContinue = {
                            if (!passphraseGate.isOpen()) {
                                passphraseGate.supply(passphraseGate.loadOrCreatePlainPassphrase())
                            }
                            isAuthenticated = true
                            renderApp()
                        },
                        onExit = { finish() }
                    )
                }
            }
        }
    }

    private fun showAuthFailedPrompt(message: String) {
        setContent {
            SecureRSSTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AuthFailedDialog(message = message, onDismiss = { finish() })
                }
            }
        }
    }

    private fun renderApp() {
        // Prime the WebView renderer while the user is on the home screen so the first article
        // tap has no Chromium cold-start delay. Posted so it doesn't block setContent.
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            WebViewPool.prime(this)
        }
        setContent {
            SecureRSSTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Block the app until the user acknowledges any tamper / accessibility warnings.
                    when {
                        integrityFailed -> IntegrityFailedDialog(onDismiss = { finish() })
                        suspiciousAccessibilityPackages.isNotEmpty() -> AccessibilityWarningDialog(
                            packages = suspiciousAccessibilityPackages,
                            onContinue = { suspiciousAccessibilityPackages = emptyList() },
                            onExit = { finish() }
                        )
                        else -> NavGraph()
                    }
                }
            }
        }
    }

    companion object {
        private const val REAUTH_GRACE_MS = 30_000L
    }
}

@androidx.compose.runtime.Composable
private fun IntegrityFailedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tampered installation") },
        text = { Text("This APK was not signed by the expected developer key. Refusing to run.") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Exit") } }
    )
}

@androidx.compose.runtime.Composable
private fun BiometricResetDialog(onContinue: () -> Unit, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onExit,
        title = { Text("Biometric protection reset") },
        text = {
            Text(
                "Your device's biometric enrolment changed, so this app can no longer use it to " +
                    "unlock your feeds. Tap Continue to unlock with your fallback passphrase, then " +
                    "re-enable biometrics from Settings if you'd like to use it again."
            )
        },
        confirmButton = { TextButton(onClick = onContinue) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onExit) { Text("Exit") } }
    )
}

@androidx.compose.runtime.Composable
private fun AuthFailedDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Authentication error") },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Exit") } }
    )
}

@androidx.compose.runtime.Composable
private fun AccessibilityWarningDialog(
    packages: List<String>,
    onContinue: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onExit,
        title = { Text("Security Warning") },
        text = {
            Text(
                "The following accessibility services are active and can read or interact with " +
                    "screen content:\n\n" + packages.joinToString("\n") { "• $it" } +
                    "\n\nDisable them in Settings → Accessibility before continuing if you don't recognise them."
            )
        },
        confirmButton = { TextButton(onClick = onContinue) { Text("Continue anyway") } },
        dismissButton = { TextButton(onClick = onExit) { Text("Exit") } }
    )
}
