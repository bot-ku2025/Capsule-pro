package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CapsuleAppEntity
import com.example.data.local.entity.CapsuleLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CapsuleDao {

    @Query("SELECT * FROM capsule_apps")
    fun getAllCapsuleApps(): Flow<List<CapsuleAppEntity>>

    @Query("SELECT * FROM capsule_apps WHERE isCloned = 1")
    fun getClonedCapsuleApps(): Flow<List<CapsuleAppEntity>>

    @Query("SELECT * FROM capsule_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getCapsuleApp(packageName: String): CapsuleAppEntity?

    @Query("SELECT * FROM capsule_apps WHERE packageName = :packageName LIMIT 1")
    fun observeCapsuleApp(packageName: String): Flow<CapsuleAppEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApp(app: CapsuleAppEntity)

    @Update
    suspend fun updateApp(app: CapsuleAppEntity)

    @Query("UPDATE capsule_apps SET isFrozen = :isFrozen, frozenCount = frozenCount + CASE WHEN :isFrozen = 1 THEN 1 ELSE 0 END WHERE packageName = :packageName")
    suspend fun setFrozenStatus(packageName: String, isFrozen: Boolean)

    @Query("UPDATE capsule_apps SET isFrozen = 1, frozenCount = frozenCount + 1 WHERE isCloned = 1")
    suspend fun freezeAllCapsuleApps()

    @Query("UPDATE capsule_apps SET isFrozen = 0 WHERE isCloned = 1")
    suspend fun defrostAllCapsuleApps()

    @Query("UPDATE capsule_apps SET isAutoFreeze = :isAutoFreeze WHERE packageName = :packageName")
    suspend fun setAutoFreeze(packageName: String, isAutoFreeze: Boolean)

    @Query("UPDATE capsule_apps SET lastLaunchedTimestamp = :timestamp, launchCount = launchCount + 1 WHERE packageName = :packageName")
    suspend fun recordLaunch(packageName: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM capsule_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Query("DELETE FROM capsule_apps")
    suspend fun clearAllCapsuleApps()

    // Logs
    @Query("SELECT * FROM capsule_logs ORDER BY timestamp DESC LIMIT 150")
    fun getLogs(): Flow<List<CapsuleLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CapsuleLogEntity)

    @Query("DELETE FROM capsule_logs")
    suspend fun clearLogs()
}
