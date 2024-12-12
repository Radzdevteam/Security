@file:Suppress("DEPRECATION")

package com.radzdev.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class InternetChecker(private val context: Context) {

    // Check if the device has an active internet connection
    fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // For devices running Android 10 (API level 29) and above, use NetworkCapabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            // For older versions, use the legacy method
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo?.isConnected == true
        }
    }
}

