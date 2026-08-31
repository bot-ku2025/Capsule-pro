package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.remember
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

@Composable
fun ShuttleAdbScreen(
    onShowToast: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isAdminActive = remember { CapsulePolicyManager.isDeviceAdminActive(context) }
    val isProfileOwner = remember { CapsulePolicyManager.isProfileOwner(context) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // File Shuttle Bridge Section
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

        // Provisioning & Work Profile Status
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

                    Button(
                        onClick = {
                            try {
                                val intent = CapsulePolicyManager.createProvisioningIntent(context)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                onShowToast("Gunakan perintah ADB di bawah jika provisioning DPM otomatis tidak didukung ROM Anda.")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CapsuleTealContainer,
                            contentColor = CapsuleCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CapsuleCyan.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buka Android Work Profile Setup Wizard",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Privileged ADB / Shizuku Command Center
        item {
            Text(
                text = "PERINTAH ADB & SHIZUKU (1-TAP SALIN)",
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
