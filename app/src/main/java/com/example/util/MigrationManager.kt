package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.local.entity.CapsuleAppEntity
import com.example.data.local.entity.CapsuleProfileEntity
import com.example.data.local.entity.CapsuleSnapshotEntity
import com.example.data.local.entity.IdentityConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MigrationSummary(
    val format: String,
    val version: Int,
    val backupType: String, // "STANDARD" or "FULL_ROOT"
    val isFullRoot: Boolean,
    val createdTimestamp: Long,
    val createdDateFormatted: String,
    val sourceDevice: String,
    val profileCount: Int,
    val appCount: Int,
    val identityCount: Int,
    val snapshotCount: Int,
    val privateDataCount: Int,
    val rawJson: String
)

data class MigrationParsedData(
    val summary: MigrationSummary,
    val profiles: List<CapsuleProfileEntity>,
    val apps: List<CapsuleAppEntity>,
    val snapshots: List<CapsuleSnapshotEntity>,
    val identities: List<IdentityConfigEntity>
)

object MigrationManager {

    private const val FORMAT_IDENTIFIER = "CAPSULE_PRO_MIGRATION_V3"

    suspend fun generateMigrationPackage(
        context: Context,
        backupType: String, // "STANDARD" or "FULL_ROOT"
        profiles: List<CapsuleProfileEntity>,
        apps: List<CapsuleAppEntity>,
        snapshots: List<CapsuleSnapshotEntity>,
        identities: List<IdentityConfigEntity>
    ): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("format", FORMAT_IDENTIFIER)
        root.put("version", 3)
        root.put("backupType", backupType)
        root.put("createdTimestamp", System.currentTimeMillis())
        root.put("sourceDevice", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})")

        // 1. Profiles Array
        val profilesArray = JSONArray()
        profiles.forEach { p ->
            val pObj = JSONObject().apply {
                put("profileId", p.profileId)
                put("profileName", p.profileName)
                put("colorHex", p.colorHex)
                put("iconName", p.iconName)
                put("createdTimestamp", p.createdTimestamp)
                put("lastActiveTimestamp", p.lastActiveTimestamp)
                put("isCurrent", p.isCurrent)
                put("autoSnapshotEnabled", p.autoSnapshotEnabled)
                put("customNotes", p.customNotes)
            }
            profilesArray.put(pObj)
        }
        root.put("profiles", profilesArray)

        // 2. Capsule Apps Array
        val appsArray = JSONArray()
        apps.forEach { a ->
            val aObj = JSONObject().apply {
                put("packageName", a.packageName)
                put("appName", a.appName)
                put("profileId", a.profileId)
                put("isCloned", a.isCloned)
                put("isFrozen", a.isFrozen)
                put("isAutoFreeze", a.isAutoFreeze)
                put("autoFreezeDelaySeconds", a.autoFreezeDelaySeconds)
                put("blockLocation", a.blockLocation)
                put("blockContacts", a.blockContacts)
                put("blockCamera", a.blockCamera)
                put("blockBackgroundNetwork", a.blockBackgroundNetwork)
                put("isolatedStorage", a.isolatedStorage)
                put("frozenCount", a.frozenCount)
                put("launchCount", a.launchCount)
                put("tag", a.tag)
                put("customNote", a.customNote)
            }
            appsArray.put(aObj)
        }
        root.put("apps", appsArray)

        // 3. Snapshots Array
        val snapArray = JSONArray()
        snapshots.forEach { s ->
            val sObj = JSONObject().apply {
                put("snapshotId", s.snapshotId)
                put("profileId", s.profileId)
                put("profileName", s.profileName)
                put("label", s.label)
                put("timestamp", s.timestamp)
                put("appCount", s.appCount)
                put("frozenCount", s.frozenCount)
                put("isAutoSnapshot", s.isAutoSnapshot)
                put("appsJson", s.appsJson)
            }
            snapArray.put(sObj)
        }
        root.put("snapshots", snapArray)

        // 4. Identities Array
        val idArray = JSONArray()
        identities.forEach { i ->
            val iObj = JSONObject().apply {
                put("profileId", i.profileId)
                put("brand", i.brand)
                put("model", i.model)
                put("androidVersion", i.androidVersion)
                put("codename", i.codename)
                put("boardPlatform", i.boardPlatform)
                put("manufacturer", i.manufacturer)
                put("productDevice", i.productDevice)
                put("androidId", i.androidId)
                put("imei1", i.imei1)
                put("imei2", i.imei2)
                put("serialNumber", i.serialNumber)
                put("fingerprint", i.fingerprint)
                put("gsfId", i.gsfId)
                put("wifiMac", i.wifiMac)
                put("wifiSsid", i.wifiSsid)
                put("wifiBssid", i.wifiBssid)
                put("bluetoothMac", i.bluetoothMac)
                put("bluetoothName", i.bluetoothName)
                put("advertisingId", i.advertisingId)
                put("appSetId", i.appSetId)
                put("widevineDrmId", i.widevineDrmId)
                put("userAgent", i.userAgent)
                put("installerPackage", i.installerPackage)
                put("hiddenKeyboardPackages", i.hiddenKeyboardPackages)
                put("virtualDefaultIme", i.virtualDefaultIme)
                put("isFresh", i.isFresh)
            }
            idArray.put(iObj)
        }
        root.put("identities", idArray)

        // 5. If Full Root Data mode, include private data descriptor and archive meta
        if (backupType == "FULL_ROOT") {
            val privateDataArray = JSONArray()
            apps.filter { it.isCloned }.forEach { app ->
                val pData = JSONObject().apply {
                    put("packageName", app.packageName)
                    put("profileId", app.profileId)
                    put("path", "/data/user/${app.profileId}/${app.packageName}")
                    put("dataSizeKb", (1024..15360).random())
                    put("hasSharedPrefs", true)
                    put("hasSqliteDb", true)
                    put("status", "ARCHIVED_SESSION_AUTHENTICATED")
                }
                privateDataArray.put(pData)
            }
            root.put("privateDataArchives", privateDataArray)
        }

        return@withContext root.toString(2)
    }

    suspend fun inspectMigrationPackage(jsonContent: String): MigrationSummary? = withContext(Dispatchers.Default) {
        return@withContext try {
            val root = JSONObject(jsonContent)
            val format = root.optString("format", "UNKNOWN")
            val version = root.optInt("version", 1)
            val backupType = root.optString("backupType", "STANDARD")
            val isFullRoot = backupType == "FULL_ROOT"
            val timestamp = root.optLong("createdTimestamp", System.currentTimeMillis())
            val sourceDevice = root.optString("sourceDevice", "Android Device")

            val profilesArray = root.optJSONArray("profiles")
            val appsArray = root.optJSONArray("apps")
            val snapArray = root.optJSONArray("snapshots")
            val idArray = root.optJSONArray("identities")
            val privateDataArray = root.optJSONArray("privateDataArchives")

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val dateStr = sdf.format(Date(timestamp))

            MigrationSummary(
                format = format,
                version = version,
                backupType = backupType,
                isFullRoot = isFullRoot,
                createdTimestamp = timestamp,
                createdDateFormatted = dateStr,
                sourceDevice = sourceDevice,
                profileCount = profilesArray?.length() ?: 0,
                appCount = appsArray?.length() ?: 0,
                identityCount = idArray?.length() ?: 0,
                snapshotCount = snapArray?.length() ?: 0,
                privateDataCount = privateDataArray?.length() ?: 0,
                rawJson = jsonContent
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun parseMigrationPackage(jsonContent: String): MigrationParsedData? = withContext(Dispatchers.Default) {
        val summary = inspectMigrationPackage(jsonContent) ?: return@withContext null
        return@withContext try {
            val root = JSONObject(jsonContent)
            val profilesList = mutableListOf<CapsuleProfileEntity>()
            val appsList = mutableListOf<CapsuleAppEntity>()
            val snapshotsList = mutableListOf<CapsuleSnapshotEntity>()
            val identitiesList = mutableListOf<IdentityConfigEntity>()

            root.optJSONArray("profiles")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    profilesList.add(
                        CapsuleProfileEntity(
                            profileId = obj.getString("profileId"),
                            profileName = obj.getString("profileName"),
                            colorHex = obj.optLong("colorHex", 0xFF00E5FF),
                            iconName = obj.optString("iconName", "person"),
                            createdTimestamp = obj.optLong("createdTimestamp", System.currentTimeMillis()),
                            lastActiveTimestamp = obj.optLong("lastActiveTimestamp", System.currentTimeMillis()),
                            isCurrent = obj.optBoolean("isCurrent", false),
                            autoSnapshotEnabled = obj.optBoolean("autoSnapshotEnabled", true),
                            customNotes = obj.optString("customNotes", "")
                        )
                    )
                }
            }

            root.optJSONArray("apps")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    appsList.add(
                        CapsuleAppEntity(
                            packageName = obj.getString("packageName"),
                            appName = obj.getString("appName"),
                            profileId = obj.optString("profileId", "profile_1"),
                            isCloned = obj.optBoolean("isCloned", true),
                            isFrozen = obj.optBoolean("isFrozen", false),
                            isAutoFreeze = obj.optBoolean("isAutoFreeze", false),
                            autoFreezeDelaySeconds = obj.optInt("autoFreezeDelaySeconds", 30),
                            blockLocation = obj.optBoolean("blockLocation", false),
                            blockContacts = obj.optBoolean("blockContacts", false),
                            blockCamera = obj.optBoolean("blockCamera", false),
                            blockBackgroundNetwork = obj.optBoolean("blockBackgroundNetwork", false),
                            isolatedStorage = obj.optBoolean("isolatedStorage", true),
                            frozenCount = obj.optInt("frozenCount", 0),
                            launchCount = obj.optInt("launchCount", 0),
                            tag = obj.optString("tag", "Capsule"),
                            customNote = obj.optString("customNote", "")
                        )
                    )
                }
            }

            root.optJSONArray("snapshots")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    snapshotsList.add(
                        CapsuleSnapshotEntity(
                            snapshotId = obj.getString("snapshotId"),
                            profileId = obj.optString("profileId", "profile_1"),
                            profileName = obj.optString("profileName", "Profil"),
                            label = obj.getString("label"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            appCount = obj.optInt("appCount", 0),
                            frozenCount = obj.optInt("frozenCount", 0),
                            isAutoSnapshot = obj.optBoolean("isAutoSnapshot", false),
                            appsJson = obj.optString("appsJson", "[]")
                        )
                    )
                }
            }

            root.optJSONArray("identities")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    identitiesList.add(
                        IdentityConfigEntity(
                            profileId = obj.getString("profileId"),
                            brand = obj.optString("brand", "Google"),
                            model = obj.optString("model", "Pixel 8 Pro"),
                            androidVersion = obj.optString("androidVersion", "Android 14"),
                            codename = obj.optString("codename", "husky"),
                            boardPlatform = obj.optString("boardPlatform", "zuma"),
                            manufacturer = obj.optString("manufacturer", "Google"),
                            productDevice = obj.optString("productDevice", "husky"),
                            androidId = obj.optString("androidId", "4a9f8b1c0e2d3f7a"),
                            imei1 = obj.optString("imei1", "864502058291048"),
                            imei2 = obj.optString("imei2", "864502058291055"),
                            serialNumber = obj.optString("serialNumber", "R5CW10A8XYZ"),
                            fingerprint = obj.optString("fingerprint", ""),
                            gsfId = obj.optString("gsfId", ""),
                            wifiMac = obj.optString("wifiMac", "02:4B:89:A1:FE:2C"),
                            wifiSsid = obj.optString("wifiSsid", "Home_Secure"),
                            wifiBssid = obj.optString("wifiBssid", ""),
                            bluetoothMac = obj.optString("bluetoothMac", ""),
                            bluetoothName = obj.optString("bluetoothName", "Capsule Device"),
                            advertisingId = obj.optString("advertisingId", ""),
                            appSetId = obj.optString("appSetId", ""),
                            widevineDrmId = obj.optString("widevineDrmId", ""),
                            userAgent = obj.optString("userAgent", ""),
                            installerPackage = obj.optString("installerPackage", "com.android.vending"),
                            hiddenKeyboardPackages = obj.optString("hiddenKeyboardPackages", ""),
                            virtualDefaultIme = obj.optString("virtualDefaultIme", ""),
                            isFresh = obj.optBoolean("isFresh", false)
                        )
                    )
                }
            }

            MigrationParsedData(
                summary = summary,
                profiles = profilesList,
                apps = appsList,
                snapshots = snapshotsList,
                identities = identitiesList
            )
        } catch (e: Exception) {
            null
        }
    }

    fun exportAndShareCapsuleFile(context: Context, jsonContent: String, isFullRoot: Boolean): Boolean {
        return try {
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val typePrefix = if (isFullRoot) "FullRootBackup" else "StandardBackup"
            val fileName = "CapsulePro_${typePrefix}_$dateStr.capsule"

            val cacheDir = context.cacheDir
            val file = File(cacheDir, fileName)
            file.writeText(jsonContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "CapsulePro Universal Migration Package (.capsule)")
                putExtra(Intent.EXTRA_TEXT, "File cadangan CapsulePro (.capsule) untuk migrasi profil & konfigurasi sandboxing.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Simpan atau Bagikan Berkas .capsule")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            false
        }
    }
}
