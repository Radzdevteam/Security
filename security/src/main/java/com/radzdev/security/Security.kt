package com.radzdev.security

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.util.*

class Security(private val context: Context) {
    private var sha1Hash: String? = null
    private var shouldCheck = false


    private val checker = "aHR0cHM6Ly9yYXcuZ2l0aHViLmNvbS9SYWR6ZGV2dGVhbS9TZWN1cml0eVBhY2thZ2VDaGVja2VyL3JlZnMvaGVhZHMvbWFpbi9jaGVja2Vy"
    private val httpClient = OkHttpClient()

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

        if (!appNameMatches || !packageNameMatches || !signatureMatches) {
            compromised()
        } else {
            validatePackageInDatabase()
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
                val sha1 = android.util.Base64.encodeToString(digest, android.util.Base64.DEFAULT)
                if (sha1 == sha1Hash) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Handle exception (no log statements)
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
        if (context is Activity) {
            context.finishAffinity()
        }
        System.exit(0)
    }

    private fun validatePackageInDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Decode the Base64 encoded URL
                val decodedUrl = String(Base64.decode(checker, Base64.DEFAULT))

                val request = Request.Builder()
                    .url(decodedUrl)
                    .get()
                    .build()

                val response = httpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonObject = JSONObject(responseBody)
                        val validPackages = jsonObject.getJSONArray("valid_packages")

                        val isPackageValid = (0 until validPackages.length()).any {
                            validPackages.getString(it) == context.packageName
                        }

                        withContext(Dispatchers.Main) {
                            if (!isPackageValid) {
                                showInvalidPackageDialog()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exception (no log statements)
            }
        }
    }

    private fun showInvalidPackageDialog() {
        if (context is Activity) {
            AlertDialog.Builder(context)
                .setTitle("App Access Denied")
                .setMessage("Access to Radz Respiratories is restricted due to security and compliance protocols. Please contact the developer to resolve the issue and obtain the necessary authorization.")
                .setCancelable(false)
                .setPositiveButton("EXIT") { _, _ ->
                    context.finishAffinity()
                    System.exit(0)
                }
                .show()
        }
    }
}
