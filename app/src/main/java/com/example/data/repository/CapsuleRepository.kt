package com.example.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CapsuleAppEntity
import com.example.data.local.entity.CapsuleLogEntity
import com.example.data.local.entity.CapsuleProfileEntity
import com.example.data.local.entity.CapsuleSnapshotEntity
import com.example.data.local.entity.IdentityConfigEntity
import com.example.data.model.AppItem
import com.example.util.DeviceIdentityGenerator
import com.example.util.MigrationParsedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CapsuleRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.capsuleDao()
    private val packageManager: PackageManager = context.packageManager

    init {
        // Run default profile setup if needed
        CoroutineScope(Dispatchers.IO).launch {
            ensureDefaultProfiles()
        }
    }

    suspend fun ensureDefaultProfiles() = withContext(Dispatchers.IO) {
        val current = dao.getCurrentProfile()
        if (current == null) {
            val defaultProfile = CapsuleProfileEntity(
                profileId = "profile_1",
                profileName = "Profil 1 (Utama)",
                colorHex = 0xFF00E5FF, // Cyan
                iconName = "person",
                isCurrent = true,
                autoSnapshotEnabled = true,
                customNotes = "Profil isolasi default"
            )
            dao.insertProfile(defaultProfile)
        }
    }

    // --- PROFILES ---
    fun getAllProfilesFlow(): Flow<List<CapsuleProfileEntity>> = dao.getAllProfiles()

    fun observeCurrentProfile(): Flow<CapsuleProfileEntity?> = dao.observeCurrentProfile()

    suspend fun getCurrentProfile(): CapsuleProfileEntity {
        return dao.getCurrentProfile() ?: run {
            val fallback = CapsuleProfileEntity(
                profileId = "profile_1",
                profileName = "Profil 1 (Utama)",
                colorHex = 0xFF00E5FF,
                isCurrent = true
            )
            dao.insertProfile(fallback)
            fallback
        }
    }

    suspend fun createProfile(name: String, colorHex: Long): CapsuleProfileEntity = withContext(Dispatchers.IO) {
        val profileId = "profile_" + UUID.randomUUID().toString().take(8)
        val profile = CapsuleProfileEntity(
            profileId = profileId,
            profileName = name.ifBlank { "Profil Baru" },
            colorHex = colorHex,
            isCurrent = false,
            autoSnapshotEnabled = true
        )
        dao.insertProfile(profile)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "PROFILE",
                appName = profile.profileName,
                action = "PROFILE_CREATED",
                details = "Profil isolasi baru '${profile.profileName}' berhasil dibuat."
            )
        )
        profile
    }

    suspend fun switchProfile(targetProfileId: String) = withContext(Dispatchers.IO) {
        val current = dao.getCurrentProfile()
        if (current != null && current.profileId != targetProfileId) {
            // 1. Auto-Snapshot of current profile before switching out
            if (current.autoSnapshotEnabled) {
                createAutoSnapshot(current.profileId, current.profileName, "Auto-Snapshot sebelum beralih ke profil lain")
            }
        }
        dao.switchActiveProfile(targetProfileId)
        val newProfile = dao.getProfileById(targetProfileId)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "PROFILE",
                appName = newProfile?.profileName ?: targetProfileId,
                action = "PROFILE_SWITCHED",
                details = "Beralih aktif ke profil '${newProfile?.profileName}'."
            )
        )
    }

    suspend fun updateProfile(profileId: String, newName: String, colorHex: Long) = withContext(Dispatchers.IO) {
        val existing = dao.getProfileById(profileId) ?: return@withContext
        val updated = existing.copy(
            profileName = newName.ifBlank { existing.profileName },
            colorHex = colorHex,
            lastActiveTimestamp = System.currentTimeMillis()
        )
        dao.updateProfile(updated)
    }

    suspend fun deleteProfile(profileId: String) = withContext(Dispatchers.IO) {
        val profile = dao.getProfileById(profileId)
        dao.clearAppsForProfile(profileId)
        dao.clearSnapshotsForProfile(profileId)
        dao.deleteProfile(profileId)

        // If the deleted profile was current, fallback to another or default
        val current = dao.getCurrentProfile()
        if (current == null) {
            ensureDefaultProfiles()
        }

        dao.insertLog(
            CapsuleLogEntity(
                packageName = "PROFILE",
                appName = profile?.profileName ?: profileId,
                action = "PROFILE_DELETED",
                details = "Profil '${profile?.profileName}' dan seluruh data kloningnya dihapus."
            )
        )
    }

    // --- APPS & SANDBOXING ---

    /**
     * Returns installed applications on Mainland mapped against current profile apps.
     */
    fun getInstalledAppsFlow(profileId: String): Flow<List<AppItem>> {
        return dao.getAllCapsuleAppsByProfile(profileId).combine(querySystemPackagesFlow()) { capsuleEntities, installedPkgs ->
            val entityMap = capsuleEntities.associateBy { it.packageName }
            installedPkgs.map { pkg ->
                val entity = entityMap[pkg.packageName]
                if (entity != null) {
                    pkg.copy(
                        isCloned = entity.isCloned,
                        isFrozen = entity.isFrozen,
                        isAutoFreeze = entity.isAutoFreeze,
                        autoFreezeDelaySeconds = entity.autoFreezeDelaySeconds,
                        isolatedStorage = entity.isolatedStorage,
                        blockLocation = entity.blockLocation,
                        blockContacts = entity.blockContacts,
                        blockCamera = entity.blockCamera,
                        blockBackgroundNetwork = entity.blockBackgroundNetwork,
                        launchCount = entity.launchCount,
                        frozenCount = entity.frozenCount,
                        lastLaunchedTimestamp = entity.lastLaunchedTimestamp,
                        tag = entity.tag,
                        customNote = entity.customNote
                    )
                } else {
                    pkg
                }
            }.sortedBy { it.appName.lowercase() }
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Returns only the apps cloned/isolated inside the active Capsule profile.
     */
    fun getCapsuleAppsFlow(profileId: String): Flow<List<AppItem>> {
        return dao.getClonedCapsuleAppsByProfile(profileId).combine(querySystemPackagesFlow()) { clonedEntities, installedPkgs ->
            val installedMap = installedPkgs.associateBy { it.packageName }
            clonedEntities.map { entity ->
                val installed = installedMap[entity.packageName]
                AppItem(
                    packageName = entity.packageName,
                    appName = installed?.appName ?: entity.appName,
                    versionName = installed?.versionName ?: "1.0",
                    icon = installed?.icon,
                    isSystem = installed?.isSystem ?: false,
                    isCloned = true,
                    isFrozen = entity.isFrozen,
                    isAutoFreeze = entity.isAutoFreeze,
                    autoFreezeDelaySeconds = entity.autoFreezeDelaySeconds,
                    isolatedStorage = entity.isolatedStorage,
                    blockLocation = entity.blockLocation,
                    blockContacts = entity.blockContacts,
                    blockCamera = entity.blockCamera,
                    blockBackgroundNetwork = entity.blockBackgroundNetwork,
                    launchCount = entity.launchCount,
                    frozenCount = entity.frozenCount,
                    lastLaunchedTimestamp = entity.lastLaunchedTimestamp,
                    tag = entity.tag,
                    customNote = entity.customNote,
                    permissions = installed?.permissions ?: emptyList(),
                    estimatedRamMb = installed?.estimatedRamMb ?: 60
                )
            }.sortedWith(compareByDescending<AppItem> { it.isFrozen }.thenBy { it.appName.lowercase() })
        }.flowOn(Dispatchers.IO)
    }

    private fun querySystemPackagesFlow(): Flow<List<AppItem>> = flow {
        val apps = querySystemPackages()
        emit(apps)
    }

    fun querySystemPackages(): List<AppItem> {
        val packages: List<PackageInfo> = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) {
            emptyList()
        }

        val result = mutableListOf<AppItem>()
        for (pkg in packages) {
            val appInfo = pkg.applicationInfo ?: continue
            if (pkg.packageName == context.packageName) continue

            val appName = try {
                appInfo.loadLabel(packageManager).toString()
            } catch (e: Exception) {
                pkg.packageName
            }

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val icon = try {
                appInfo.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }

            val versionName = pkg.versionName ?: "1.0"
            val requestedPermissions = pkg.requestedPermissions?.toList() ?: emptyList()
            val ramMb = 25 + (Math.abs(pkg.packageName.hashCode()) % 80)

            result.add(
                AppItem(
                    packageName = pkg.packageName,
                    appName = appName,
                    versionName = versionName,
                    icon = icon,
                    isSystem = isSystem,
                    permissions = requestedPermissions,
                    estimatedRamMb = ramMb
                )
            )
        }

        if (result.size < 5) {
            result.addAll(getDemoFallbackApps())
        }

        return result
    }

    private fun getDemoFallbackApps(): List<AppItem> {
        return listOf(
            AppItem(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                versionName = "2.24.18",
                isSystem = false,
                permissions = listOf("android.permission.CAMERA", "android.permission.READ_CONTACTS"),
                estimatedRamMb = 95
            ),
            AppItem(
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                versionName = "10.14.5",
                isSystem = false,
                permissions = listOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"),
                estimatedRamMb = 110
            ),
            AppItem(
                packageName = "com.zhiliaoapp.musically",
                appName = "TikTok",
                versionName = "36.2.4",
                isSystem = false,
                permissions = listOf("android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION"),
                estimatedRamMb = 160
            ),
            AppItem(
                packageName = "id.dana",
                appName = "DANA Dompet Digital",
                versionName = "2.55.0",
                isSystem = false,
                permissions = listOf("android.permission.CAMERA", "android.permission.READ_PHONE_STATE"),
                estimatedRamMb = 85
            ),
            AppItem(
                packageName = "com.shopee.id",
                appName = "Shopee",
                versionName = "3.28.10",
                isSystem = false,
                permissions = listOf("android.permission.ACCESS_COARSE_LOCATION"),
                estimatedRamMb = 120
            ),
            AppItem(
                packageName = "com.google.android.gm",
                appName = "Gmail",
                versionName = "2024.08.11",
                isSystem = true,
                permissions = listOf("android.permission.GET_ACCOUNTS", "android.permission.READ_CONTACTS"),
                estimatedRamMb = 70
            ),
            AppItem(
                packageName = "com.mobile.legends",
                appName = "Mobile Legends: Bang Bang",
                versionName = "1.8.92",
                isSystem = false,
                permissions = listOf("android.permission.RECORD_AUDIO", "android.permission.ACCESS_NETWORK_STATE"),
                estimatedRamMb = 320
            ),
            AppItem(
                packageName = "com.spotify.music",
                appName = "Spotify",
                versionName = "8.9.64",
                isSystem = false,
                permissions = listOf("android.permission.INTERNET", "android.permission.BLUETOOTH_CONNECT"),
                estimatedRamMb = 80
            )
        )
    }

    suspend fun cloneAppToCapsule(app: AppItem, profileId: String, tag: String = "Dual Space") = withContext(Dispatchers.IO) {
        val entity = CapsuleAppEntity(
            packageName = app.packageName,
            profileId = profileId,
            appName = app.appName,
            isCloned = true,
            isFrozen = false,
            isAutoFreeze = false,
            isolatedStorage = true,
            tag = tag,
            cloneTimestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdateApp(entity)
        val profile = dao.getProfileById(profileId)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = app.packageName,
                appName = app.appName,
                action = "CLONED",
                details = "Aplikasi dikloning ke [${profile?.profileName ?: profileId}] ($tag)"
            )
        )
    }

    suspend fun cloneAppToProfile(packageName: String, profileId: String, appName: String, tag: String = "Play Store Direct") = withContext(Dispatchers.IO) {
        val entity = CapsuleAppEntity(
            packageName = packageName,
            profileId = profileId,
            appName = appName,
            isCloned = true,
            isFrozen = false,
            isAutoFreeze = false,
            isolatedStorage = true,
            tag = tag,
            cloneTimestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdateApp(entity)
        val profile = dao.getProfileById(profileId)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = packageName,
                appName = appName,
                action = "PLAY_INSTALLED",
                details = "Aplikasi $appName dipasang langsung ke [${profile?.profileName ?: profileId}]"
            )
        )
    }

    suspend fun removeFromCapsule(packageName: String, profileId: String, appName: String) = withContext(Dispatchers.IO) {
        dao.deleteApp(packageName, profileId)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = packageName,
                appName = appName,
                action = "UNINSTALLED",
                details = "Aplikasi dihapus dari profil $profileId"
            )
        )
    }

    suspend fun freezeApp(packageName: String, profileId: String, appName: String) = withContext(Dispatchers.IO) {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            am?.killBackgroundProcesses(packageName)
        } catch (_: Exception) {}

        dao.setFrozenStatus(packageName, profileId, true)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = packageName,
                appName = appName,
                action = "FROZEN",
                details = "Aplikasi dibekukan di profil $profileId."
            )
        )
    }

    suspend fun defrostApp(packageName: String, profileId: String, appName: String) = withContext(Dispatchers.IO) {
        dao.setFrozenStatus(packageName, profileId, false)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = packageName,
                appName = appName,
                action = "DEFROSTED",
                details = "Aplikasi dicairkan di profil $profileId."
            )
        )
    }

    suspend fun freezeAllCapsuleApps(profileId: String) = withContext(Dispatchers.IO) {
        dao.freezeAllCapsuleApps(profileId)
        val profile = dao.getProfileById(profileId)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "GLOBAL",
                appName = profile?.profileName ?: profileId,
                action = "BATCH_FROZEN",
                details = "Semua aplikasi dalam profil [${profile?.profileName}] telah dibekukan serentak."
            )
        )
    }

    suspend fun defrostAllCapsuleApps(profileId: String) = withContext(Dispatchers.IO) {
        dao.defrostAllCapsuleApps(profileId)
        val profile = dao.getProfileById(profileId)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "GLOBAL",
                appName = profile?.profileName ?: profileId,
                action = "BATCH_DEFROSTED",
                details = "Semua aplikasi dalam profil [${profile?.profileName}] telah dicairkan kembali."
            )
        )
    }

    suspend fun updateAutoFreeze(packageName: String, profileId: String, appName: String, isAutoFreeze: Boolean, delaySeconds: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getCapsuleApp(packageName, profileId)
        if (existing != null) {
            dao.updateApp(existing.copy(isAutoFreeze = isAutoFreeze, autoFreezeDelaySeconds = delaySeconds))
            dao.insertLog(
                CapsuleLogEntity(
                    packageName = packageName,
                    appName = appName,
                    action = "AUTO_FREEZE_CONFIG",
                    details = "Auto-Freeze [Profil $profileId]: ${if (isAutoFreeze) "Aktif (${delaySeconds}s)" else "Nonaktif"}"
                )
            )
        }
    }

    suspend fun updateAppOps(
        packageName: String,
        profileId: String,
        appName: String,
        blockLocation: Boolean,
        blockContacts: Boolean,
        blockCamera: Boolean,
        blockBackgroundNetwork: Boolean,
        isolatedStorage: Boolean
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getCapsuleApp(packageName, profileId)
        if (existing != null) {
            val updated = existing.copy(
                blockLocation = blockLocation,
                blockContacts = blockContacts,
                blockCamera = blockCamera,
                blockBackgroundNetwork = blockBackgroundNetwork,
                isolatedStorage = isolatedStorage
            )
            dao.updateApp(updated)
            dao.insertLog(
                CapsuleLogEntity(
                    packageName = packageName,
                    appName = appName,
                    action = "PRIVACY_GUARD",
                    details = "Privacy Ops diperbarui di profil $profileId."
                )
            )
        }
    }

    suspend fun launchApp(packageName: String, profileId: String, appName: String, inCapsule: Boolean): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                if (inCapsule) {
                    withContext(Dispatchers.IO) {
                        dao.setFrozenStatus(packageName, profileId, false)
                        dao.recordLaunch(packageName, profileId)
                        dao.insertLog(
                            CapsuleLogEntity(
                                packageName = packageName,
                                appName = appName,
                                action = "LAUNCH",
                                details = "Diluncurkan dalam Ruang Isolasi [$profileId] (Otomatis Dicairkan)"
                            )
                        )
                    }
                }

                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    true
                } else {
                    openAppSettings(packageName)
                    true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membuka $appName: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun openAppSettings(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka info aplikasi", Toast.LENGTH_SHORT).show()
        }
    }

    // --- REAL-TIME SNAPSHOT & BACKUP SYSTEM ---

    fun getSnapshotsForProfileFlow(profileId: String): Flow<List<CapsuleSnapshotEntity>> = dao.getSnapshotsForProfile(profileId)

    suspend fun createAutoSnapshot(profileId: String, profileName: String, reason: String = "Auto-Snapshot Real-Time"): CapsuleSnapshotEntity = withContext(Dispatchers.IO) {
        val apps = dao.getClonedCapsuleAppsList(profileId)
        val jsonArray = JSONArray()
        apps.forEach { app ->
            val obj = JSONObject().apply {
                put("packageName", app.packageName)
                put("appName", app.appName)
                put("isCloned", app.isCloned)
                put("isFrozen", app.isFrozen)
                put("isAutoFreeze", app.isAutoFreeze)
                put("autoFreezeDelaySeconds", app.autoFreezeDelaySeconds)
                put("isolatedStorage", app.isolatedStorage)
                put("blockLocation", app.blockLocation)
                put("blockContacts", app.blockContacts)
                put("blockCamera", app.blockCamera)
                put("blockBackgroundNetwork", app.blockBackgroundNetwork)
                put("tag", app.tag)
                put("customNote", app.customNote)
                put("launchCount", app.launchCount)
                put("frozenCount", app.frozenCount)
            }
            jsonArray.put(obj)
        }

        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeStr = sdf.format(Date())
        val snapshot = CapsuleSnapshotEntity(
            snapshotId = "snap_" + UUID.randomUUID().toString().take(8),
            profileId = profileId,
            profileName = profileName,
            timestamp = System.currentTimeMillis(),
            label = "$reason ($timeStr)",
            appCount = apps.size,
            frozenCount = apps.count { it.isFrozen },
            isAutoSnapshot = true,
            appsJson = jsonArray.toString()
        )
        dao.insertSnapshot(snapshot)
        snapshot
    }

    suspend fun createManualSnapshot(profileId: String, customLabel: String): CapsuleSnapshotEntity = withContext(Dispatchers.IO) {
        val profile = dao.getProfileById(profileId)
        val profileName = profile?.profileName ?: "Profil $profileId"
        val apps = dao.getClonedCapsuleAppsList(profileId)
        val jsonArray = JSONArray()
        apps.forEach { app ->
            val obj = JSONObject().apply {
                put("packageName", app.packageName)
                put("appName", app.appName)
                put("isCloned", app.isCloned)
                put("isFrozen", app.isFrozen)
                put("isAutoFreeze", app.isAutoFreeze)
                put("autoFreezeDelaySeconds", app.autoFreezeDelaySeconds)
                put("isolatedStorage", app.isolatedStorage)
                put("blockLocation", app.blockLocation)
                put("blockContacts", app.blockContacts)
                put("blockCamera", app.blockCamera)
                put("blockBackgroundNetwork", app.blockBackgroundNetwork)
                put("tag", app.tag)
                put("customNote", app.customNote)
                put("launchCount", app.launchCount)
                put("frozenCount", app.frozenCount)
            }
            jsonArray.put(obj)
        }

        val snapshot = CapsuleSnapshotEntity(
            snapshotId = "snap_" + UUID.randomUUID().toString().take(8),
            profileId = profileId,
            profileName = profileName,
            timestamp = System.currentTimeMillis(),
            label = customLabel.ifBlank { "Snapshot Manual" },
            appCount = apps.size,
            frozenCount = apps.count { it.isFrozen },
            isAutoSnapshot = false,
            appsJson = jsonArray.toString()
        )
        dao.insertSnapshot(snapshot)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "SNAPSHOT",
                appName = profileName,
                action = "SNAPSHOT_CREATED",
                details = "Snapshot '${snapshot.label}' berhasil dibuat (${snapshot.appCount} aplikasi)."
            )
        )
        snapshot
    }

    suspend fun restoreSnapshot(snapshot: CapsuleSnapshotEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray(snapshot.appsJson)
            val restoredApps = mutableListOf<CapsuleAppEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                restoredApps.add(
                    CapsuleAppEntity(
                        packageName = obj.getString("packageName"),
                        profileId = snapshot.profileId,
                        appName = obj.optString("appName", "App"),
                        isCloned = obj.optBoolean("isCloned", true),
                        isFrozen = obj.optBoolean("isFrozen", false),
                        isAutoFreeze = obj.optBoolean("isAutoFreeze", false),
                        autoFreezeDelaySeconds = obj.optInt("autoFreezeDelaySeconds", 15),
                        isolatedStorage = obj.optBoolean("isolatedStorage", true),
                        blockLocation = obj.optBoolean("blockLocation", false),
                        blockContacts = obj.optBoolean("blockContacts", false),
                        blockCamera = obj.optBoolean("blockCamera", false),
                        blockBackgroundNetwork = obj.optBoolean("blockBackgroundNetwork", false),
                        tag = obj.optString("tag", "Capsule"),
                        customNote = obj.optString("customNote", ""),
                        launchCount = obj.optInt("launchCount", 0),
                        frozenCount = obj.optInt("frozenCount", 0),
                        cloneTimestamp = System.currentTimeMillis()
                    )
                )
            }

            // Replace profile apps with restored snapshot state
            dao.clearAppsForProfile(snapshot.profileId)
            if (restoredApps.isNotEmpty()) {
                dao.insertOrUpdateApps(restoredApps)
            }

            dao.insertLog(
                CapsuleLogEntity(
                    packageName = "RESTORE",
                    appName = snapshot.profileName,
                    action = "SNAPSHOT_RESTORED",
                    details = "Data dipulihkan dari snapshot '${snapshot.label}' (${restoredApps.size} aplikasi)."
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreLastSnapshot(profileId: String): Boolean = withContext(Dispatchers.IO) {
        val latest = dao.getLatestSnapshot(profileId) ?: return@withContext false
        restoreSnapshot(latest)
    }

    suspend fun deleteSnapshot(snapshotId: String) = withContext(Dispatchers.IO) {
        dao.deleteSnapshot(snapshotId)
    }

    // --- FULL BACKUP EXPORT & IMPORT ---

    suspend fun exportFullBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 2)
        root.put("appName", "CapsulePro")
        root.put("exportTimestamp", System.currentTimeMillis())

        val currentProfile = dao.getCurrentProfile()
        root.put("currentProfileId", currentProfile?.profileId ?: "profile_1")

        // Profiles array
        val profilesArray = JSONArray()
        // We query from DB via Flow collection or direct
        val profiles = mutableListOf<CapsuleProfileEntity>()
        // Let's get current profiles
        currentProfile?.let { profiles.add(it) }
        profiles.forEach { p ->
            val pObj = JSONObject().apply {
                put("profileId", p.profileId)
                put("profileName", p.profileName)
                put("colorHex", p.colorHex)
                put("iconName", p.iconName)
                put("autoSnapshotEnabled", p.autoSnapshotEnabled)
            }
            profilesArray.put(pObj)
        }
        root.put("profiles", profilesArray)

        // All apps in current profile
        val apps = currentProfile?.let { dao.getClonedCapsuleAppsList(it.profileId) } ?: emptyList()
        val appsArray = JSONArray()
        apps.forEach { app ->
            val aObj = JSONObject().apply {
                put("packageName", app.packageName)
                put("appName", app.appName)
                put("profileId", app.profileId)
                put("isCloned", app.isCloned)
                put("isFrozen", app.isFrozen)
                put("isAutoFreeze", app.isAutoFreeze)
                put("autoFreezeDelaySeconds", app.autoFreezeDelaySeconds)
                put("isolatedStorage", app.isolatedStorage)
                put("blockLocation", app.blockLocation)
                put("blockContacts", app.blockContacts)
                put("blockCamera", app.blockCamera)
                put("blockBackgroundNetwork", app.blockBackgroundNetwork)
                put("tag", app.tag)
                put("customNote", app.customNote)
            }
            appsArray.put(aObj)
        }
        root.put("apps", appsArray)

        root.toString(2)
    }

    suspend fun importFullBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val appsArray = root.optJSONArray("apps") ?: return@withContext false

            val currentProfile = getCurrentProfile()
            val profileId = currentProfile.profileId

            val restoredApps = mutableListOf<CapsuleAppEntity>()
            for (i in 0 until appsArray.length()) {
                val obj = appsArray.getJSONObject(i)
                restoredApps.add(
                    CapsuleAppEntity(
                        packageName = obj.getString("packageName"),
                        profileId = profileId,
                        appName = obj.optString("appName", "App"),
                        isCloned = obj.optBoolean("isCloned", true),
                        isFrozen = obj.optBoolean("isFrozen", false),
                        isAutoFreeze = obj.optBoolean("isAutoFreeze", false),
                        autoFreezeDelaySeconds = obj.optInt("autoFreezeDelaySeconds", 15),
                        isolatedStorage = obj.optBoolean("isolatedStorage", true),
                        blockLocation = obj.optBoolean("blockLocation", false),
                        blockContacts = obj.optBoolean("blockContacts", false),
                        blockCamera = obj.optBoolean("blockCamera", false),
                        blockBackgroundNetwork = obj.optBoolean("blockBackgroundNetwork", false),
                        tag = obj.optString("tag", "Backup"),
                        customNote = obj.optString("customNote", ""),
                        cloneTimestamp = System.currentTimeMillis()
                    )
                )
            }

            if (restoredApps.isNotEmpty()) {
                dao.insertOrUpdateApps(restoredApps)
                // Save a snapshot of this import
                createAutoSnapshot(profileId, currentProfile.profileName, "Snapshot Pemulihan Cadangan File")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- IDENTITY / DEVICE SPOOFING ---
    fun observeIdentityConfig(profileId: String): Flow<IdentityConfigEntity?> = dao.observeIdentityConfig(profileId)

    suspend fun getIdentityConfig(profileId: String): IdentityConfigEntity = withContext(Dispatchers.IO) {
        dao.getIdentityConfig(profileId) ?: run {
            val fresh = DeviceIdentityGenerator.createFreshBlankIdentity(profileId)
            dao.insertOrUpdateIdentityConfig(fresh)
            fresh
        }
    }

    suspend fun getAllIdentityConfigs(): List<IdentityConfigEntity> = withContext(Dispatchers.IO) {
        dao.getAllIdentityConfigs()
    }

    suspend fun saveIdentityConfig(config: IdentityConfigEntity) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateIdentityConfig(config)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "IDENTITY",
                appName = config.brand + " " + config.model,
                action = "IDENTITY_UPDATED",
                details = "Identitas perangkat untuk profil '${config.profileId}' diperbarui (IMEI: ${config.imei1})."
            )
        )
    }

    suspend fun randomizeIdentityForProfile(profileId: String): IdentityConfigEntity = withContext(Dispatchers.IO) {
        val randomized = DeviceIdentityGenerator.generateRandomizedIdentity(profileId)
        dao.insertOrUpdateIdentityConfig(randomized)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "IDENTITY",
                appName = randomized.model,
                action = "IDENTITY_RANDOMIZED",
                details = "Identitas baru diacak untuk profil '$profileId': ${randomized.brand} ${randomized.model} (IMEI: ${randomized.imei1})."
            )
        )
        randomized
    }

    // --- UNIVERSAL MIGRATION RESTORE ---
    suspend fun restoreUniversalMigrationPackage(
        data: MigrationParsedData,
        isFullRootRestore: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Restore profiles
            if (data.profiles.isNotEmpty()) {
                dao.insertProfiles(data.profiles)
            }

            // 2. Restore apps
            if (data.apps.isNotEmpty()) {
                dao.insertOrUpdateApps(data.apps)
            }

            // 3. Restore snapshots
            if (data.snapshots.isNotEmpty()) {
                dao.insertSnapshots(data.snapshots)
            }

            // 4. Restore identity configs
            data.identities.forEach { identity ->
                dao.insertOrUpdateIdentityConfig(identity)
            }

            val modeStr = if (isFullRootRestore) "Full Root Data (Termasuk Sesi Akun)" else "Standar Universal"
            dao.insertLog(
                CapsuleLogEntity(
                    packageName = "MIGRATION",
                    appName = "Restore .capsule",
                    action = "RESTORE_COMPLETE",
                    details = "Pemulihan berhasil ($modeStr): ${data.profiles.size} profil, ${data.apps.size} aplikasi, ${data.identities.size} konfigurasi identitas."
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- LOGS ---
    fun getAllCapsuleAppsFlow(): Flow<List<CapsuleAppEntity>> = dao.getAllCapsuleApps()
    fun getAllIdentitiesFlow(): Flow<List<IdentityConfigEntity>> = dao.getAllIdentitiesFlow()
    fun getAllSnapshotsFlow(): Flow<List<CapsuleSnapshotEntity>> = dao.getAllSnapshots()

    fun getLogsFlow(): Flow<List<CapsuleLogEntity>> = dao.getLogs()

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        dao.clearLogs()
    }

    suspend fun destroyCapsuleSpace() = withContext(Dispatchers.IO) {
        val current = getCurrentProfile()
        dao.clearAppsForProfile(current.profileId)
        dao.clearSnapshotsForProfile(current.profileId)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "SYSTEM",
                appName = current.profileName,
                action = "DESTROYED",
                details = "Ruang isolasi profil '${current.profileName}' telah direset."
            )
        )
    }
}
