package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.AutoFreezeAmber
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.CapsuleCyanLight
import com.example.ui.theme.CapsuleTealContainer
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlacierBlue
import com.example.ui.theme.SandboxGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailSheet(
    app: AppItem,
    onDismiss: () -> Unit,
    onCloneToCapsule: (AppItem) -> Unit,
    onRemoveFromCapsule: (AppItem) -> Unit,
    onFreezeToggle: (AppItem) -> Unit,
    onAutoFreezeChange: (AppItem, Boolean, Int) -> Unit,
    onLaunchMainland: () -> Unit,
    onLaunchCapsule: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenOps: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var isAutoFreeze by remember(app) { mutableStateOf(app.isAutoFreeze) }
    var selectedDelay by remember(app) { mutableStateOf(app.autoFreezeDelaySeconds) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 10.dp),
                color = DarkBorder,
                shape = RoundedCornerShape(2.dp)
            ) {
                Box(modifier = Modifier.size(width = 40.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: App Monogram/Icon & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconImage(
                    drawable = app.icon,
                    appName = app.appName,
                    size = 56.dp,
                    shapeRadius = 14.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Versi ${app.versionName} • ~${app.estimatedRamMb} MB Memory",
                        fontSize = 11.sp,
                        color = CapsuleCyanLight
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dual Launch Options Section
            Text(
                text = "PELUNCURAN PARALEL (DUAL PROFILE)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CapsuleCyan,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (app.isCloned) {
                        Button(
                            onClick = onLaunchCapsule,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CapsuleCyan,
                                contentColor = Color(0xFF090D16)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buka di Ruang Isolasi Capsule (Akun 2)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Button(
                            onClick = { onCloneToCapsule(app) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CapsuleCyan,
                                contentColor = Color(0xFF090D16)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kloning ke Ruang Isolasi Capsule",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = onLaunchMainland,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimaryDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buka di Profil Utama (Mainland)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (app.isCloned) {
                Spacer(modifier = Modifier.height(18.dp))

                // App Freezing & Hibernation Card
                Text(
                    text = "KONTROL PEMBEKUAN (HIBERNATION ENGINE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlacierBlue,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlacierBlue.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Instant Freeze toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AcUnit,
                                    contentDescription = null,
                                    tint = GlacierBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (app.isFrozen) "Status: Sedang Dibekukan ❄️" else "Status: Aktif Berjalan ⚡",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (app.isFrozen) GlacierBlue else SandboxGreen
                                    )
                                    Text(
                                        text = if (app.isFrozen) "Aplikasi tidak menguras baterai atau data" else "Aplikasi dapat menerima notifikasi/latar",
                                        fontSize = 11.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                            }

                            Switch(
                                checked = app.isFrozen,
                                onCheckedChange = { onFreezeToggle(app) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF090D16),
                                    checkedTrackColor = GlacierBlue,
                                    uncheckedThumbColor = TextSecondaryDark,
                                    uncheckedTrackColor = DarkSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.material3.HorizontalDivider(color = DarkBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Auto-Freeze on exit toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LockClock,
                                    contentDescription = null,
                                    tint = AutoFreezeAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Auto-Freeze Otomatis",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                    Text(
                                        text = "Bekukan aplikasi otomatis setelah selesai digunakan",
                                        fontSize = 11.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                            }

                            Switch(
                                checked = isAutoFreeze,
                                onCheckedChange = {
                                    isAutoFreeze = it
                                    onAutoFreezeChange(app, it, selectedDelay)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF090D16),
                                    checkedTrackColor = AutoFreezeAmber,
                                    uncheckedThumbColor = TextSecondaryDark,
                                    uncheckedTrackColor = DarkSurfaceVariant
                                )
                            )
                        }

                        // Delay selector
                        if (isAutoFreeze) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Jeda Waktu Auto-Freeze:",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val delays = listOf(0 to "Segera", 15 to "15s", 30 to "30s", 60 to "1m", 300 to "5m")
                                delays.forEach { (sec, label) ->
                                    val isSelected = selectedDelay == sec
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedDelay = sec
                                                onAutoFreezeChange(app, true, sec)
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) AutoFreezeAmber else DarkSurfaceVariant,
                                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF090D16) else TextPrimaryDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Ops & Privacy & Tools
            Text(
                text = "ALAT & PENGATURAN ISOLASI",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    if (app.isCloned) {
                        // Privacy Ops
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenOps() }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CapsuleCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Privacy Ops & Izin Sandbox",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "Batasi izin Lokasi, Kontak, Kamera, & Data Latar Belakang",
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark
                                )
                            }
                        }

                        androidx.compose.material3.HorizontalDivider(color = DarkBorder)
                    }

                    // Copy Package Name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                clipboardManager.setText(AnnotatedString(app.packageName))
                                Toast.makeText(context, "Package name disalin: ${app.packageName}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = CapsuleCyanLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Salin Nama Paket (${app.packageName})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimaryDark
                        )
                    }

                    androidx.compose.material3.HorizontalDivider(color = DarkBorder)

                    // Open Android App Details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSettings() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Buka Info Aplikasi Sistem Android",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimaryDark
                        )
                    }

                    if (app.isCloned) {
                        androidx.compose.material3.HorizontalDivider(color = DarkBorder)

                        // Remove / Delete from Capsule
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRemoveFromCapsule(app) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Hapus Kloning dari Capsule",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF5350)
                            )
                        }
                    }
                }
            }
        }
    }
}
