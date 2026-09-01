package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.CapsulePolicyManager
import com.example.ui.WorkingEngineMode
import com.example.ui.theme.AutoFreezeAmber
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.CapsuleCyanLight
import com.example.ui.theme.CapsuleTealContainer
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlacierBlue
import com.example.ui.theme.SandboxGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.util.LanguageManager
import com.example.util.RootStatus

@Composable
fun ShuttleAdbScreen(
    rootStatus: RootStatus,
    workingEngineMode: WorkingEngineMode,
    onRequestRoot: () -> Unit,
    onSetupWorkProfileRoot: () -> Unit,
    onTestRoot: () -> Unit,
    onSelectEngineMode: (WorkingEngineMode) -> Unit,
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isAdminActive = remember { CapsulePolicyManager.isDeviceAdminActive(context) }
    val isProfileOwner = remember { CapsulePolicyManager.isProfileOwner(context) }

    var showInfoDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. ROOT ENGINE & SUPERUSER CONTROLLER (NEW)
        // ==========================================
        item {
            Text(
                text = LanguageManager.getString("sec_root_engine"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (rootStatus.isGranted) SandboxGreen else AutoFreezeAmber,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    if (rootStatus.isGranted) SandboxGreen.copy(alpha = 0.6f) else AutoFreezeAmber.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (rootStatus.isGranted) Color(0xFF00381B) else Color(0xFF3E2723),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (rootStatus.isGranted) SandboxGreen else AutoFreezeAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = LanguageManager.getString("root_card_title"),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = if (rootStatus.isGranted)
                                        "Magisk / KernelSU / APatch (${rootStatus.uid})"
                                    else if (rootStatus.hasSuBinary)
                                        "Binary SU Terdeteksi (Belum Diizinkan)"
                                    else
                                        "Non-Root Device Mode",
                                    fontSize = 11.sp,
                                    color = if (rootStatus.isGranted) SandboxGreen else TextSecondaryDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (rootStatus.isGranted) Color(0xFF072115) else Color(0xFF1B1817),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (rootStatus.isGranted) SandboxGreen.copy(alpha = 0.4f) else DarkBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (rootStatus.isGranted)
                                "✓ ${LanguageManager.getString("root_status_granted")} (${rootStatus.suVersion})"
                            else if (rootStatus.hasSuBinary)
                                "⚠️ ${LanguageManager.getString("root_status_detected")}"
                            else
                                "ℹ️ ${LanguageManager.getString("root_status_none")}",
                            fontSize = 11.sp,
                            color = if (rootStatus.isGranted) SandboxGreen else AutoFreezeAmber,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Root Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Request Root / Grant Button
                        Button(
                            onClick = { onRequestRoot() },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rootStatus.isGranted) SandboxGreen else AutoFreezeAmber,
                                contentColor = Color(0xFF090D16)
                            )
                        ) {
                            Icon(
                                imageVector = if (rootStatus.isGranted) Icons.Default.CheckCircle else Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (rootStatus.isGranted) "Root Aktif ✓" else "Minta Izin Root",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 1-Click Root Work Profile Creator
                        Button(
                            onClick = { onSetupWorkProfileRoot() },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CapsuleCyan,
                                contentColor = Color(0xFF090D16)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1-Klik Setup via Root",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Test Root Command Button
                    OutlinedButton(
                        onClick = { onTestRoot() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimaryDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = CapsuleCyanLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🧪 Tes Eksekusi Perintah Root Shell (su)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. WORKING ENGINE SELECTOR (NEW)
        // ==========================================
        item {
            Text(
                text = LanguageManager.getString("sec_engine_mode"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CapsuleCyan,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkingEngineMode.values().forEach { mode ->
                        val isSelected = workingEngineMode == mode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectEngineMode(mode) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CapsuleCyan.copy(alpha = 0.15f) else DarkSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.2.dp,
                                if (isSelected) CapsuleCyan else DarkBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (isSelected) CapsuleCyan else TextSecondaryDark.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = mode.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) CapsuleCyanLight else TextPrimaryDark
                                    )
                                    Text(
                                        text = mode.description,
                                        fontSize = 10.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. FILE SHUTTLE BRIDGE SECTION
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, CapsuleCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CapsuleTealContainer, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = CapsuleCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "File Shuttle (Cross-Profile Bridge)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Transfer foto, dokumen & media antar ruang",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "File Shuttle memungkinkan Anda mentransfer file antara profil utama (Mainland) dan profil terisolasi (Capsule) secara aman tanpa membocorkan privasi penyimpanan.",
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "*/*"
                                    putExtra(Intent.EXTRA_TEXT, "Shuttle data dari CapsulePro")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Kirim File via Shuttle"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CapsuleCyan,
                                contentColor = Color(0xFF090D16)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kirim ke Capsule",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onShowToast("File Shuttle siap menerima berkas dari ruang isolasi")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextPrimaryDark
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = CapsuleCyanLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Buka Shuttle Hub",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. STATUS SISTEM & PROVISIONING
        // ==========================================
        item {
            Text(
                text = "STATUS SISTEM & MODE PROVISIONING",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CapsuleCyan,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    StatusRow(
                        title = "Root Engine Privilege (su)",
                        status = if (rootStatus.isGranted) "Root Aktif (uid=0) ✓" else if (rootStatus.hasSuBinary) "SU Tersedia" else "Non-Root",
                        isActive = rootStatus.isGranted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StatusRow(
                        title = "Sandboxing Dual-Space Engine",
                        status = "Aktif & Terisolasi",
                        isActive = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StatusRow(
                        title = "Device Administrator",
                        status = if (isAdminActive) "Diberikan ✓" else "Opsional (Tersedia)",
                        isActive = isAdminActive
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StatusRow(
                        title = "Managed Profile Owner (DPM)",
                        status = if (isProfileOwner) "Profile Owner Aktif" else "Mode Sandboxing Mandiri",
                        isActive = isProfileOwner
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val intent = CapsulePolicyManager.createAddDeviceAdminIntent(context)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    onShowToast("Tidak dapat membuka pengaturan Device Admin: ${e.localizedMessage}")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAdminActive) CapsuleTealContainer else CapsuleCyan,
                                contentColor = if (isAdminActive) CapsuleCyan else Color(0xFF090D16)
                            ),
                            border = if (isAdminActive) androidx.compose.foundation.BorderStroke(1.dp, CapsuleCyan.copy(alpha = 0.5f)) else null
                        ) {
                            Icon(
                                imageVector = if (isAdminActive) Icons.Default.CheckCircle else Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAdminActive) "Admin Aktif ✓" else "Izin Admin",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                showInfoDialog = true
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CapsuleCyanLight
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CapsuleCyan.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CapsuleCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Panduan Setup",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. PERINTAH ROOT, ADB & SHIZUKU (1-TAP SALIN)
        // ==========================================
        item {
            Text(
                text = "PERINTAH ROOT & PRIVILEGED SHELL (1-TAP SALIN)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SandboxGreen,
                letterSpacing = 0.5.sp
            )
        }

        item {
            CommandCard(
                title = "Perintah Root Freeze & Force-Stop",
                subtitle = "Jalankan melalui Terminal Root (Termux su / KernelSU) untuk membekukan paket",
                command = CapsulePolicyManager.getRootFreezeCommand("com.target.package"),
                onCopy = {
                    clipboardManager.setText(AnnotatedString(it))
                    onShowToast("Perintah Root Freeze disalin!")
                }
            )
        }

        item {
            CommandCard(
                title = "Perintah Root Buat Work Profile Capsule",
                subtitle = "Membuat profil terisolasi langsung via root tanpa ADB komputer",
                command = CapsulePolicyManager.getRootCreateProfileCommand(context.packageName),
                onCopy = {
                    clipboardManager.setText(AnnotatedString(it))
                    onShowToast("Perintah Root Profile disalin!")
                }
            )
        }

        item {
            Text(
                text = "PERINTAH ADB & SHIZUKU (NON-ROOT)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GlacierBlue,
                letterSpacing = 0.5.sp
            )
        }

        item {
            CommandCard(
                title = "Set CapsulePro sebagai Work Profile Owner",
                subtitle = "Jalankan melalui Terminal ADB di Komputer atau Shizuku / Wireless Debugging",
                command = CapsulePolicyManager.getAdbCommandForProfileOwner(context.packageName),
                onCopy = {
                    clipboardManager.setText(AnnotatedString(it))
                    onShowToast("Perintah DPM disalin ke clipboard!")
                }
            )
        }

        item {
            CommandCard(
                title = "Perintah ADB Freeze / Suspend Manual",
                subtitle = "Bekukan paket target secara instan via ADB",
                command = CapsulePolicyManager.getAdbFreezeCommand("com.target.package"),
                onCopy = {
                    clipboardManager.setText(AnnotatedString(it))
                    onShowToast("Perintah ADB suspend disalin!")
                }
            )
        }

        item {
            CommandCard(
                title = "Perintah Shizuku / User-Space Disable",
                subtitle = "Nonaktifkan paket tanpa izin root menggunakan Shizuku API",
                command = CapsulePolicyManager.getShizukuFreezeCommand("com.target.package"),
                onCopy = {
                    clipboardManager.setText(AnnotatedString(it))
                    onShowToast("Perintah Shizuku disalin!")
                }
            )
        }
    }

    // Work Profile Setup Info & Launcher Dialog
    if (showInfoDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CapsuleCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Panduan Mode Engine CapsulePro",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Pilihan Metode Setup:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CapsuleCyanLight
                    )
                    Text(
                        text = "1. Mode Root: Jika HP Anda sudah di-root (Magisk/KernelSU), cukup klik tombol '1-Klik Setup via Root'.\n2. Mode Shizuku / ADB: Untuk HP tanpa root dengan Wireless Debugging.\n3. Mode Sandboxing Mandiri: Berfungsi langsung 100% tanpa izin khusus.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        lineHeight = 16.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF162235),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CapsuleCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "💡 Rekomendasi:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CapsuleCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Jika HP Anda memiliki Root, gunakan 'Minta Izin Root' di bagian paling atas untuk eksekusi paling cepat dan tanpa batas.",
                                fontSize = 11.sp,
                                color = TextPrimaryDark,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInfoDialog = false
                        try {
                            val intent = CapsulePolicyManager.createProvisioningIntent(context)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            onShowToast("Setup wizard ditolak oleh sistem ROM: ${e.localizedMessage}")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CapsuleCyan,
                        contentColor = Color(0xFF090D16)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Buka Wizard Android", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showInfoDialog = false }
                ) {
                    Text("Tutup", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun StatusRow(
    title: String,
    status: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = TextPrimaryDark,
            fontWeight = FontWeight.Medium
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isActive) Color(0xFF00381B) else DarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
                0.8.dp,
                if (isActive) SandboxGreen else DarkBorder
            )
        ) {
            Text(
                text = status,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) SandboxGreen else TextSecondaryDark,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun CommandCard(
    title: String,
    subtitle: String,
    command: String,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                }

                Surface(
                    modifier = Modifier.clickable { onCopy(command) },
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = CapsuleCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Salin",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CapsuleCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF070B14),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Text(
                    text = command,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CapsuleCyanLight,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
