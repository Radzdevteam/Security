package com.radzdev.security

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Security(private val context: Context) {

    private var sha1Hash: String? = null
    private var shouldCheck = false
    private val jsonUrl = "https://raw.githubusercontent.com/Radzdevteam/SecurityPackageChecker/refs/heads/main/checker"

    fun setSHA1(hash: String?) {
        sha1Hash = hash
        shouldCheck = true
    }

    fun checkAppIntegrity(appNameBytes: ByteArray, packageNameBytes: ByteArray) {
        val appName = String(appNameBytes)
        val packageName = String(packageNameBytes)

        val appNameMatches = matchesAscii(appName, getApplicationLabel())
        val packageNameMatches = matchesAscii(packageName, context.packageName)

        Log.d("Security", "App Name match: $appNameMatches")
        Log.d("Security", "Package Name match: $packageNameMatches")

        if (!appNameMatches || !packageNameMatches) {
            Log.d("Security", "Integrity check failed.")
            compromised()
        } else {
            Log.d("Security", "Integrity check passed.")
        }
    }

    fun check() {
        val packageName = context.packageName

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(jsonUrl)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Security", "Failed to fetch JSON: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val isRegistered = isPackageRegistered(responseBody, packageName)
                    (context as? android.app.Activity)?.runOnUiThread {
                        if (!isRegistered) {
                            showUnregisteredDialog()
                        } else {
                            Log.d("Security", "Package name is registered.")
                        }
                    }
                }
            }
        })
    }

    private fun isPackageRegistered(jsonData: String, packageName: String): Boolean {
        return try {
            val jsonObject = JSONObject(jsonData)
            val registeredPackages = jsonObject.getJSONArray("registered_packages")
            for (i in 0 until registeredPackages.length()) {
                if (registeredPackages.getString(i) == packageName) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            Log.e("Security", "Error parsing JSON: ${e.message}")
            false
        }
    }

    private fun showUnregisteredDialog() {
        val builder = AlertDialog.Builder(context)
            .setTitle("App Not Registered")
            .setMessage("This app is either not registered on Radz App Updater or has been terminated. Contact the developer on Facebook at Mhuradz Alegre.")
            .setCancelable(false)

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


    private fun matchesAscii(expected: String, actual: String): Boolean {
        return expected == actual
    }

    private fun getApplicationLabel(): String {
        return context.packageManager.getApplicationLabel(context.applicationInfo).toString()
    }

    private fun compromised() {
        Log.d("Security", "App compromised!")
        (context as? android.app.Activity)?.finishAffinity()
        System.exit(0)
    }
}
