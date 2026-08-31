package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "capsule_logs")
data class CapsuleLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val action: String, // "CLONE", "FREEZE", "DEFROST", "AUTO_FREEZE", "LAUNCH", "OPS_UPDATE", "UNINSTALL"
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)
