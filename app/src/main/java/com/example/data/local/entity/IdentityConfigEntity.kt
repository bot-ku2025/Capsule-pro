package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "identity_configs")
data class IdentityConfigEntity(
    @PrimaryKey
    val profileId: String,
    // Device Metadata
    val brand: String = "Google",
    val model: String = "Pixel 8 Pro (husky)",
    val androidVersion: String = "Android 14 (API 34)",
    val codename: String = "husky",
    val boardPlatform: String = "zuma / tensor",
    val manufacturer: String = "Google",
    val productDevice: String = "husky",

    // Identifiers
    val androidId: String = "4a9f8b1c0e2d3f7a",
    val imei1: String = "864502058291048",
    val imei2: String = "864502058291055",
    val serialNumber: String = "R5CW10A8XYZ",
    val fingerprint: String = "google/husky/husky:14/UQ1A.240205.004/11269751:user/release-keys",
    val gsfId: String = "3a89bc12e4f01",

    // Network Spoofing
    val wifiMac: String = "02:4B:89:A1:FE:2C",
    val wifiSsid: String = "Home_Secure_5G",
    val wifiBssid: String = "f0:9f:c2:74:18:20",
    val bluetoothMac: String = "00:1A:7D:DA:71:13",
    val bluetoothName: String = "Capsule Device",

    // Tracking & App Identifiers
    val advertisingId: String = "38400000-8cf0-11bd-b23e-10b96e40000d",
    val appSetId: String = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    val widevineDrmId: String = "WV-DRM-492019482910",
    val userAgent: String = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36",
    val installerPackage: String = "com.android.vending",
    val hiddenKeyboardPackages: String = "com.google.android.inputmethod.latin",
    val virtualDefaultIme: String = "com.android.inputmethod.latin/.LatinIME",

    val lastSynchronizedTimestamp: Long = 0L,
    val isFresh: Boolean = false
)
