package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CapsuleLogEntity
import com.example.data.model.AppItem
import com.example.data.repository.CapsuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CapsuleTab {
    MAINLAND,      // Primary profile apps
    CAPSULE,       // Sandboxed / Cloned apps (Island equivalent)
    GLACIER,       // Deep freeze & auto-freeze manager (Greenify equivalent)
    SHUTTLE_ADB,   // File Shuttle & ADB/Shizuku/DPM commands
    SETTINGS_LOGS  // Audit logs, Privacy guard, & Settings
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

    // Raw sources from Repository
    val allInstalledApps: StateFlow<List<AppItem>> = repository.getInstalledAppsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val capsuleApps: StateFlow<List<AppItem>> = repository.getCapsuleAppsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<CapsuleLogEntity>> = repository.getLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
            repository.cloneAppToCapsule(app, tag)
            showToast("✓ ${app.appName} dikloning ke Capsule Sandbox")
        }
    }

    fun removeFromCapsule(app: AppItem) {
        viewModelScope.launch {
            repository.removeFromCapsule(app.packageName, app.appName)
            if (_selectedAppForDetail.value?.packageName == app.packageName) {
                _selectedAppForDetail.value = null
            }
            showToast("${app.appName} dihapus dari Capsule")
        }
    }

    fun freezeApp(app: AppItem) {
        viewModelScope.launch {
            repository.freezeApp(app.packageName, app.appName)
            showToast("❄️ ${app.appName} dibekukan (Deep Hibernated)")
        }
    }

    fun defrostApp(app: AppItem) {
        viewModelScope.launch {
            repository.defrostApp(app.packageName, app.appName)
            showToast("⚡ ${app.appName} dicairkan")
        }
    }

    fun freezeAllInactive() {
        viewModelScope.launch {
            repository.freezeAllCapsuleApps()
            showToast("❄️ Semua aplikasi Kapsul berhasil dibekukan!")
        }
    }

    fun defrostAll() {
        viewModelScope.launch {
            repository.defrostAllCapsuleApps()
            showToast("⚡ Semua aplikasi Kapsul telah dicairkan!")
        }
    }

    fun updateAutoFreezeConfig(app: AppItem, enabled: Boolean, delaySeconds: Int) {
        viewModelScope.launch {
            repository.updateAutoFreeze(app.packageName, app.appName, enabled, delaySeconds)
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
            repository.updateAppOps(
                packageName = app.packageName,
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
            repository.launchApp(app.packageName, app.appName, inCapsule)
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
            showToast("Ruang isolasi Capsule telah direset")
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
