package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun PrivacyOpsDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onSaveOps: (blockLocation: Boolean, blockContacts: Boolean, blockCamera: Boolean, blockNetwork: Boolean, isolatedStorage: Boolean) -> Unit
) {
    var blockLocation by remember { mutableStateOf(app.blockLocation) }
    var blockContacts by remember { mutableStateOf(app.blockContacts) }
    var blockCamera by remember { mutableStateOf(app.blockCamera) }
    var blockNetwork by remember { mutableStateOf(app.blockBackgroundNetwork) }
    var isolatedStorage by remember { mutableStateOf(app.isolatedStorage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = CapsuleCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Privacy Ops Guard",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = app.appName,
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Konfigurasi isolasi hak akses khusus di dalam Capsule sandbox:",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Storage Isolation Switch
                OpsToggleItem(
                    icon = Icons.Default.FolderSpecial,
                    title = "Isolasi Penyimpanan File",
                    subtitle = "Pisahkan akses foto, dokumen & media dari profil utama",
                    checked = isolatedStorage,
                    onCheckedChange = { isolatedStorage = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location Guard
                OpsToggleItem(
                    icon = Icons.Default.LocationOn,
                    title = "Blokir Pelacakan Lokasi",
                    subtitle = "Kirim lokasi kosong saat aplikasi meminta GPS",
                    checked = blockLocation,
                    onCheckedChange = { blockLocation = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Contacts Guard
                OpsToggleItem(
                    icon = Icons.Default.Contacts,
                    title = "Blokir Akses Buku Kontak",
                    subtitle = "Cegah aplikasi membaca nomor telepon utama",
                    checked = blockContacts,
                    onCheckedChange = { blockContacts = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Camera Guard
                OpsToggleItem(
                    icon = Icons.Default.CameraAlt,
                    title = "Blokir Sensor Kamera Latar",
                    subtitle = "Matikan izin kamera saat aplikasi di latar belakang",
                    checked = blockCamera,
                    onCheckedChange = { blockCamera = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Background Data Cut
                OpsToggleItem(
                    icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                    title = "Putus Data Latar Belakang",
                    subtitle = "Hentikan transmisi data internet saat aplikasi tidak aktif",
                    checked = blockNetwork,
                    onCheckedChange = { blockNetwork = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveOps(blockLocation, blockContacts, blockCamera, blockNetwork, isolatedStorage)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CapsuleCyan,
                    contentColor = Color(0xFF090D16)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryDark)
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun OpsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) CapsuleCyan else TextSecondaryDark,
                    modifier = Modifier.size(20.dp)
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
                        text = subtitle,
                        fontSize = 10.sp,
                        color = TextSecondaryDark,
                        lineHeight = 12.sp
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF090D16),
                    checkedTrackColor = CapsuleCyan,
                    uncheckedThumbColor = TextSecondaryDark,
                    uncheckedTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}
