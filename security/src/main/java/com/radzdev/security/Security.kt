package com.radzdev.security

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.security.MessageDigest
import java.util.Base64

class Security(private val context: Context) {
    private var sha1Hash: String? = null
    private var shouldCheck = false

    fun setSHA1(hash: String?) {
        this.sha1Hash = hash
        this.shouldCheck = true
    }

    fun checkAppIntegrity(appNameBytes: ByteArray?, packageNameBytes: ByteArray?) {
        val appName = String(appNameBytes!!)
        val packageName = String(packageNameBytes!!)

        val appNameMatches = matchesAscii(appName, applicationLabel)
        val packageNameMatches = matchesAscii(packageName, context.packageName)
        val signatureMatches = matchesSignature()

        Log.d("Security", "App Name match: $appNameMatches")
        Log.d("Security", "Package Name match: $packageNameMatches")
        Log.d("Security", "Signature match: $signatureMatches")

        if (!appNameMatches) {
            Log.d(
                "Security",
                "App Name does not match! Expected: $appName, Actual: $applicationLabel"
            )
        }

        if (!packageNameMatches) {
            Log.d(
                "Security",
                "Package Name does not match! Expected: $packageName, Actual: ${context.packageName}"
            )
        }

        if (!signatureMatches) {
            Log.d("Security", "Signature does not match!")
        }

        if (!appNameMatches || !packageNameMatches || !signatureMatches) {
            Log.d("Security", "Integrity check failed.")
            compromised()
        } else {
            Log.d("Security", "Integrity check passed.")
        }
    }

    private fun matchesSignature(): Boolean {
        if (!shouldCheck || sha1Hash.isNullOrEmpty()) return true

        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo.apkContentsSigners
            } else {
                TODO("VERSION.SDK_INT < P")
            }
            val messageDigest = MessageDigest.getInstance("SHA-1")

            for (signature in signatures) {
                val digest = messageDigest.digest(signature.toByteArray())
                val sha1 = Base64.getEncoder().encodeToString(digest)
                Log.d("Security", "Calculated SHA-1: $sha1")
                if (sha1 == sha1Hash) {
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e("Security", "Error while verifying signature: ${e.message}")
        }

        return false
    }

    private val applicationLabel: String
        get() {
            val packageManager = context.packageManager
            return packageManager.getApplicationLabel(context.applicationInfo).toString()
        }

    private fun matchesAscii(expected: String, actual: String): Boolean {
        return expected == actual
    }

    private fun compromised() {
        Log.d("Security", "App compromised!")
        if (context is Activity) {
            context.finishAffinity()
        }
        System.exit(0)
    }
}
