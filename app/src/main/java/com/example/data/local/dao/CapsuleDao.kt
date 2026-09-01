package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.CapsuleAppEntity
import com.example.data.local.entity.CapsuleLogEntity
import com.example.data.local.entity.CapsuleProfileEntity
import com.example.data.local.entity.CapsuleSnapshotEntity
import com.example.data.local.entity.IdentityConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CapsuleDao {

    // --- PROFILES ---
    @Query("SELECT * FROM capsule_profiles ORDER BY createdTimestamp ASC")
    fun getAllProfiles(): Flow<List<CapsuleProfileEntity>>

    @Query("SELECT * FROM capsule_profiles WHERE isCurrent = 1 LIMIT 1")
    fun observeCurrentProfile(): Flow<CapsuleProfileEntity?>

    @Query("SELECT * FROM capsule_profiles WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentProfile(): CapsuleProfileEntity?

    @Query("SELECT * FROM capsule_profiles WHERE profileId = :profileId LIMIT 1")
    suspend fun getProfileById(profileId: String): CapsuleProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CapsuleProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<CapsuleProfileEntity>)

    @Update
    suspend fun updateProfile(profile: CapsuleProfileEntity)

    @Query("UPDATE capsule_profiles SET isCurrent = 0")
    suspend fun clearCurrentProfileFlags()

    @Query("UPDATE capsule_profiles SET isCurrent = 1, lastActiveTimestamp = :timestamp WHERE profileId = :profileId")
    suspend fun setActiveProfileFlag(profileId: String, timestamp: Long = System.currentTimeMillis())

    @Transaction
    suspend fun switchActiveProfile(profileId: String) {
        clearCurrentProfileFlags()
        setActiveProfileFlag(profileId, System.currentTimeMillis())
    }

    @Query("DELETE FROM capsule_profiles WHERE profileId = :profileId")
    suspend fun deleteProfile(profileId: String)

    // --- APPS PER PROFILE ---
    @Query("SELECT * FROM capsule_apps")
    fun getAllCapsuleApps(): Flow<List<CapsuleAppEntity>>

    @Query("SELECT * FROM capsule_apps WHERE profileId = :profileId")
    fun getAllCapsuleAppsByProfile(profileId: String): Flow<List<CapsuleAppEntity>>

    @Query("SELECT * FROM capsule_apps WHERE profileId = :profileId AND isCloned = 1")
    fun getClonedCapsuleAppsByProfile(profileId: String): Flow<List<CapsuleAppEntity>>

    @Query("SELECT * FROM capsule_apps WHERE profileId = :profileId AND isCloned = 1")
    suspend fun getClonedCapsuleAppsList(profileId: String): List<CapsuleAppEntity>

    @Query("SELECT * FROM capsule_apps WHERE packageName = :packageName AND profileId = :profileId LIMIT 1")
    suspend fun getCapsuleApp(packageName: String, profileId: String): CapsuleAppEntity?

    @Query("SELECT * FROM capsule_apps WHERE packageName = :packageName AND profileId = :profileId LIMIT 1")
    fun observeCapsuleApp(packageName: String, profileId: String): Flow<CapsuleAppEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApp(app: CapsuleAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApps(apps: List<CapsuleAppEntity>)

    @Update
    suspend fun updateApp(app: CapsuleAppEntity)

    @Query("UPDATE capsule_apps SET isFrozen = :isFrozen, frozenCount = frozenCount + CASE WHEN :isFrozen = 1 THEN 1 ELSE 0 END WHERE packageName = :packageName AND profileId = :profileId")
    suspend fun setFrozenStatus(packageName: String, profileId: String, isFrozen: Boolean)

    @Query("UPDATE capsule_apps SET isFrozen = 1, frozenCount = frozenCount + 1 WHERE profileId = :profileId AND isCloned = 1")
    suspend fun freezeAllCapsuleApps(profileId: String)

    @Query("UPDATE capsule_apps SET isFrozen = 0 WHERE profileId = :profileId AND isCloned = 1")
    suspend fun defrostAllCapsuleApps(profileId: String)

    @Query("UPDATE capsule_apps SET isAutoFreeze = :isAutoFreeze WHERE packageName = :packageName AND profileId = :profileId")
    suspend fun setAutoFreeze(packageName: String, profileId: String, isAutoFreeze: Boolean)

    @Query("UPDATE capsule_apps SET lastLaunchedTimestamp = :timestamp, launchCount = launchCount + 1 WHERE packageName = :packageName AND profileId = :profileId")
    suspend fun recordLaunch(packageName: String, profileId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM capsule_apps WHERE packageName = :packageName AND profileId = :profileId")
    suspend fun deleteApp(packageName: String, profileId: String)

    @Query("DELETE FROM capsule_apps WHERE profileId = :profileId")
    suspend fun clearAppsForProfile(profileId: String)

    @Query("DELETE FROM capsule_apps")
    suspend fun clearAllCapsuleApps()

    // --- SNAPSHOTS & BACKUP ---
    @Query("SELECT * FROM capsule_snapshots WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getSnapshotsForProfile(profileId: String): Flow<List<CapsuleSnapshotEntity>>

    @Query("SELECT * FROM capsule_snapshots ORDER BY timestamp DESC")
    fun getAllSnapshots(): Flow<List<CapsuleSnapshotEntity>>

    @Query("SELECT * FROM capsule_snapshots WHERE profileId = :profileId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshot(profileId: String): CapsuleSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: CapsuleSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshots(snapshots: List<CapsuleSnapshotEntity>)

    @Query("DELETE FROM capsule_snapshots WHERE snapshotId = :snapshotId")
    suspend fun deleteSnapshot(snapshotId: String)

    @Query("DELETE FROM capsule_snapshots WHERE profileId = :profileId")
    suspend fun clearSnapshotsForProfile(profileId: String)

    // --- IDENTITY / DEVICE SPOOFING ---
    @Query("SELECT * FROM identity_configs WHERE profileId = :profileId LIMIT 1")
    fun observeIdentityConfig(profileId: String): Flow<IdentityConfigEntity?>

    @Query("SELECT * FROM identity_configs WHERE profileId = :profileId LIMIT 1")
    suspend fun getIdentityConfig(profileId: String): IdentityConfigEntity?

    @Query("SELECT * FROM identity_configs")
    fun getAllIdentitiesFlow(): Flow<List<IdentityConfigEntity>>

    @Query("SELECT * FROM identity_configs")
    suspend fun getAllIdentityConfigs(): List<IdentityConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateIdentityConfig(config: IdentityConfigEntity)

    @Query("DELETE FROM identity_configs WHERE profileId = :profileId")
    suspend fun deleteIdentityConfig(profileId: String)

    @Query("DELETE FROM identity_configs")
    suspend fun clearAllIdentityConfigs()

    // --- LOGS ---
    @Query("SELECT * FROM capsule_logs ORDER BY timestamp DESC LIMIT 150")
    fun getLogs(): Flow<List<CapsuleLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CapsuleLogEntity)

    @Query("DELETE FROM capsule_logs")
    suspend fun clearLogs()
}
