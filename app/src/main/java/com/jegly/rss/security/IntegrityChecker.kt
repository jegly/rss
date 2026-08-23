package com.jegly.rss.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.security.MessageDigest

/**
 * Compares the running APK's signing certificate against a hardcoded list of acceptable SHA-256
 * digests. Returns true if the APK signature matches; false if the app has been re-signed
 * (repackaging, modding).
 *
 * Catch: trivially defeated by an attacker who can also patch this file, so it's defense-in-depth,
 * not a security boundary. Useful for detecting "casual" tampering by users running modded APKs.
 */
object IntegrityChecker {

    /**
     * SHA-256 digests of acceptable signing certs (hex, lowercase, no separators).
     * Populate with `keytool -list -v -alias <alias> -keystore <ks> | grep SHA256` on your
     * release keystore, stripped of colons. Empty list = check is a no-op (fails open) — keep
     * empty during initial dev, fill in before first release.
     */
    private val EXPECTED_SHA256: Set<String> = setOf(
        "98d324d4106a368c62729a0a24d9ac9a6b47f8ac4c6585348531f0ee4eb6a04c"
    )

    /** Returns true if signing matches expectations, the APK is debuggable, or no expectations have been declared. */
    fun verifySignature(context: Context): Boolean {
        // Debug builds are signed with ~/.android/debug.keystore (per-machine, per-developer).
        // The integrity check is only meaningful for released APKs distributed publicly.
        if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) return true
        if (EXPECTED_SHA256.isEmpty()) return true
        val actual = runCatching { currentSigningDigests(context) }.getOrElse { return false }
        return actual.any { it in EXPECTED_SHA256 }
    }

    private fun currentSigningDigests(context: Context): List<String> {
        val pm = context.packageManager
        val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        val signers = info.signingInfo ?: return emptyList()
        val sigs = if (signers.hasMultipleSigners()) {
            signers.apkContentsSigners
        } else {
            signers.signingCertificateHistory
        }
        val md = MessageDigest.getInstance("SHA-256")
        return sigs.map { sig ->
            md.reset()
            md.digest(sig.toByteArray()).joinToString("") { "%02x".format(it) }
        }
    }
}
