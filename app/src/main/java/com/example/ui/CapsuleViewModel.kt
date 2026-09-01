package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CapsuleAppEntity
import com.example.data.local.entity.CapsuleLogEntity
import com.example.data.local.entity.CapsuleProfileEntity
import com.example.data.local.entity.CapsuleSnapshotEntity
import com.example.data.local.entity.IdentityConfigEntity
import com.example.data.model.AppItem
import com.example.data.repository.CapsuleRepository
import com.example.util.DeviceIdentityGenerator
import com.example.util.DevicePreset
import com.example.util.IpRotationResult
import com.example.util.LanguageManager
import com.example.util.MigrationParsedData
import com.example.util.NetworkIpRotator
import com.example.util.PlayAppItem
import com.example.util.RootCommandResult
import com.example.util.RootEngine
import com.example.util.RootStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CapsuleTab {
    MAINLAND,      // Primary profile apps
    CAPSULE,       // Sandboxed / Cloned apps (Work Profile)
    IDENTITY,      // Device spoofing, Anti-fingerprint & IP Rotation
    GLACIER,       // Deep freeze & auto-freeze manager
    SHUTTLE_ADB,   // File Shuttle, Root & ADB/Shizuku/DPM commands
    SETTINGS_LOGS  // Audit logs, Privacy guard, Engine & Settings
}

enum class WorkingEngineMode(val title: String, val description: String) {
    ROOT("Mode Root (Superuser)", "Eksekusi langsung via Magisk/KernelSU tanpa PC/ADB"),
    SHIZUKU("Mode Shizuku / ADB", "Menggunakan Shizuku API / Wireless ADB shell"),
    DEVICE_ADMIN("Mode Device Admin (DPM)", "Menggunakan API Device Policy Manager Android"),
    SANDBOX("Mode Sandboxing Mandiri", "Isolasi data virtual tanpa root")
}

enum class MainlandFilter {
    ALL,
    USER_ONLY,
    SYSTEM_ONLY,
    NOT_IN_CAPSULE
}

enum class CapsuleFilter {
    ALL,
    ACTIVE,
    FROZEN,
    AUTO_FREEZE_ENABLED
}

@OptIn(ExperimentalCoroutinesApi::class)
class CapsuleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CapsuleRepository(application)

    private val _currentTab = MutableStateFlow(CapsuleTab.MAINLAND)
    val currentTab: StateFlow<CapsuleTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _mainlandFilter = MutableStateFlow(MainlandFilter.ALL)
    val mainlandFilter: StateFlow<MainlandFilter> = _mainlandFilter.asStateFlow()

    private val _capsuleFilter = MutableStateFlow(CapsuleFilter.ALL)
    val capsuleFilter: StateFlow<CapsuleFilter> = _capsuleFilter.asStateFlow()

    private val _selectedAppForDetail = MutableStateFlow<AppItem?>(null)
    val selectedAppForDetail: StateFlow<AppItem?> = _selectedAppForDetail.asStateFlow()

    private val _selectedAppForOps = MutableStateFlow<AppItem?>(null)
    val selectedAppForOps: StateFlow<AppItem?> = _selectedAppForOps.asStateFlow()

    private val _screenOffAutoFreeze = MutableStateFlow(true)
    val screenOffAutoFreeze: StateFlow<Boolean> = _screenOffAutoFreeze.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Root Status & Working Engine Mode State
    private val _rootStatus = MutableStateFlow(RootStatus(hasSuBinary = false, isGranted = false, message = "Memeriksa status root..."))
    val rootStatus: StateFlow<RootStatus> = _rootStatus.asStateFlow()

    private val _workingEngineMode = MutableStateFlow(WorkingEngineMode.SANDBOX)
    val workingEngineMode: StateFlow<WorkingEngineMode> = _workingEngineMode.asStateFlow()

    // Profiles
    val allProfiles: StateFlow<List<CapsuleProfileEntity>> = repository.getAllProfilesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentProfile: StateFlow<CapsuleProfileEntity?> = repository.observeCurrentProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Dynamic apps based on active profile
    val allInstalledApps: StateFlow<List<AppItem>> = currentProfile.flatMapLatest { profile ->
        val profileId = profile?.profileId ?: "profile_1"
        repository.getInstalledAppsFlow(profileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val capsuleApps: StateFlow<List<AppItem>> = currentProfile.flatMapLatest { profile ->
        val profileId = profile?.profileId ?: "profile_1"
        repository.getCapsuleAppsFlow(profileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Snapshots for active profile
    val currentProfileSnapshots: StateFlow<List<CapsuleSnapshotEntity>> = currentProfile.flatMapLatest { profile ->
        val profileId = profile?.profileId ?: "profile_1"
        repository.getSnapshotsForProfileFlow(profileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All entities across profiles for migration backup
    val allCapsuleAppEntities: StateFlow<List<CapsuleAppEntity>> = repository.getAllCapsuleAppsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSnapshots: StateFlow<List<CapsuleSnapshotEntity>> = repository.getAllSnapshotsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIdentities: StateFlow<List<IdentityConfigEntity>> = repository.getAllIdentitiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<CapsuleLogEntity>> = repository.getLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Identity Config for Active Profile
    val identityConfig: StateFlow<IdentityConfigEntity?> = currentProfile.flatMapLatest { profile ->
        val profileId = profile?.profileId ?: "profile_1"
        repository.observeIdentityConfig(profileId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // IP Fresh Dialog Popup State
    private val _lastIpRotationResult = MutableStateFlow<IpRotationResult?>(null)
    val lastIpRotationResult: StateFlow<IpRotationResult?> = _lastIpRotationResult.asStateFlow()

    // Action Execution Progress State
    private val _isExecutingAction = MutableStateFlow(false)
    val isExecutingAction: StateFlow<Boolean> = _isExecutingAction.asStateFlow()

    private val _actionStatusMessage = MutableStateFlow("")
    val actionStatusMessage: StateFlow<String> = _actionStatusMessage.asStateFlow()

    init {
        refreshRootStatus()
    }

    fun refreshRootStatus() {
        viewModelScope.launch {
            val status = RootEngine.checkRootStatus()
            _rootStatus.value = status
            if (status.isGranted) {
                _workingEngineMode.value = WorkingEngineMode.ROOT
            }
        }
    }

    fun setEngineMode(mode: WorkingEngineMode) {
        _workingEngineMode.value = mode
        showToast("✓ Engine aktif: ${mode.title}")
    }

    fun requestRootAccess() {
        viewModelScope.launch {
            val result = RootEngine.requestRootAccess()
            if (result.success && result.output.contains("uid=0")) {
                refreshRootStatus()
                _workingEngineMode.value = WorkingEngineMode.ROOT
                showToast(LanguageManager.getString("root_granted_toast"))
            } else {
                refreshRootStatus()
                showToast(LanguageManager.getString("root_denied_toast"))
            }
        }
    }

    fun testRootCommand() {
        viewModelScope.launch {
            val result = RootEngine.execute("id && echo 'Root Engine CapsulePro Berfungsi Normal'")
            if (result.success) {
                showToast("✓ Tes Root Berhasil:\n${result.output.take(80)}")
            } else {
                showToast("⚠️ Tes Root Gagal: ${result.error}")
            }
        }
    }

    fun setupWorkProfileViaRoot() {
        viewModelScope.launch {
            showToast("⚡ Menjalankan 1-Klik Setup Work Profile via Root...")
            val result = RootEngine.setupWorkProfileWithRoot(getApplication())
            if (result.success) {
                showToast(LanguageManager.getString("root_profile_success"))
                refreshRootStatus()
            } else {
                showToast("${LanguageManager.getString("root_profile_failed")} ${result.error}")
            }
        }
    }

    // --- PROFILE MANAGEMENT ---

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            repository.switchProfile(profileId)
            val profile = allProfiles.value.find { it.profileId == profileId }
            showToast("🔄 Beralih ke ${profile?.profileName ?: "Profil baru"}")
        }
    }

    fun createProfile(name: String, colorHex: Long) {
        viewModelScope.launch {
            val profile = repository.createProfile(name, colorHex)
            repository.switchProfile(profile.profileId)
            showToast("✨ Profil '${profile.profileName}' dibuat & diaktifkan")
        }
    }

    fun updateProfile(profileId: String, newName: String, colorHex: Long) {
        viewModelScope.launch {
            repository.updateProfile(profileId, newName, colorHex)
            showToast("✓ Profil diperbarui")
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
            showToast("🗑️ Profil berhasil dihapus")
        }
    }

    // --- SNAPSHOT & FULL BACKUP SYSTEM ---

    fun createManualSnapshot(label: String) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            val snap = repository.createManualSnapshot(profile.profileId, label)
            showToast("📸 Snapshot '${snap.label}' disimpan (${snap.appCount} aplikasi)")
        }
    }

    fun restoreSnapshot(snapshot: CapsuleSnapshotEntity) {
        viewModelScope.launch {
            val success = repository.restoreSnapshot(snapshot)
            if (success) {
                showToast("♻️ Snapshot '${snapshot.label}' berhasil dipulihkan!")
            } else {
                showToast("⚠️ Gagal memulihkan snapshot")
            }
        }
    }

    fun restoreLastSnapshot() {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            val success = repository.restoreLastSnapshot(profile.profileId)
            if (success) {
                showToast("♻️ Berhasil pulihkan snapshot terakhir (${profile.profileName})")
            } else {
                showToast("⚠️ Belum ada snapshot tersimpan untuk profil ini")
            }
        }
    }

    fun deleteSnapshot(snapshotId: String) {
        viewModelScope.launch {
            repository.deleteSnapshot(snapshotId)
            showToast("🗑️ Snapshot dihapus")
        }
    }

    fun exportFullBackup(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportFullBackupJson()
            onExportReady(json)
            showToast("📦 Data Full Backup siap dibagikan / diekspor")
        }
    }

    fun importFullBackup(jsonString: String) {
        viewModelScope.launch {
            val success = repository.importFullBackupJson(jsonString)
            if (success) {
                showToast("✅ Berhasil memulihkan Full Backup ke profil aktif!")
            } else {
                showToast("⚠️ Format file cadangan tidak valid")
            }
        }
    }

    // Filtered Mainland apps
    val filteredMainlandApps: StateFlow<List<AppItem>> = combine(
        allInstalledApps,
        _searchQuery,
        _mainlandFilter
    ) { apps, query, filter ->
        apps.filter { app ->
            val matchesQuery = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                MainlandFilter.ALL -> true
                MainlandFilter.USER_ONLY -> !app.isSystem
                MainlandFilter.SYSTEM_ONLY -> app.isSystem
                MainlandFilter.NOT_IN_CAPSULE -> !app.isCloned
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Capsule apps
    val filteredCapsuleApps: StateFlow<List<AppItem>> = combine(
        capsuleApps,
        _searchQuery,
        _capsuleFilter
    ) { apps, query, filter ->
        apps.filter { app ->
            val matchesQuery = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                CapsuleFilter.ALL -> true
                CapsuleFilter.ACTIVE -> !app.isFrozen
                CapsuleFilter.FROZEN -> app.isFrozen
                CapsuleFilter.AUTO_FREEZE_ENABLED -> app.isAutoFreeze
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics
    val frozenCount: StateFlow<Int> = capsuleApps.combine(_currentTab) { apps, _ ->
        apps.count { it.isFrozen }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val savedRamTotalMb: StateFlow<Int> = capsuleApps.combine(_currentTab) { apps, _ ->
        apps.filter { it.isFrozen }.sumOf { it.estimatedRamMb }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun selectTab(tab: CapsuleTab) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMainlandFilter(filter: MainlandFilter) {
        _mainlandFilter.value = filter
    }

    fun setCapsuleFilter(filter: CapsuleFilter) {
        _capsuleFilter.value = filter
    }

    fun selectAppForDetail(app: AppItem?) {
        _selectedAppForDetail.value = app
    }

    fun selectAppForOps(app: AppItem?) {
        _selectedAppForOps.value = app
    }

    fun toggleScreenOffAutoFreeze() {
        _screenOffAutoFreeze.value = !_screenOffAutoFreeze.value
    }

    fun cloneToCapsule(app: AppItem, tag: String = "Dual Space") {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            repository.cloneAppToCapsule(app, profile.profileId, tag)
            showToast("✓ ${app.appName} dikloning ke [${profile.profileName}]")
        }
    }

    fun removeFromCapsule(app: AppItem) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            repository.removeFromCapsule(app.packageName, profile.profileId, app.appName)
            if (_selectedAppForDetail.value?.packageName == app.packageName) {
                _selectedAppForDetail.value = null
            }
            showToast("${app.appName} dihapus dari ${profile.profileName}")
        }
    }

    fun freezeApp(app: AppItem) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            if (_workingEngineMode.value == WorkingEngineMode.ROOT) {
                RootEngine.freezeAppRoot(app.packageName)
            }
            repository.freezeApp(app.packageName, profile.profileId, app.appName)
            val engineNote = if (_workingEngineMode.value == WorkingEngineMode.ROOT) " (Root pm disable)" else ""
            showToast("❄️ ${app.appName} dibekukan$engineNote")
        }
    }

    fun defrostApp(app: AppItem) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            if (_workingEngineMode.value == WorkingEngineMode.ROOT) {
                RootEngine.defrostAppRoot(app.packageName)
            }
            repository.defrostApp(app.packageName, profile.profileId, app.appName)
            val engineNote = if (_workingEngineMode.value == WorkingEngineMode.ROOT) " (Root pm enable)" else ""
            showToast("⚡ ${app.appName} dicairkan$engineNote")
        }
    }

    fun freezeAllInactive() {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            if (_workingEngineMode.value == WorkingEngineMode.ROOT) {
                capsuleApps.value.forEach {
                    RootEngine.freezeAppRoot(it.packageName)
                }
            }
            repository.freezeAllCapsuleApps(profile.profileId)
            showToast("❄️ Semua aplikasi dalam [${profile.profileName}] dibekukan!")
        }
    }

    fun defrostAll() {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            if (_workingEngineMode.value == WorkingEngineMode.ROOT) {
                capsuleApps.value.forEach {
                    RootEngine.defrostAppRoot(it.packageName)
                }
            }
            repository.defrostAllCapsuleApps(profile.profileId)
            showToast("⚡ Semua aplikasi dalam [${profile.profileName}] dicairkan!")
        }
    }

    fun updateAutoFreezeConfig(app: AppItem, enabled: Boolean, delaySeconds: Int) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            repository.updateAutoFreeze(app.packageName, profile.profileId, app.appName, enabled, delaySeconds)
            showToast("Auto-Freeze ${if (enabled) "Diaktifkan ($delaySeconds detik)" else "Dinonaktifkan"} untuk ${app.appName}")
        }
    }

    fun updatePrivacyOps(
        app: AppItem,
        blockLocation: Boolean,
        blockContacts: Boolean,
        blockCamera: Boolean,
        blockBackgroundNetwork: Boolean,
        isolatedStorage: Boolean
    ) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            repository.updateAppOps(
                packageName = app.packageName,
                profileId = profile.profileId,
                appName = app.appName,
                blockLocation = blockLocation,
                blockContacts = blockContacts,
                blockCamera = blockCamera,
                blockBackgroundNetwork = blockBackgroundNetwork,
                isolatedStorage = isolatedStorage
            )
            showToast("🛡️ Privacy Guard diperbarui untuk ${app.appName}")
        }
    }

    fun launchApp(app: AppItem, inCapsule: Boolean) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            repository.launchApp(app.packageName, profile.profileId, app.appName, inCapsule)
        }
    }

    fun openAppSettings(packageName: String) {
        repository.openAppSettings(packageName)
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            showToast("Riwayat log audit dibersihkan")
        }
    }

    fun destroyCapsule() {
        viewModelScope.launch {
            repository.destroyCapsuleSpace()
            showToast("Ruang isolasi profil aktif telah direset")
        }
    }

    // --- IDENTITY & DEVICE SPOOFING ---
    fun randomizeCurrentProfileIdentity() {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            repository.randomizeIdentityForProfile(profile.profileId)
            showToast("🎲 Identitas acak baru dibuat untuk ${profile.profileName}")
        }
    }

    fun applyDevicePreset(preset: DevicePreset) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            val newConfig = DeviceIdentityGenerator.generateRandomizedIdentity(profile.profileId, preset)
            repository.saveIdentityConfig(newConfig)
            showToast("✓ Preset ${preset.model} berhasil diterapkan")
        }
    }

    fun updateIdentityConfig(config: IdentityConfigEntity) {
        viewModelScope.launch {
            repository.saveIdentityConfig(config)
            showToast("✓ Konfigurasi identitas diperbarui")
        }
    }

    // --- SYNCHRONIZE SYSTEM & IP ROTATION ---
    fun synchronizeSystem() {
        viewModelScope.launch {
            _isExecutingAction.value = true
            _actionStatusMessage.value = "🔄 Menyinkronkan identitas profil ke sistem kernel..."
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            val isRoot = _rootStatus.value.isGranted

            // 1. Sync identity system properties via root if available
            val config = repository.getIdentityConfig(profile.profileId)
            if (isRoot) {
                RootEngine.executeRoot("setprop ro.product.brand \"${config.brand}\"")
                RootEngine.executeRoot("setprop ro.product.model \"${config.model}\"")
                RootEngine.executeRoot("setprop ro.product.device \"${config.productDevice}\"")
                RootEngine.executeRoot("setprop ro.build.fingerprint \"${config.fingerprint}\"")
            }

            // 2. Cycle Airplane mode (2.0s on, 2.5s off) to acquire fresh dynamic IP
            val ipResult = NetworkIpRotator.cycleAirplaneModeAndFetchNewIp(
                context = getApplication(),
                isRootMode = isRoot
            ) { progressMsg ->
                _actionStatusMessage.value = progressMsg
            }

            // 3. Graceful profile restart (without whole device reboot)
            _actionStatusMessage.value = "⚡ Menjalankan Graceful Profile Restart (2.5s delay)..."
            if (isRoot) {
                // Graceful profile stop & restart
                RootEngine.executeRoot("am stop-user -f ${profile.profileId.filter { it.isDigit() }.ifEmpty { "10" }}")
                delay(1500)
                RootEngine.executeRoot("am start-user ${profile.profileId.filter { it.isDigit() }.ifEmpty { "10" }}")
            } else {
                delay(1500)
            }

            _isExecutingAction.value = false
            _actionStatusMessage.value = ""

            // 4. Trigger High-Contrast Neon Green IP Fresh popup
            _lastIpRotationResult.value = ipResult
            showToast("✓ Sinkronisasi & Pergantian IP Selesai")
        }
    }

    // --- RESTART PROFIL INI (Graceful cooldown + IP rotation) ---
    fun restartProfileWithIpCycle() {
        viewModelScope.launch {
            _isExecutingAction.value = true
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            val isRoot = _rootStatus.value.isGranted

            _actionStatusMessage.value = "⚡ Menyiapkan Graceful Restart Profil '${profile.profileName}'..."
            delay(1000)

            // Airplane mode cycle for BTS release & dynamic carrier IP rotation
            val ipResult = NetworkIpRotator.cycleAirplaneModeAndFetchNewIp(
                context = getApplication(),
                isRootMode = isRoot
            ) { progressMsg ->
                _actionStatusMessage.value = progressMsg
            }

            _actionStatusMessage.value = "🔄 Mengaktifkan kembali sesi profil sandbox..."
            if (isRoot) {
                RootEngine.executeRoot("am stop-user -f ${profile.profileId.filter { it.isDigit() }.ifEmpty { "10" }}")
                delay(1200)
                RootEngine.executeRoot("am start-user ${profile.profileId.filter { it.isDigit() }.ifEmpty { "10" }}")
            } else {
                delay(1200)
            }

            _isExecutingAction.value = false
            _actionStatusMessage.value = ""

            // Trigger High-Contrast Neon Green IP Fresh popup
            _lastIpRotationResult.value = ipResult
            showToast("✓ Profil '${profile.profileName}' berhasil direstart dengan IP baru!")
        }
    }

    fun dismissIpFreshDialog() {
        _lastIpRotationResult.value = null
    }

    // --- PLAY ENGINE DIRECT INSTALL ---
    fun handlePlayAppInstalled(app: PlayAppItem) {
        viewModelScope.launch {
            val profile = currentProfile.value ?: repository.getCurrentProfile()
            repository.cloneAppToProfile(app.packageName, profile.profileId, app.appName)
            showToast("✓ Aplikasi ${app.appName} aktif di Sandbox ${profile.profileName}!")
        }
    }

    // --- UNIVERSAL RESTORE (.CAPSULE) ---
    fun restoreUniversalPackage(data: MigrationParsedData, isFullRootRestore: Boolean) {
        viewModelScope.launch {
            val success = repository.restoreUniversalMigrationPackage(data, isFullRootRestore)
            if (success) {
                val modeText = if (isFullRootRestore) "Full Root (Termasuk Sesi Akun)" else "Standar Universal"
                showToast("✓ Berhasil memulihkan paket cadangan ($modeText)!")
            } else {
                showToast("⚠️ Gagal memulihkan paket cadangan. Format tidak valid.")
            }
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

