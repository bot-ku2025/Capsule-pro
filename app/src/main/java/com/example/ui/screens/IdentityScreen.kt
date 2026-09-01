package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.CapsuleProfileEntity
import com.example.data.local.entity.IdentityConfigEntity
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.GlacierBlue
import com.example.ui.theme.SandboxGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.util.DeviceIdentityGenerator
import com.example.util.DevicePreset
import com.example.util.RootStatus

@Composable
fun IdentityScreen(
    currentProfile: CapsuleProfileEntity?,
    allProfiles: List<CapsuleProfileEntity>,
    identityConfig: IdentityConfigEntity?,
    rootStatus: RootStatus,
    isExecutingAction: Boolean,
    actionStatusMessage: String,
    onSelectProfile: (String) -> Unit,
    onRandomizeIdentity: () -> Unit,
    onApplyPreset: (DevicePreset) -> Unit,
    onUpdateField: (IdentityConfigEntity) -> Unit,
    onSynchronizeSystem: () -> Unit,
    onRestartProfileWithIpCycle: () -> Unit,
    onRequestRootAccess: () -> Unit
) {
    val isRoot = rootStatus.isGranted
    var editingFieldDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showPresetMenu by remember { mutableStateOf(false) }
    var showProfileDropdown by remember { mutableStateOf(false) }

    val config = identityConfig ?: DeviceIdentityGenerator.createFreshBlankIdentity(currentProfile?.profileId ?: "profile_1")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Title & Subtitle
        item {
            Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Identity",
                            color = TextPrimaryDark,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure device spoofing & anti-fingerprint",
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                    }

                    // Preset Selector Button (When in Root mode)
                    if (isRoot) {
                        Box {
                            OutlinedButton(
                                onClick = { showPresetMenu = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CapsuleCyan
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = showPresetMenu,
                                onDismissRequest = { showPresetMenu = false },
                                modifier = Modifier.background(DarkSurfaceCard)
                            ) {
                                DeviceIdentityGenerator.PRESETS.forEach { preset ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(preset.model, color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${preset.brand} • ${preset.boardPlatform}", color = TextSecondaryDark, fontSize = 11.sp)
                                            }
                                        },
                                        onClick = {
                                            onApplyPreset(preset)
                                            showPresetMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Active Profile Selector Card & Status Badge
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showProfileDropdown = true },
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(currentProfile?.colorHex ?: 0xFF00E5FF).copy(alpha = 0.2f))
                                .border(1.dp, Color(currentProfile?.colorHex ?: 0xFF00E5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(currentProfile?.colorHex ?: 0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PROFILE: ${currentProfile?.profileName ?: "No Profile"}",
                                color = TextPrimaryDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (config.isFresh) "Keadaan Fresh / Belum Dikonfigurasi" else "Identitas Kustom Aktif",
                                color = if (config.isFresh) TextSecondaryDark else SandboxGreen,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRoot) SandboxGreen.copy(alpha = 0.2f) else DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isRoot) SandboxGreen else DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isRoot) "ROOT" else "NON-ROOT",
                                    color = if (isRoot) SandboxGreen else TextSecondaryDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showProfileDropdown,
                            onDismissRequest = { showProfileDropdown = false },
                            modifier = Modifier.background(DarkSurfaceCard)
                        ) {
                            allProfiles.forEach { profile ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = profile.profileName,
                                            color = if (profile.profileId == currentProfile?.profileId) CapsuleCyan else TextPrimaryDark,
                                            fontWeight = if (profile.profileId == currentProfile?.profileId) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSelectProfile(profile.profileId)
                                        showProfileDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Progress Banner (when Synchronize / Restart profile is running)
        if (isExecutingAction) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF072722)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = actionStatusMessage,
                                color = Color(0xFF00E676),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF00E676),
                                strokeWidth = 2.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF00E676),
                            trackColor = DarkSurface
                        )
                    }
                }
            }
        }

        // 3. Action Buttons (Synchronize & Restart Profile)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Synchronize System Button
                Button(
                    onClick = onSynchronizeSystem,
                    enabled = isRoot && !isExecutingAction,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceCard,
                        contentColor = CapsuleCyan,
                        disabledContainerColor = DarkSurface,
                        disabledContentColor = TextSecondaryDark
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Synchronize System",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Restart Profil Ini (Graceful cooldown & restart without rebooting device)
                Button(
                    onClick = onRestartProfileWithIpCycle,
                    enabled = isRoot && !isExecutingAction,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurfaceCard,
                        contentColor = Color(0xFF00E676),
                        disabledContainerColor = DarkSurface,
                        disabledContentColor = TextSecondaryDark
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Restart",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Restart Profil Ini",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Non-Root Mode Locked Notice Card (If not Root)
        if (!isRoot) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1428)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Fitur Khusus Mode Root (Superuser)",
                                color = Color(0xFFA78BFA),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Penyamaran identitas tingkat mendalam (IMEI, MAC Address, Model, Fingerprint OS) membutuhkan izin Root/Superuser untuk memodifikasi properti Zygote sistem Android.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRequestRootAccess,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Minta Izin Root / Buka Kunci", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. Section: DEVICE METADATA
        item {
            Text(
                text = "DEVICE METADATA",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    IdentityValueRow(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Brand",
                        value = config.brand,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Brand" to config.brand }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Model",
                        value = config.model,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Model" to config.model }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.PhoneAndroid,
                        title = "Android Version",
                        value = config.androidVersion,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Android Version" to config.androidVersion }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Speed,
                        title = "Tech Model / Codename",
                        value = "${config.codename} (${config.productDevice})",
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Codename" to config.codename }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Speed,
                        title = "Board / Platform",
                        value = config.boardPlatform,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Board / Platform" to config.boardPlatform }
                    )
                }
            }
        }

        // 6. Section: IDENTIFIERS (+ Randomize Button)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "IDENTIFIERS",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (isRoot) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRandomizeIdentity() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Randomize",
                            tint = CapsuleCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Randomize",
                            color = CapsuleCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    IdentityValueRow(
                        icon = Icons.Default.VpnKey,
                        title = "Android ID",
                        value = config.androidId,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Android ID" to config.androidId }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Numbers,
                        title = "IMEI 1",
                        value = config.imei1,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "IMEI 1" to config.imei1 }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Numbers,
                        title = "IMEI 2",
                        value = config.imei2,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "IMEI 2" to config.imei2 }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Key,
                        title = "Serial",
                        value = config.serialNumber,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Serial Number" to config.serialNumber }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Fingerprint,
                        title = "Fingerprint",
                        value = config.fingerprint,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Fingerprint" to config.fingerprint }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Cloud,
                        title = "GSF ID",
                        value = config.gsfId,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "GSF ID" to config.gsfId }
                    )
                }
            }
        }

        // 7. Section: NETWORK & CONNECTIVITY SPOOFING
        item {
            Text(
                text = "NETWORK & CONNECTIVITY",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    IdentityValueRow(
                        icon = Icons.Default.Wifi,
                        title = "WiFi MAC",
                        value = config.wifiMac,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "WiFi MAC" to config.wifiMac }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Wifi,
                        title = "Wi-Fi SSID",
                        value = config.wifiSsid,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Wi-Fi SSID" to config.wifiSsid }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Wifi,
                        title = "Wi-Fi BSSID",
                        value = config.wifiBssid,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Wi-Fi BSSID" to config.wifiBssid }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Bluetooth,
                        title = "Bluetooth MAC",
                        value = config.bluetoothMac,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Bluetooth MAC" to config.bluetoothMac }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Bluetooth,
                        title = "Nearby Bluetooth Name",
                        value = config.bluetoothName,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Bluetooth Name" to config.bluetoothName }
                    )
                }
            }
        }

        // 8. Section: TRACKING & APP IDS
        item {
            Text(
                text = "TRACKING & APP IDENTIFIERS",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    IdentityValueRow(
                        icon = Icons.Default.Public,
                        title = "Advertising ID (GAID)",
                        value = config.advertisingId,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Advertising ID" to config.advertisingId }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Key,
                        title = "App Set ID",
                        value = config.appSetId,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "App Set ID" to config.appSetId }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Key,
                        title = "Widevine DRM ID",
                        value = config.widevineDrmId,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Widevine DRM ID" to config.widevineDrmId }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Language,
                        title = "User Agent",
                        value = config.userAgent,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "User Agent" to config.userAgent }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.ShoppingBag,
                        title = "Installer Package",
                        value = config.installerPackage,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Installer Package" to config.installerPackage }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Keyboard,
                        title = "Hidden Keyboard Packages",
                        value = config.hiddenKeyboardPackages,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Hidden Keyboard Packages" to config.hiddenKeyboardPackages }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = DarkBorder.copy(alpha = 0.5f))

                    IdentityValueRow(
                        icon = Icons.Default.Keyboard,
                        title = "Virtual Default IME",
                        value = config.virtualDefaultIme,
                        isMonospace = true,
                        enabled = isRoot,
                        onEdit = { editingFieldDialog = "Virtual Default IME" to config.virtualDefaultIme }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Edit Field Dialog
    editingFieldDialog?.let { (fieldTitle, currentValue) ->
        var tempValue by remember { mutableStateOf(currentValue) }

        Dialog(onDismissRequest = { editingFieldDialog = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = DarkSurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Edit $fieldTitle",
                        color = TextPrimaryDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempValue,
                        onValueChange = { tempValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CapsuleCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { editingFieldDialog = null },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Batal", color = TextSecondaryDark)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val updated = when (fieldTitle) {
                                    "Brand" -> config.copy(brand = tempValue)
                                    "Model" -> config.copy(model = tempValue)
                                    "Android Version" -> config.copy(androidVersion = tempValue)
                                    "Codename" -> config.copy(codename = tempValue)
                                    "Board / Platform" -> config.copy(boardPlatform = tempValue)
                                    "Android ID" -> config.copy(androidId = tempValue)
                                    "IMEI 1" -> config.copy(imei1 = tempValue)
                                    "IMEI 2" -> config.copy(imei2 = tempValue)
                                    "Serial Number" -> config.copy(serialNumber = tempValue)
                                    "Fingerprint" -> config.copy(fingerprint = tempValue)
                                    "GSF ID" -> config.copy(gsfId = tempValue)
                                    "WiFi MAC" -> config.copy(wifiMac = tempValue)
                                    "Wi-Fi SSID" -> config.copy(wifiSsid = tempValue)
                                    "Wi-Fi BSSID" -> config.copy(wifiBssid = tempValue)
                                    "Bluetooth MAC" -> config.copy(bluetoothMac = tempValue)
                                    "Bluetooth Name" -> config.copy(bluetoothName = tempValue)
                                    "Advertising ID" -> config.copy(advertisingId = tempValue)
                                    "App Set ID" -> config.copy(appSetId = tempValue)
                                    "Widevine DRM ID" -> config.copy(widevineDrmId = tempValue)
                                    "User Agent" -> config.copy(userAgent = tempValue)
                                    "Installer Package" -> config.copy(installerPackage = tempValue)
                                    "Hidden Keyboard Packages" -> config.copy(hiddenKeyboardPackages = tempValue)
                                    "Virtual Default IME" -> config.copy(virtualDefaultIme = tempValue)
                                    else -> config
                                }.copy(isFresh = false)

                                onUpdateField(updated)
                                editingFieldDialog = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CapsuleCyan, contentColor = Color.Black)
                        ) {
                            Text("Simpan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdentityValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    isMonospace: Boolean = false,
    enabled: Boolean = true,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable { onEdit() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                .background(DarkSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) CapsuleCyan else TextSecondaryDark,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value.ifEmpty { "-" },
                    color = if (value == "-" || value.isEmpty()) TextSecondaryDark else TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = if (isMonospace) FontWeight.Normal else FontWeight.SemiBold,
                    fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                    maxLines = 2
                )
            }
        }

        if (enabled) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = TextSecondaryDark.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
