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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CapsuleTab
import com.example.ui.CapsuleViewModel
import com.example.ui.components.AppDetailSheet
import com.example.ui.components.CapsuleHeader
import com.example.ui.components.CapsuleNavigationBar
import com.example.ui.components.PrivacyOpsDialog
import com.example.ui.screens.CapsuleScreen
import com.example.ui.screens.GlacierScreen
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
    val selectedAppForDetail by viewModel.selectedAppForDetail.collectAsStateWithLifecycle()
    val selectedAppForOps by viewModel.selectedAppForOps.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

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
                savedRamMb = savedRamTotalMb
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
                        onNavigateToMainland = { viewModel.selectTab(CapsuleTab.MAINLAND) }
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
                        onShowToast = { viewModel.showToast(it) }
                    )
                }

                CapsuleTab.SETTINGS_LOGS -> {
                    SettingsLogsScreen(
                        logs = logs,
                        onClearLogs = { viewModel.clearLogs() },
                        onDestroyCapsule = { viewModel.destroyCapsule() }
                    )
                }
            }
        }
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
}
