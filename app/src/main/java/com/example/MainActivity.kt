package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CapsuleTab
import com.example.ui.CapsuleViewModel
import com.example.ui.components.AppDetailSheet
import com.example.ui.components.CapsuleHeader
import com.example.ui.components.CapsuleNavigationBar
import com.example.ui.components.FloatingAssistantDialog
import com.example.ui.components.IpFreshDialog
import com.example.ui.components.PlayEngineSheet
import com.example.ui.components.PrivacyOpsDialog
import com.example.ui.components.ProfileBackupDialog
import com.example.ui.components.UniversalMigrationDialog
import com.example.ui.screens.CapsuleScreen
import com.example.ui.screens.GlacierScreen
import com.example.ui.screens.IdentityScreen
import com.example.ui.screens.MainlandScreen
import com.example.ui.screens.SettingsLogsScreen
import com.example.ui.screens.ShuttleAdbScreen
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CapsuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.util.LanguageManager.init(this)

        setContent {
            MyApplicationTheme {
                CapsuleProApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CapsuleProApp(viewModel: CapsuleViewModel) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val mainlandFilter by viewModel.mainlandFilter.collectAsStateWithLifecycle()
    val capsuleFilter by viewModel.capsuleFilter.collectAsStateWithLifecycle()
    val filteredMainlandApps by viewModel.filteredMainlandApps.collectAsStateWithLifecycle()
    val filteredCapsuleApps by viewModel.filteredCapsuleApps.collectAsStateWithLifecycle()
    val capsuleApps by viewModel.capsuleApps.collectAsStateWithLifecycle()
    val frozenCount by viewModel.frozenCount.collectAsStateWithLifecycle()
    val savedRamTotalMb by viewModel.savedRamTotalMb.collectAsStateWithLifecycle()
    val screenOffAutoFreeze by viewModel.screenOffAutoFreeze.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val rootStatus by viewModel.rootStatus.collectAsStateWithLifecycle()
    val workingEngineMode by viewModel.workingEngineMode.collectAsStateWithLifecycle()
    val selectedAppForDetail by viewModel.selectedAppForDetail.collectAsStateWithLifecycle()
    val selectedAppForOps by viewModel.selectedAppForOps.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    // Profiles & Snapshots
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val currentProfile by viewModel.currentProfile.collectAsStateWithLifecycle()
    val currentProfileSnapshots by viewModel.currentProfileSnapshots.collectAsStateWithLifecycle()
    val allCapsuleAppEntities by viewModel.allCapsuleAppEntities.collectAsStateWithLifecycle()
    val allSnapshots by viewModel.allSnapshots.collectAsStateWithLifecycle()
    val allIdentities by viewModel.allIdentities.collectAsStateWithLifecycle()

    // Identity & IP Rotation
    val identityConfig by viewModel.identityConfig.collectAsStateWithLifecycle()
    val lastIpRotationResult by viewModel.lastIpRotationResult.collectAsStateWithLifecycle()
    val isExecutingAction by viewModel.isExecutingAction.collectAsStateWithLifecycle()
    val actionStatusMessage by viewModel.actionStatusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showFloatingDialog by remember { mutableStateOf(false) }
    var showProfileBackupDialog by remember { mutableStateOf(false) }
    var showPlayEngineSheet by remember { mutableStateOf(false) }
    var showUniversalMigrationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CapsuleHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                totalCloned = capsuleApps.size,
                totalFrozen = frozenCount,
                savedRamMb = savedRamTotalMb,
                currentProfile = currentProfile,
                onOpenProfileDialog = { showProfileBackupDialog = true },
                onOpenFloatingAssistant = { showFloatingDialog = true },
                onOpenPlayEngine = { showPlayEngineSheet = true },
                onOpenUniversalMigration = { showUniversalMigrationDialog = true }
            )
        },
        bottomBar = {
            CapsuleNavigationBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) },
                clonedCount = capsuleApps.size,
                frozenCount = frozenCount
            )
        },
        containerColor = DarkCanvas
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                CapsuleTab.MAINLAND -> {
                    MainlandScreen(
                        apps = filteredMainlandApps,
                        currentFilter = mainlandFilter,
                        onFilterSelect = { viewModel.setMainlandFilter(it) },
                        onAppClick = { viewModel.selectAppForDetail(it) },
                        onCloneClick = { viewModel.cloneToCapsule(it) },
                        onLaunchClick = { viewModel.launchApp(it, inCapsule = false) }
                    )
                }

                CapsuleTab.CAPSULE -> {
                    CapsuleScreen(
                        apps = filteredCapsuleApps,
                        currentFilter = capsuleFilter,
                        onFilterSelect = { viewModel.setCapsuleFilter(it) },
                        onAppClick = { viewModel.selectAppForDetail(it) },
                        onLaunchClick = { viewModel.launchApp(it, inCapsule = true) },
                        onFreezeToggle = { app ->
                            if (app.isFrozen) viewModel.defrostApp(app) else viewModel.freezeApp(app)
                        },
                        onOpsClick = { viewModel.selectAppForOps(it) },
                        onFreezeAll = { viewModel.freezeAllInactive() },
                        onDefrostAll = { viewModel.defrostAll() },
                        onOpenTools = { showFloatingDialog = true },
                        onNavigateToMainland = { viewModel.selectTab(CapsuleTab.MAINLAND) }
                    )
                }

                CapsuleTab.IDENTITY -> {
                    IdentityScreen(
                        currentProfile = currentProfile,
                        allProfiles = allProfiles,
                        identityConfig = identityConfig,
                        rootStatus = rootStatus,
                        isExecutingAction = isExecutingAction,
                        actionStatusMessage = actionStatusMessage,
                        onSelectProfile = { viewModel.switchProfile(it) },
                        onRandomizeIdentity = { viewModel.randomizeCurrentProfileIdentity() },
                        onApplyPreset = { preset -> viewModel.applyDevicePreset(preset) },
                        onUpdateField = { updated -> viewModel.updateIdentityConfig(updated) },
                        onSynchronizeSystem = { viewModel.synchronizeSystem() },
                        onRestartProfileWithIpCycle = { viewModel.restartProfileWithIpCycle() },
                        onRequestRootAccess = { viewModel.requestRootAccess() }
                    )
                }

                CapsuleTab.GLACIER -> {
                    GlacierScreen(
                        capsuleApps = capsuleApps,
                        frozenCount = frozenCount,
                        savedRamMb = savedRamTotalMb,
                        screenOffAutoFreeze = screenOffAutoFreeze,
                        onToggleScreenOffFreeze = { viewModel.toggleScreenOffAutoFreeze() },
                        onFreezeAll = { viewModel.freezeAllInactive() },
                        onDefrostAll = { viewModel.defrostAll() },
                        onFreezeApp = { viewModel.freezeApp(it) },
                        onDefrostApp = { viewModel.defrostApp(it) },
                        onAppClick = { viewModel.selectAppForDetail(it) }
                    )
                }

                CapsuleTab.SHUTTLE_ADB -> {
                    ShuttleAdbScreen(
                        rootStatus = rootStatus,
                        workingEngineMode = workingEngineMode,
                        onRequestRoot = { viewModel.requestRootAccess() },
                        onSetupWorkProfileRoot = { viewModel.setupWorkProfileViaRoot() },
                        onTestRoot = { viewModel.testRootCommand() },
                        onSelectEngineMode = { viewModel.setEngineMode(it) },
                        onShowToast = { viewModel.showToast(it) }
                    )
                }

                CapsuleTab.SETTINGS_LOGS -> {
                    SettingsLogsScreen(
                        logs = logs,
                        workingEngineMode = workingEngineMode,
                        rootStatus = rootStatus,
                        onSelectEngineMode = { viewModel.setEngineMode(it) },
                        onOpenProfileBackup = { showProfileBackupDialog = true },
                        onClearLogs = { viewModel.clearLogs() },
                        onDestroyCapsule = { viewModel.destroyCapsule() }
                    )
                }
            }
        }
    }

    // Profile & Snapshot Backup Dialog
    if (showProfileBackupDialog) {
        ProfileBackupDialog(
            viewModel = viewModel,
            profiles = allProfiles,
            currentProfile = currentProfile,
            snapshots = currentProfileSnapshots,
            onDismiss = { showProfileBackupDialog = false }
        )
    }

    // High-Contrast Bright Neon Green IP Fresh Dialog
    lastIpRotationResult?.let { result ->
        IpFreshDialog(
            result = result,
            onDismiss = { viewModel.dismissIpFreshDialog() }
        )
    }

    // Play Backend Engine Bottom Sheet
    if (showPlayEngineSheet) {
        PlayEngineSheet(
            currentProfile = currentProfile,
            onAppInstalled = { app ->
                viewModel.handlePlayAppInstalled(app)
            },
            onDismiss = { showPlayEngineSheet = false }
        )
    }

    // Universal Migration Dialog (.capsule)
    if (showUniversalMigrationDialog) {
        UniversalMigrationDialog(
            profiles = allProfiles,
            capsuleApps = allCapsuleAppEntities,
            snapshots = allSnapshots,
            identities = allIdentities,
            rootStatus = rootStatus,
            onExecuteRestore = { parsedData, isFullRoot ->
                viewModel.restoreUniversalPackage(parsedData, isFullRoot)
            },
            onRequestRoot = { viewModel.requestRootAccess() },
            onDismiss = { showUniversalMigrationDialog = false }
        )
    }

    // Detail Bottom Sheet
    selectedAppForDetail?.let { app ->
        AppDetailSheet(
            app = app,
            onDismiss = { viewModel.selectAppForDetail(null) },
            onCloneToCapsule = { viewModel.cloneToCapsule(it) },
            onRemoveFromCapsule = { viewModel.removeFromCapsule(it) },
            onFreezeToggle = {
                if (it.isFrozen) viewModel.defrostApp(it) else viewModel.freezeApp(it)
            },
            onAutoFreezeChange = { targetApp, enabled, delay ->
                viewModel.updateAutoFreezeConfig(targetApp, enabled, delay)
            },
            onLaunchMainland = {
                viewModel.launchApp(app, inCapsule = false)
            },
            onLaunchCapsule = {
                viewModel.launchApp(app, inCapsule = true)
            },
            onOpenSettings = {
                viewModel.openAppSettings(app.packageName)
            },
            onOpenOps = {
                viewModel.selectAppForOps(app)
            }
        )
    }

    // Privacy Ops Dialog
    selectedAppForOps?.let { app ->
        PrivacyOpsDialog(
            app = app,
            onDismiss = { viewModel.selectAppForOps(null) },
            onSaveOps = { blockLocation, blockContacts, blockCamera, blockNetwork, isolatedStorage ->
                viewModel.updatePrivacyOps(
                    app = app,
                    blockLocation = blockLocation,
                    blockContacts = blockContacts,
                    blockCamera = blockCamera,
                    blockBackgroundNetwork = blockNetwork,
                    isolatedStorage = isolatedStorage
                )
            }
        )
    }

    // Floating Assistant Dialog
    if (showFloatingDialog) {
        FloatingAssistantDialog(
            onDismiss = { showFloatingDialog = false }
        )
    }
}
