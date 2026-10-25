package com.jegly.rss.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * Compares the running APK's current signing certificate against the expected SHA-256 digest.
 * Returns true if the APK signature matches; false if the app has been re-signed
 * (repackaging, modding).
 *
 * Catch: trivially defeated by an attacker who can also patch this file, so it's defense-in-depth,
 * not a security boundary. Useful for detecting "casual" tampering by users running modded APKs.
 */
object IntegrityChecker {

    // Expected signing-cert SHA-256 digests, stored XOR-masked (never as greppable hex — string
    // literals survive R8) and compared as raw bytes. M = jegly key0 (98d324d4…),
    // P = ML-DSA-87 PQC key (5f99de5b…).
    // API 37+ verifies the APK Signature Scheme v3.2 hybrid block, so the current signer is the PQC
    // key there. key0 is deliberately NOT accepted on API 37+: an APK re-signed with only key0 would
    // otherwise pass. Older releases ignore v3.2 and still see key0.
    private val K = intArrayOf(0xEC, 0x5D, 0x5A, 0xEB, 0x9D, 0x7B, 0x49, 0x95, 0xCF)
    private val M = intArrayOf(
        0x74, 0x8E, 0x7E, 0x3F, 0x8D, 0x11, 0x7F, 0x19, 0xAD, 0x9E, 0xC7, 0x50, 0xCF, 0x44, 0xD7, 0xD3,
        0xFE, 0x88, 0x14, 0xF1, 0x16, 0x8E, 0x18, 0x4F, 0xCC, 0xA4, 0x3F, 0x02, 0x13, 0xEC, 0x4B, 0xD1,
    )
    private val P = intArrayOf(
        0xB3, 0xC4, 0x84, 0xB0, 0xAC, 0xEE, 0x3C, 0xA8, 0x80, 0x04, 0x80, 0xF1, 0x00, 0x4F, 0x80, 0x6E,
        0xA0, 0xA4, 0xCD, 0x54, 0x8C, 0x10, 0x2F, 0x3C, 0x56, 0x5E, 0x72, 0x1E, 0x47, 0xC4, 0x9D, 0x3B,
    )

    /** Reconstruct the expected 32-byte cert digest from the masked form (never materialized as hex). */
    private fun expected(): ByteArray {
        val masked = if (Build.VERSION.SDK_INT >= 37) P else M
        return ByteArray(masked.size) { (masked[it] xor K[it % K.size]).toByte() }
    }

    /** Returns true if the current signer matches, or the APK is debuggable. */
    fun verifySignature(context: Context): Boolean {
        // Debug builds are signed with ~/.android/debug.keystore (per-machine, per-developer).
        // The integrity check is only meaningful for released APKs distributed publicly.
        if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) return true
        return runCatching { currentSignerMatches(context) }.getOrElse { false }
    }

    private fun currentSignerMatches(context: Context): Boolean {
        val pm = context.packageManager
        val info = pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        // Current signer only — never signingCertificateHistory, which still contains key0.
        val signers = info.signingInfo?.apkContentsSigners ?: return false
        val md = MessageDigest.getInstance("SHA-256")
        val want = expected()
        return signers.any { sig ->
            md.reset()
            md.digest(sig.toByteArray()).contentEquals(want)
        }
    }
}
