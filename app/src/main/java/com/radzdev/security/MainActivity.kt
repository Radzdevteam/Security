package com.radzdev.security

import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Set the expected SHA1 hash for app signature
        val cs = Security(this)
        cs.setSHA1("f6:7b:4a:7f:92:0b:ae:ad:ef:79:4a:ca:d8:18:55:df:b7:99:0d:9e")

        // Construct the byte arrays for APP_NAME and PACKAGE_NAME
        val appNameBytes = byteArrayOf(115, 101, 99, 117, 114, 105, 116, 121)
        val packageNameBytes = byteArrayOf(
            99, 111, 109, 46, 114, 97, 100, 122, 100, 101, 118, 46, 115, 101, 99, 117, 114, 105, 116, 121
        )

        // Pass the byte arrays to checkAppIntegrity
        cs.checkAppIntegrity(appNameBytes, packageNameBytes)

        // Check if the signature matches
        cs.check()
    }
}
