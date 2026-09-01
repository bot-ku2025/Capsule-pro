package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "capsule_profiles")
data class CapsuleProfileEntity(
    @PrimaryKey
    val profileId: String,
    val profileName: String,
    val colorHex: Long = 0xFF00E5FF, // Capsule Cyan default
    val iconName: String = "person",
    val createdTimestamp: Long = System.currentTimeMillis(),
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = false,
    val autoSnapshotEnabled: Boolean = true,
    val customNotes: String = ""
)
