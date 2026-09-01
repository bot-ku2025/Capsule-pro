package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "capsule_snapshots")
data class CapsuleSnapshotEntity(
    @PrimaryKey
    val snapshotId: String,
    val profileId: String,
    val profileName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val label: String,
    val appCount: Int,
    val frozenCount: Int,
    val isAutoSnapshot: Boolean = false,
    val appsJson: String
)
