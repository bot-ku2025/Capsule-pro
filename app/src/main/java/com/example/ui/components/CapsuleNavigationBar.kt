package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CapsuleTab
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlacierBlue
import com.example.ui.theme.TextSecondaryDark
import com.example.util.LanguageManager

@Composable
fun CapsuleNavigationBar(
    currentTab: CapsuleTab,
    onTabSelected: (CapsuleTab) -> Unit,
    clonedCount: Int,
    frozenCount: Int,
    modifier: Modifier = Modifier
) {
    val currentLang by LanguageManager.currentLanguage.collectAsStateWithLifecycle()

    NavigationBar(
        modifier = modifier
            .background(DarkCanvas)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = DarkSurface,
        tonalElevation = 6.dp
    ) {
        // Mainland Tab
        NavigationBarItem(
            selected = currentTab == CapsuleTab.MAINLAND,
            onClick = { onTabSelected(CapsuleTab.MAINLAND) },
            icon = {
                Icon(
                    imageVector = if (currentTab == CapsuleTab.MAINLAND) Icons.Filled.Apps else Icons.Outlined.Apps,
                    contentDescription = "Mainland",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = LanguageManager.getString("tab_mainland"),
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CapsuleTab.MAINLAND) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF090D16),
                selectedTextColor = CapsuleCyan,
                indicatorColor = CapsuleCyan,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            )
        )

        // Capsule (Sandbox) Tab
        NavigationBarItem(
            selected = currentTab == CapsuleTab.CAPSULE,
            onClick = { onTabSelected(CapsuleTab.CAPSULE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == CapsuleTab.CAPSULE) Icons.Filled.Security else Icons.Outlined.Security,
                    contentDescription = "Capsule",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "${LanguageManager.getString("tab_capsule")} ($clonedCount)",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CapsuleTab.CAPSULE) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF090D16),
                selectedTextColor = CapsuleCyan,
                indicatorColor = CapsuleCyan,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            )
        )

        // Identity (Device Spoofing) Tab
        NavigationBarItem(
            selected = currentTab == CapsuleTab.IDENTITY,
            onClick = { onTabSelected(CapsuleTab.IDENTITY) },
            icon = {
                Icon(
                    imageVector = if (currentTab == CapsuleTab.IDENTITY) Icons.Filled.Fingerprint else Icons.Outlined.Fingerprint,
                    contentDescription = "Identity",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = LanguageManager.getString("tab_identity"),
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CapsuleTab.IDENTITY) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF090D16),
                selectedTextColor = CapsuleCyan,
                indicatorColor = CapsuleCyan,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            )
        )

        // Glacier Freezer Tab
        NavigationBarItem(
            selected = currentTab == CapsuleTab.GLACIER,
            onClick = { onTabSelected(CapsuleTab.GLACIER) },
            icon = {
                Icon(
                    imageVector = if (currentTab == CapsuleTab.GLACIER) Icons.Filled.AcUnit else Icons.Outlined.AcUnit,
                    contentDescription = "Glacier Freezer",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "${LanguageManager.getString("tab_glacier")} ($frozenCount)",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CapsuleTab.GLACIER) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF090D16),
                selectedTextColor = GlacierBlue,
                indicatorColor = GlacierBlue,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            )
        )

        // Shuttle & ADB Tab
        NavigationBarItem(
            selected = currentTab == CapsuleTab.SHUTTLE_ADB,
            onClick = { onTabSelected(CapsuleTab.SHUTTLE_ADB) },
            icon = {
                Icon(
                    imageVector = if (currentTab == CapsuleTab.SHUTTLE_ADB) Icons.Filled.Terminal else Icons.Outlined.Terminal,
                    contentDescription = "Shuttle & ADB",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = LanguageManager.getString("tab_bridge"),
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CapsuleTab.SHUTTLE_ADB) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF090D16),
                selectedTextColor = CapsuleCyan,
                indicatorColor = CapsuleCyan,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            )
        )

        // Settings & Logs Tab
        NavigationBarItem(
            selected = currentTab == CapsuleTab.SETTINGS_LOGS,
            onClick = { onTabSelected(CapsuleTab.SETTINGS_LOGS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == CapsuleTab.SETTINGS_LOGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = LanguageManager.getString("tab_settings"),
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CapsuleTab.SETTINGS_LOGS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF090D16),
                selectedTextColor = CapsuleCyan,
                indicatorColor = CapsuleCyan,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            )
        )
    }
}
