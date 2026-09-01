package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.CapsuleAppEntity
import com.example.data.local.entity.CapsuleProfileEntity
import com.example.data.local.entity.CapsuleSnapshotEntity
import com.example.data.local.entity.IdentityConfigEntity
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.SandboxGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.util.MigrationManager
import com.example.util.MigrationParsedData
import com.example.util.MigrationSummary
import com.example.util.RootStatus
import kotlinx.coroutines.launch

@Composable
fun UniversalMigrationDialog(
    profiles: List<CapsuleProfileEntity>,
    capsuleApps: List<CapsuleAppEntity>,
    snapshots: List<CapsuleSnapshotEntity>,
    identities: List<IdentityConfigEntity>,
    rootStatus: RootStatus,
    onExecuteRestore: (MigrationParsedData, Boolean) -> Unit, // data, isFullRootRestore
    onRequestRoot: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Export .capsule, 1: Restore .capsule

    // Export state
    var selectedBackupType by remember { mutableStateOf("STANDARD") } // "STANDARD" or "FULL_ROOT"
    var isGeneratingExport by remember { mutableStateOf(false) }
    var generatedPackageJson by remember { mutableStateOf<String?>(null) }

    // Restore state
    var importJsonInput by remember { mutableStateOf("") }
    var inspectingSummary by remember { mutableStateOf<MigrationSummary?>(null) }
    var parsedMigrationData by remember { mutableStateOf<MigrationParsedData?>(null) }
    var isInspecting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp)),
            color = DarkCanvas
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CapsuleCyan.copy(alpha = 0.15f))
                                .border(1.dp, CapsuleCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhonelinkSetup,
                                contentDescription = "Universal Migration",
                                tint = CapsuleCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Migrasi & Cadangan Menyeluruh",
                                color = TextPrimaryDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Paket .capsule • Ganti HP / Reset Pabrik",
                                color = CapsuleCyan,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Ekspor .capsule vs Pulihkan
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurface,
                    contentColor = CapsuleCyan,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CapsuleCyan,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "📦 Buat Paket Cadangan (.capsule)",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "📂 Pulihkan dari Berkas",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                if (selectedTab == 0) {
                    // TAB 0: EXPORT .CAPSULE
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "PILIH MODE CADANGAN:",
                                color = TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Card 1: Standard Mode (Non-Root / Universal)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = if (selectedBackupType == "STANDARD") 1.5.dp else 1.dp,
                                        color = if (selectedBackupType == "STANDARD") CapsuleCyan else DarkBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedBackupType = "STANDARD" },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedBackupType == "STANDARD") Color(0xFF0C2430) else DarkSurfaceCard
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
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(if (selectedBackupType == "STANDARD") CapsuleCyan else TextSecondaryDark)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Mode Standar (Universal / Semua HP)",
                                                color = TextPrimaryDark,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = CapsuleCyan.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Semua Perangkat",
                                                color = CapsuleCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "• Seluruh Profil Sandbox (${profiles.size} profil)\n• Daftar seluruh aplikasi kloningan (${capsuleApps.size} aplikasi)\n• Aturan isolasi privasi & freeze timer\n• Seluruh riwayat snapshot & konfigurasi identitas",
                                        color = TextSecondaryDark,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "✓ Ukuran file sangat ringan (< 1 MB), proses instan, kompatibel di semua HP.",
                                        color = CapsuleCyan,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Card 2: Full Data Mode (Requires Root)
                        item {
                            val isRoot = rootStatus.isGranted
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = if (selectedBackupType == "FULL_ROOT") 1.5.dp else 1.dp,
                                        color = if (selectedBackupType == "FULL_ROOT") Color(0xFF00E676) else DarkBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedBackupType = "FULL_ROOT" },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedBackupType == "FULL_ROOT") Color(0xFF06291C) else DarkSurfaceCard
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
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(if (selectedBackupType == "FULL_ROOT") Color(0xFF00E676) else TextSecondaryDark)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Mode Penuh + Data Login (Membutuhkan Root)",
                                                color = TextPrimaryDark,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isRoot) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (isRoot) "ROOT SIAP 🛡️" else "PERLU ROOT",
                                                color = if (isRoot) Color(0xFF00E676) else Color(0xFFA78BFA),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "• Semua isi dari Mode Standar di atas\n• + Database internal aplikasi (/data/user/X/ & /data/data/)\n• + Sesi token login & shared preferences agar akun tidak perlu login ulang di HP baru",
                                        color = TextSecondaryDark,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "⚡ Mengembalikan status akun aktif langsung saat direstore di HP baru berakses Root.",
                                        color = Color(0xFF00E676),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Generate & Share Buttons
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isGeneratingExport = true
                                    coroutineScope.launch {
                                        val json = MigrationManager.generateMigrationPackage(
                                            context = context,
                                            backupType = selectedBackupType,
                                            profiles = profiles,
                                            apps = capsuleApps,
                                            snapshots = snapshots,
                                            identities = identities
                                        )
                                        generatedPackageJson = json
                                        isGeneratingExport = false
                                        MigrationManager.exportAndShareCapsuleFile(
                                            context = context,
                                            jsonContent = json,
                                            isFullRoot = selectedBackupType == "FULL_ROOT"
                                        )
                                    }
                                },
                                enabled = !isGeneratingExport,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedBackupType == "FULL_ROOT") Color(0xFF00E676) else CapsuleCyan,
                                    contentColor = Color(0xFF090D16)
                                )
                            ) {
                                if (isGeneratingExport) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color(0xFF090D16),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mengemas Paket .capsule...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Simpan / Bagikan Berkas (.capsule)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // Copy JSON Quick Option
                        generatedPackageJson?.let { rawJson ->
                            item {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("CapsulePro Migration JSON", rawJson)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "✓ Teks migrasi disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CapsuleCyan)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Salin Kode Cadangan JSON", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: RESTORE .CAPSULE
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "PILIH / TEMPEL BERKAS CADANGAN (.CAPSULE):",
                                color = TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = importJsonInput,
                                onValueChange = {
                                    importJsonInput = it
                                    if (it.trim().startsWith("{")) {
                                        isInspecting = true
                                        coroutineScope.launch {
                                            inspectingSummary = MigrationManager.inspectMigrationPackage(it)
                                            parsedMigrationData = MigrationManager.parseMigrationPackage(it)
                                            isInspecting = false
                                        }
                                    } else {
                                        inspectingSummary = null
                                        parsedMigrationData = null
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                placeholder = { Text("Tempel isi teks berkas .capsule atau JSON di sini...", fontSize = 12.sp, color = TextSecondaryDark) },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CapsuleCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark
                                )
                            )
                        }

                        // Quick Paste Button
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = clipboard.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            val text = clip.getItemAt(0).text.toString()
                                            importJsonInput = text
                                            isInspecting = true
                                            coroutineScope.launch {
                                                inspectingSummary = MigrationManager.inspectMigrationPackage(text)
                                                parsedMigrationData = MigrationManager.parseMigrationPackage(text)
                                                isInspecting = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CapsuleCyan)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tempel dari Clipboard", fontSize = 12.sp)
                                }
                            }
                        }

                        // PRE-RESTORE INSPECTOR PREVIEW
                        inspectingSummary?.let { summary ->
                            val isRoot = rootStatus.isGranted
                            val isFullRootFile = summary.isFullRoot

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = SandboxGreen,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Paket Cadangan Valid!",
                                                    color = TextPrimaryDark,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isFullRootFile) Color(0xFF00E676).copy(alpha = 0.2f) else CapsuleCyan.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (isFullRootFile) "FULL ROOT DATA" else "STANDARD",
                                                    color = if (isFullRootFile) Color(0xFF00E676) else CapsuleCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "• Dibuat: ${summary.createdDateFormatted}\n• Asal Perangkat: ${summary.sourceDevice}\n• Jumlah Profil: ${summary.profileCount} profil\n• Jumlah Aplikasi: ${summary.appCount} aplikasi\n• Konfigurasi Identitas: ${summary.identityCount} profil\n• Snapshot Checkpoints: ${summary.snapshotCount}",
                                            color = TextSecondaryDark,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        )

                                        // Fallback Root check
                                        if (isFullRootFile && !isRoot) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1D0E)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("File Berisi Full Root Data, Tapi HP Belum Root", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Anda tetap bisa memulihkan semua profil & aturan privasi sekarang secara Standar. File cadangan ini tidak akan rusak dan dapat di-restore ulang secara Full Root kapan pun setelah HP di-root!",
                                                        color = TextSecondaryDark,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Execute Button
                                        Button(
                                            onClick = {
                                                parsedMigrationData?.let { data ->
                                                    onExecuteRestore(data, isFullRootFile && isRoot)
                                                    onDismiss()
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isFullRootFile && isRoot) Color(0xFF00E676) else CapsuleCyan,
                                                contentColor = Color(0xFF090D16)
                                            )
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isFullRootFile && isRoot) "Mulai Restore Full Root Sekarang" else "Mulai Pemulihan Cepat",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
