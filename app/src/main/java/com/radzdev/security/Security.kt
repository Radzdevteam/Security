package com.radzdev.security

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import com.google.firebase.database.*

class Security(private val context: Context) {

    private var sha1Hash: String? = null
    private var shouldCheck = false
    private val database = FirebaseDatabase.getInstance()
    private val registeredPackagesRef = database.getReference("registered_packages")
    private val internetChecker = InternetChecker(context)

    fun setSHA1(hash: String?) {
        sha1Hash = hash
        shouldCheck = true
    }

    fun checkAppIntegrity(appNameBytes: ByteArray, packageNameBytes: ByteArray) {
        // Check internet before proceeding
        if (!internetChecker.isInternetAvailable()) {
            showNoInternetDialog()
            return
        }

        val appName = String(appNameBytes)
        val packageName = String(packageNameBytes)

        val appNameMatches = matchesAscii(appName, getApplicationLabel())
        val packageNameMatches = matchesAscii(packageName, context.packageName)

        Log.d("Security", "App Name match: $appNameMatches")
        Log.d("Security", "Package Name match: $packageNameMatches")

        // Detailed logs when there is a mismatch
        if (!appNameMatches) {
            Log.d("Security", "App Name does not match! Expected: $appName, Actual: ${getApplicationLabel()}")
        }

        if (!packageNameMatches) {
            Log.d("Security", "Package Name does not match! Expected: $packageName, Actual: ${context.packageName}")
        }

        if (!appNameMatches || !packageNameMatches) {
            Log.d("Security", "Integrity check failed.")
            compromised()
        } else {
            Log.d("Security", "Integrity check passed.")
        }
    }

    fun check() {
        // Check internet before proceeding
        if (!internetChecker.isInternetAvailable()) {
            showNoInternetDialog()
            return
        }

        val packageName = context.packageName

        // Fetch registered packages from Firebase Realtime Database
        registeredPackagesRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                val registeredPackages = dataSnapshot?.children?.mapNotNull { it.getValue(String::class.java) } ?: emptyList()

                if (!registeredPackages.contains(packageName)) {
                    showUnregisteredDialog()
                } else {
                    Log.d("Security", "Package name is registered.")
                }
            } else {
                Log.e("Security", "Failed to fetch registered packages: ${task.exception?.message}")
            }
        }
    }

    private fun showUnregisteredDialog() {
        val builder = AlertDialog.Builder(context)
            .setTitle("Unauthorized Application Detected")
            .setMessage("This application is not authorized to execute as it has not been registered with Radz App Updater. For further assistance, please contact the developer via Facebook at Mhuradz Alegre.")
            .setCancelable(false)

        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(context)
            .setTitle("No Internet Connection")
            .setMessage("An active internet connection is required to proceed. Please connect to the internet and try again.")
            .setCancelable(false)
            .setPositiveButton("Retry") { dialog, _ ->
                dialog.dismiss()
                check()
            }

        val dialog = builder.create()
        dialog.setCancelable(false)
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
