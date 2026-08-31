package com.example.data.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val appName: String,
    val versionName: String = "1.0",
    val icon: Drawable? = null,
    val isSystem: Boolean = false,
    val isCloned: Boolean = false,
    val isFrozen: Boolean = false,
    val isAutoFreeze: Boolean = false,
    val autoFreezeDelaySeconds: Int = 15,
    val isolatedStorage: Boolean = true,
    val blockLocation: Boolean = false,
    val blockContacts: Boolean = false,
    val blockCamera: Boolean = false,
    val blockBackgroundNetwork: Boolean = false,
    val launchCount: Int = 0,
    val frozenCount: Int = 0,
    val lastLaunchedTimestamp: Long = 0L,
    val tag: String = "Capsule",
    val customNote: String = "",
    val permissions: List<String> = emptyList(),
    val estimatedRamMb: Int = 45
)
