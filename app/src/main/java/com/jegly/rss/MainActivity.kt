package com.jegly.rss

import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.jegly.rss.presentation.navigation.NavGraph
import com.jegly.rss.presentation.theme.SecureRSSTheme
import com.jegly.rss.security.BiometricAuthManager
import com.jegly.rss.security.EncryptionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var biometricAuthManager: BiometricAuthManager
    @Inject lateinit var encryptionManager: EncryptionManager
    
    private var lastBackgroundTime: Long = 0
    private var isAuthenticated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initial check for Screenshot Protection
        updateScreenshotProtection()
        
        // 2. Tapjacking Protection (Filter Touches when Obscured)
        window.decorView.filterTouchesWhenObscured = true

        // 3. Accessibility Permission Abuse Detection
        checkAccessibilityServices()
    }

    private fun updateScreenshotProtection() {
        val isProtected = encryptionManager.getBoolean("screenshot_protection", true)
        if (isProtected) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun checkAccessibilityServices() {
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (service in enabledServices) {
            val packageName = service.resolveInfo.serviceInfo.packageName
            if (!packageName.startsWith("com.google.") && !packageName.startsWith("com.android.")) {
                Toast.makeText(this, "Security Warning: Suspicious Accessibility Service detected ($packageName)", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        lastBackgroundTime = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        // Re-apply protection in case it changed in settings
        updateScreenshotProtection()
        
        val useBiometrics = encryptionManager.getBoolean("use_biometrics", false)
        val currentTime = System.currentTimeMillis()
        
        val shouldReauth = (currentTime - lastBackgroundTime > 30000) && lastBackgroundTime != 0L
        
        if (useBiometrics && (shouldReauth || !isAuthenticated)) {
            isAuthenticated = false
            authenticateUser()
        } else {
            isAuthenticated = true
            renderApp()
        }
    }

    private fun authenticateUser() {
        if (biometricAuthManager.isBiometricAvailable()) {
            biometricAuthManager.showBiometricPrompt(this,
                onSuccess = {
                    isAuthenticated = true
                    renderApp()
                },
                onError = { finish() }
            )
        } else {
            isAuthenticated = true
            renderApp()
        }
    }

    private fun renderApp() {
        setContent {
            SecureRSSTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }
}
