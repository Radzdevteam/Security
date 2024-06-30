@file:Suppress("DEPRECATION")

package com.radzdev.security

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale

class Security(private val context: Context) {
    private var sha1Hash: String? = null
    private var shouldCheck = false

    fun setSHA1(hash: String?) {
        sha1Hash = hash
        shouldCheck = true
    }

    fun check() {
        if (shouldCheck && currentSignature != sha1Hash) {
            compromised()
        }
    }

    private val currentSignature: String
        get() {
            val signature = AppInfoUtils.getSignature(context, context.packageName, "SHA1")
            return Base64Utils.formatSignature(signature?.lowercase(Locale.getDefault()))
        }

    private fun compromised() {
        // Show a toast or log the event if needed
        (context as Activity).finishAffinity()
        System.exit(0)
    }

    private object Base64Utils {
        fun formatSignature(signature: String?): String {
            if (signature.isNullOrEmpty()) return signature ?: ""
            return signature.chunked(2).joinToString(":")
        }
    }

    private object AppInfoUtils {
        fun getSignature(context: Context, packageName: String, algorithm: String): String? {
            val signatures = getSignatures(context, packageName)
            return if (signatures.isNotEmpty()) {
                getSignatureString(signatures[0], algorithm)
            } else null
        }

        private fun getSignatures(context: Context, packageName: String): Array<Signature> {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    packageInfo.signingInfo.apkContentsSigners
                } else {
                    val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                    packageInfo.signatures
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                emptyArray()
            }
        }

        private fun getSignatureString(signature: Signature, algorithm: String): String {
            return try {
                val digest = MessageDigest.getInstance(algorithm).digest(signature.toByteArray())
                digest.joinToString("") { String.format("%02x", it) }
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                "error!"
            }
        }
    }
}
