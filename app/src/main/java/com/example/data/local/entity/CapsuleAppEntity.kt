package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "capsule_apps",
    primaryKeys = ["packageName", "profileId"]
)
data class CapsuleAppEntity(
    val packageName: String,
    val profileId: String = "profile_1",
    val appName: String,
    val isCloned: Boolean = true,
    val isFrozen: Boolean = false,
    val isAutoFreeze: Boolean = false,
    val autoFreezeDelaySeconds: Int = 15,
    val isolatedStorage: Boolean = true,
    val blockLocation: Boolean = false,
    val blockContacts: Boolean = false,
    val blockCamera: Boolean = false,
    val blockBackgroundNetwork: Boolean = false,
    val cloneTimestamp: Long = System.currentTimeMillis(),
    val lastLaunchedTimestamp: Long = 0L,
    val launchCount: Int = 0,
    val frozenCount: Int = 0,
    val tag: String = "Capsule",
    val customNote: String = ""
)
