package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.CapsulePolicyManager
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.GlacierBlue
import com.example.ui.theme.SandboxGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningOrange
import com.example.util.LanguageManager
import com.example.util.RootStatus

@Composable
fun IslandSetupWizardScreen(
    rootStatus: RootStatus,
    onRequestRootAccess: () -> Unit,
    onSetupWorkProfileViaRoot: () -> Unit,
    onCompleteSetup: () -> Unit,
    canDismiss: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var activeTabStep by remember { mutableIntStateOf(0) } // 0: Konsep, 1: Provisioning, 2: Status
    var expandedMethod by remember { mutableIntStateOf(1) } // 1: Enterprise DPM, 2: Root, 3: ADB, 4: Sandbox

    val isProfileOwner = remember { CapsulePolicyManager.isProfileOwner(context) }
    val isDeviceAdmin = remember { CapsulePolicyManager.isDeviceAdminActive(context) }
    val isManagedProfile = remember { CapsulePolicyManager.isManagedProfile(context) }

    val isAnyIsolationActive = isProfileOwner || isDeviceAdmin || isManagedProfile || rootStatus.isGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar with Optional Close
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CapsuleCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = CapsuleCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Island Enterprise Setup",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CapsuleCyan
                )
            }

            if (canDismiss) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = TextSecondaryDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CapsuleCyan, Color(0xFF00838F))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF090D16),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Setup Ruang Isolasi Android",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Teknologi Managed Profile (Work Profile) bawaan sistem Android. Mengisolasi aplikasi ganda dengan 0% konsumsi baterai tambahan & pemisahan data 100% aman.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step Navigation Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StepTabPill(
                title = "1. Konsep Isolasi",
                isSelected = activeTabStep == 0,
                modifier = Modifier.weight(1f),
                onClick = { activeTabStep = 0 }
            )
            StepTabPill(
                title = "2. Aktivasi Profil",
                isSelected = activeTabStep == 1,
                modifier = Modifier.weight(1f),
                onClick = { activeTabStep = 1 }
            )
            StepTabPill(
                title = "3. Masuk Capsule",
                isSelected = activeTabStep == 2,
                modifier = Modifier.weight(1f),
                onClick = { activeTabStep = 2 }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeTabStep) {
            0 -> {
                // STEP 1: CONCEPT EXPLANATION (MAINLAND VS ISLAND)
                ConceptExplanationSection(onNext = { activeTabStep = 1 })
            }
            1 -> {
                // STEP 2: PROVISIONING METHODS
                ProvisioningMethodsSection(
                    rootStatus = rootStatus,
                    expandedMethod = expandedMethod,
                    onSelectMethod = { expandedMethod = it },
                    onRequestRoot = onRequestRootAccess,
                    onSetupRoot = onSetupWorkProfileViaRoot,
                    onLaunchDpm = {
                        try {
                            val intent = CapsulePolicyManager.createProvisioningIntent(context)
                            context.startActivity(intent)
                            Toast.makeText(context, "Memulai Android Enterprise Provisioning...", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            try {
                                val fallbackIntent = CapsulePolicyManager.createAddDeviceAdminIntent(context)
                                context.startActivity(fallbackIntent)
                                Toast.makeText(context, "Membuka aktivasi Device Administrator...", Toast.LENGTH_SHORT).show()
                            } catch (e2: Exception) {
                                Toast.makeText(context, "Error memulai provisioning: ${e2.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onNext = { activeTabStep = 2 }
                )
            }
            2 -> {
                // STEP 3: STATUS & ENTER CAPSULE PRO
                StatusAndLaunchSection(
                    isAnyIsolationActive = isAnyIsolationActive,
                    isProfileOwner = isProfileOwner,
                    isDeviceAdmin = isDeviceAdmin,
                    isManagedProfile = isManagedProfile,
                    rootGranted = rootStatus.isGranted,
                    onEnterCapsule = onCompleteSetup
                )
            }
        }
    }
}

@Composable
private fun StepTabPill(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) CapsuleCyan.copy(alpha = 0.2f) else DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CapsuleCyan else DarkBorder
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) CapsuleCyan else TextSecondaryDark,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ConceptExplanationSection(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mainland Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Mainland (Ruang Pribadi)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ruang utama smartphone Anda. Seluruh data pribadi, foto galeri, kontak, dan akun asli Anda berada di sini dengan aman tanpa terganggu.",
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Island / Capsule Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, CapsuleCyan.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CapsuleCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = CapsuleCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Island / Capsule (Ruang Isolasi)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CapsuleCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ruang isolasi ber-badge tas kerja di level sistem Android. Aplikasi di dalam Island tidak dapat membaca SMS, galeri, atau data Mainland, dan dapat dibekukan instan.",
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Key Pillars
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PillarItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "0% Extra Battery Drain",
                    desc = "Menggunakan fitur multi-user native Android, bukan virtual machine yang lambat."
                )
                HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                PillarItem(
                    icon = Icons.Default.AcUnit,
                    title = "True Kernel Freezing (Glacier)",
                    desc = "Membekukan aplikasi agar 0% memori RAM terpakai saat aplikasi tidak digunakan."
                )
                HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                PillarItem(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Fitur Lanjutan Capsule Pro",
                    desc = "Di dalam ruang kerja, nikmati Identity Spoofing, Play Engine bypass, IP Fresh cycling, dan Snapshot backup."
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CapsuleCyan,
                contentColor = Color(0xFF090D16)
            )
        ) {
            Text(
                text = "Lanjut ke Metode Aktivasi",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PillarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CapsuleCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
            Text(
                text = desc,
                fontSize = 10.sp,
                color = TextSecondaryDark
            )
        }
    }
}

@Composable
private fun ProvisioningMethodsSection(
    rootStatus: RootStatus,
    expandedMethod: Int,
    onSelectMethod: (Int) -> Unit,
    onRequestRoot: () -> Unit,
    onSetupRoot: () -> Unit,
    onLaunchDpm: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val pkgName = context.packageName

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Pilih salah satu metode di bawah untuk mengaktifkan ruang kerja:",
            fontSize = 12.sp,
            color = TextSecondaryDark
        )

        // METHOD 1: OFFICIAL ANDROID ENTERPRISE PROVISIONING
        MethodAccordionCard(
            number = "A",
            title = "Android Enterprise DPM (Resmi)",
            subtitle = "Alur aktivasi Work Profile resmi Google",
            isRecommended = true,
            isExpanded = expandedMethod == 1,
            onClick = { onSelectMethod(1) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Metode paling direkomendasikan jika HP Anda mendukung pembuatan Work Profile. Menjalankan dialog resmi sistem.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
                Button(
                    onClick = onLaunchDpm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CapsuleCyan, contentColor = Color(0xFF090D16))
                ) {
                    Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🏢 Buka Dialog Android Enterprise", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // METHOD 2: 1-CLICK ROOT SUPERUSER
        MethodAccordionCard(
            number = "B",
            title = "1-Klik via Root Superuser",
            subtitle = if (rootStatus.isGranted) "✓ Akses Root Terdeteksi" else "Perlu Magisk / KernelSU",
            isRecommended = rootStatus.isGranted,
            isExpanded = expandedMethod == 2,
            onClick = { onSelectMethod(2) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Langsung membuat user profile baru dan menetapkan Profile Owner secara otomatis via shell root tanpa butuh PC.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
                if (rootStatus.isGranted) {
                    Button(
                        onClick = onSetupRoot,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SandboxGreen, contentColor = Color(0xFF042014))
                    ) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("⚡ Eksekusi 1-Klik Setup Work Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onRequestRoot,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarningOrange, contentColor = Color.Black)
                    ) {
                        Text("Minta Izin Superuser Root", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // METHOD 3: ADB COMMAND
        MethodAccordionCard(
            number = "C",
            title = "Perintah ADB Shell (PC / Shizuku)",
            subtitle = "Aktivasi manual via komputer atau wireless debugging",
            isRecommended = false,
            isExpanded = expandedMethod == 3,
            onClick = { onSelectMethod(3) }
        ) {
            val adbCmd = CapsulePolicyManager.getAdbCommandForProfileOwner(pkgName)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Jalankan perintah berikut melalui Terminal ADB di PC Anda:",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = adbCmd,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CapsuleCyan,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", adbCmd))
                                Toast.makeText(context, "Perintah disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondaryDark, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // METHOD 4: SOFTWARE SANDBOX
        MethodAccordionCard(
            number = "D",
            title = "Software Sandbox Engine",
            subtitle = "Langsung aktifkan isolasi data instan tanpa syarat",
            isRecommended = false,
            isExpanded = expandedMethod == 4,
            onClick = { onSelectMethod(4) }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Gunakan engine isolasi data lokal internal Capsule Pro. Tidak memerlukan hak administrator khusus dan langsung siap digunakan.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
                OutlinedButton(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CapsuleCyan)
                ) {
                    Text("🛡️ Lanjutkan dengan Software Sandbox", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CapsuleCyan,
                contentColor = Color(0xFF090D16)
            )
        ) {
            Text(
                text = "Periksa Status & Selesaikan",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MethodAccordionCard(
    number: String,
    title: String,
    subtitle: String,
    isRecommended: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) CapsuleCyan else DarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isExpanded) CapsuleCyan else DarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpanded) Color(0xFF090D16) else TextSecondaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            if (isRecommended) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SandboxGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Direkomendasikan",
                                        color = SandboxGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun StatusAndLaunchSection(
    isAnyIsolationActive: Boolean,
    isProfileOwner: Boolean,
    isDeviceAdmin: Boolean,
    isManagedProfile: Boolean,
    rootGranted: Boolean,
    onEnterCapsule: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isAnyIsolationActive) SandboxGreen else WarningOrange
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAnyIsolationActive) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isAnyIsolationActive) SandboxGreen else WarningOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isAnyIsolationActive) "Status: Ruang Isolasi Siap!" else "Status: Siap Masuk Mode Sandbox",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAnyIsolationActive) SandboxGreen else WarningOrange
                        )
                        Text(
                            text = if (isAnyIsolationActive) "Lingkungan terisolasi telah aktif di perangkat Anda." else "Capsule Pro akan beroperasi dengan software container.",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                StatusRow(label = "Device Administrator", isOk = isDeviceAdmin)
                StatusRow(label = "Profile Owner (Work Profile)", isOk = isProfileOwner || isManagedProfile)
                StatusRow(label = "Superuser Root Access", isOk = rootGranted)
                StatusRow(label = "CapsulePro Sandbox Engine", isOk = true)
            }
        }

        // What you get inside
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "✨ Fitur Lengkap Capsule Pro di Dalam Ruang:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CapsuleCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Mainland & Capsule: Kloning APK, freeze/unfreeze tanpa makan RAM\n• Identitas: Spoofing IMEI, Android ID, GSF, MAC, Serial & Brand Model\n• Glacier: Freezer otomatis saat layar mati\n• Play Engine: Pemasang APK bypass Play Store resmi\n• Bridge: File shuttle antar ruang & konsol perintah ADB",
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onEnterCapsule,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CapsuleCyan,
                contentColor = Color(0xFF090D16)
            )
        ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Buka Capsule Pro Sekarang",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusRow(label: String, isOk: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = TextPrimaryDark)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOk) SandboxGreen else Color.Gray)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOk) "Aktif" else "Opsional",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isOk) SandboxGreen else TextSecondaryDark
            )
        }
    }
}
