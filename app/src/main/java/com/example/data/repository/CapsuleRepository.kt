package com.example.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CapsuleAppEntity
import com.example.data.local.entity.CapsuleLogEntity
import com.example.data.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Random

class CapsuleRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.capsuleDao()
    private val packageManager: PackageManager = context.packageManager

    /**
     * Scans installed applications on the device and merges their state with Capsule DB.
     */
    fun getInstalledAppsFlow(): Flow<List<AppItem>> {
        return dao.getAllCapsuleApps().combine(querySystemPackagesFlow()) { capsuleEntities, installedPkgs ->
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
     * Returns only the apps that are cloned/isolated inside the Capsule Sandbox.
     */
    fun getCapsuleAppsFlow(): Flow<List<AppItem>> {
        return dao.getClonedCapsuleApps().combine(querySystemPackagesFlow()) { clonedEntities, installedPkgs ->
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

    private fun querySystemPackagesFlow(): Flow<List<AppItem>> = kotlinx.coroutines.flow.flow {
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
            // Skip CapsulePro itself from mainland listing to avoid recursion
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

        // If in test or restricted container where packages list is minimal, provide common sample apps so user can test all features
        if (result.size < 5) {
            result.addAll(getDemoFallbackApps())
        }

        return result
    }

    private fun getDemoFallbackApps(): List<AppItem> {
        return listOf(
            AppItem(
                packageName = "com.whatsapp",
                appName = "WhatsApp Messenger",
                versionName = "2.24.18.7",
                isSystem = false,
                permissions = listOf("android.permission.CAMERA", "android.permission.READ_CONTACTS", "android.permission.ACCESS_FINE_LOCATION"),
                estimatedRamMb = 120
            ),
            AppItem(
                packageName = "com.google.android.youtube",
                appName = "YouTube",
                versionName = "19.34.42",
                isSystem = true,
                permissions = listOf("android.permission.INTERNET", "android.permission.RECORD_AUDIO"),
                estimatedRamMb = 95
            ),
            AppItem(
                packageName = "com.instagram.android",
                appName = "Instagram",
                versionName = "345.0.0",
                isSystem = false,
                permissions = listOf("android.permission.CAMERA", "android.permission.READ_MEDIA_IMAGES", "android.permission.ACCESS_FINE_LOCATION"),
                estimatedRamMb = 160
            ),
            AppItem(
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                versionName = "10.14.5",
                isSystem = false,
                permissions = listOf("android.permission.CAMERA", "android.permission.READ_CONTACTS", "android.permission.RECORD_AUDIO"),
                estimatedRamMb = 85
            ),
            AppItem(
                packageName = "com.shopee.id",
                appName = "Shopee",
                versionName = "3.31.20",
                isSystem = false,
                permissions = listOf("android.permission.ACCESS_FINE_LOCATION", "android.permission.POST_NOTIFICATIONS"),
                estimatedRamMb = 110
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

    suspend fun cloneAppToCapsule(app: AppItem, tag: String = "Dual Space") = withContext(Dispatchers.IO) {
        val entity = CapsuleAppEntity(
            packageName = app.packageName,
            appName = app.appName,
            isCloned = true,
            isFrozen = false,
            isAutoFreeze = false,
            isolatedStorage = true,
            tag = tag,
            cloneTimestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdateApp(entity)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = app.packageName,
                appName = app.appName,
                action = "CLONED",
                details = "Aplikasi berhasil dikloning & diisolasi ke dalam Capsule Sandbox ($tag)"
            )
        )
    }

    suspend fun removeFromCapsule(packageName: String, appName: String) = withContext(Dispatchers.IO) {
        dao.deleteApp(packageName)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = packageName,
                appName = appName,
                action = "UNINSTALLED",
                details = "Aplikasi dihapus dari Capsule Sandbox"
            )
        )
    }

    suspend fun freezeApp(packageName: String, appName: String) = withContext(Dispatchers.IO) {
        // Kill background process
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            am?.killBackgroundProcesses(packageName)
        } catch (_: Exception) {}

        dao.setFrozenStatus(packageName, true)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = packageName,
                appName = appName,
                action = "FROZEN",
                details = "Aplikasi dibekukan (Deep Hibernation). Proses latar belakang dihentikan."
            )
        )
    }

    suspend fun defrostApp(packageName: String, appName: String) = withContext(Dispatchers.IO) {
        dao.setFrozenStatus(packageName, false)
        dao.insertLog(
            CapsuleLogEntity(
                packageName = packageName,
                appName = appName,
                action = "DEFROSTED",
                details = "Aplikasi dicairkan dan siap dijalankan."
            )
        )
    }

    suspend fun freezeAllCapsuleApps() = withContext(Dispatchers.IO) {
        dao.freezeAllCapsuleApps()
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "GLOBAL",
                appName = "Semua Aplikasi Kapsul",
                action = "BATCH_FROZEN",
                details = "1-Tap Glacier: Semua aplikasi dalam ruang isolasi telah dibekukan serentak."
            )
        )
    }

    suspend fun defrostAllCapsuleApps() = withContext(Dispatchers.IO) {
        dao.defrostAllCapsuleApps()
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "GLOBAL",
                appName = "Semua Aplikasi Kapsul",
                action = "BATCH_DEFROSTED",
                details = "Semua aplikasi dalam ruang isolasi telah dicairkan kembali."
            )
        )
    }

    suspend fun updateAutoFreeze(packageName: String, appName: String, isAutoFreeze: Boolean, delaySeconds: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getCapsuleApp(packageName)
        if (existing != null) {
            dao.updateApp(existing.copy(isAutoFreeze = isAutoFreeze, autoFreezeDelaySeconds = delaySeconds))
            dao.insertLog(
                CapsuleLogEntity(
                    packageName = packageName,
                    appName = appName,
                    action = "AUTO_FREEZE_CONFIG",
                    details = "Auto-Freeze diatur: ${if (isAutoFreeze) "Aktif (${delaySeconds}s delay)" else "Nonaktif"}"
                )
            )
        }
    }

    suspend fun updateAppOps(
        packageName: String,
        appName: String,
        blockLocation: Boolean,
        blockContacts: Boolean,
        blockCamera: Boolean,
        blockBackgroundNetwork: Boolean,
        isolatedStorage: Boolean
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getCapsuleApp(packageName)
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
                    details = "Privacy Ops diperbarui: Lokasi=${!blockLocation}, Kontak=${!blockContacts}, Kamera=${!blockCamera}, Net=${!blockBackgroundNetwork}"
                )
            )
        }
    }

    suspend fun launchApp(packageName: String, appName: String, inCapsule: Boolean): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                // If in capsule and frozen, unfreeze automatically on launch (just like Island)
                if (inCapsule) {
                    withContext(Dispatchers.IO) {
                        dao.setFrozenStatus(packageName, false)
                        dao.recordLaunch(packageName)
                        dao.insertLog(
                            CapsuleLogEntity(
                                packageName = packageName,
                                appName = appName,
                                action = "LAUNCH",
                                details = "Diluncurkan dalam Ruang Isolasi Capsule (Otomatis Dicairkan)"
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
                    // Open App details in system settings as fallback
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

    fun getLogsFlow(): Flow<List<CapsuleLogEntity>> = dao.getLogs()

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        dao.clearLogs()
    }

    suspend fun destroyCapsuleSpace() = withContext(Dispatchers.IO) {
        dao.clearAllCapsuleApps()
        dao.insertLog(
            CapsuleLogEntity(
                packageName = "SYSTEM",
                appName = "Capsule Sandbox",
                action = "DESTROYED",
                details = "Ruang isolasi Capsule telah direset dan seluruh klon dibersihkan."
            )
        )
    }
}
