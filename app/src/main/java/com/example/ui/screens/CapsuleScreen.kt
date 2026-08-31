package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.CapsuleFilter
import com.example.ui.components.AppItemCard
import com.example.ui.theme.AutoFreezeAmber
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.CapsuleCyanLight
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.GlacierBlue
import com.example.ui.theme.GlacierBlueContainer
import com.example.ui.theme.SandboxGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun CapsuleScreen(
    apps: List<AppItem>,
    currentFilter: CapsuleFilter,
    onFilterSelect: (CapsuleFilter) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onLaunchClick: (AppItem) -> Unit,
    onFreezeToggle: (AppItem) -> Unit,
    onOpsClick: (AppItem) -> Unit,
    onFreezeAll: () -> Unit,
    onDefrostAll: () -> Unit,
    onNavigateToMainland: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Quick Action Bar (Freeze All / Defrost All)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onFreezeAll,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlacierBlueContainer,
                    contentColor = GlacierBlue
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlacierBlue.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bekukan Semua",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onDefrostAll,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SandboxGreen
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, SandboxGreen.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cairkan Semua",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = currentFilter == CapsuleFilter.ALL,
                    onClick = { onFilterSelect(CapsuleFilter.ALL) },
                    label = { Text("Semua (${apps.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CapsuleCyan,
                        selectedLabelColor = Color(0xFF090D16),
                        containerColor = DarkSurfaceCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = currentFilter == CapsuleFilter.ALL,
                        borderColor = DarkBorder,
                        selectedBorderColor = CapsuleCyan
                    )
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == CapsuleFilter.ACTIVE,
                    onClick = { onFilterSelect(CapsuleFilter.ACTIVE) },
                    label = { Text("Aktif ⚡", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SandboxGreen,
                        selectedLabelColor = Color(0xFF090D16),
                        containerColor = DarkSurfaceCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = currentFilter == CapsuleFilter.ACTIVE,
                        borderColor = DarkBorder,
                        selectedBorderColor = SandboxGreen
                    )
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == CapsuleFilter.FROZEN,
                    onClick = { onFilterSelect(CapsuleFilter.FROZEN) },
                    label = { Text("Dibekukan ❄️", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GlacierBlue,
                        selectedLabelColor = Color(0xFF090D16),
                        containerColor = DarkSurfaceCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = currentFilter == CapsuleFilter.FROZEN,
                        borderColor = DarkBorder,
                        selectedBorderColor = GlacierBlue
                    )
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == CapsuleFilter.AUTO_FREEZE_ENABLED,
                    onClick = { onFilterSelect(CapsuleFilter.AUTO_FREEZE_ENABLED) },
                    label = { Text("Auto-Freeze ⏱️", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AutoFreezeAmber,
                        selectedLabelColor = Color(0xFF090D16),
                        containerColor = DarkSurfaceCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = currentFilter == CapsuleFilter.AUTO_FREEZE_ENABLED,
                        borderColor = DarkBorder,
                        selectedBorderColor = AutoFreezeAmber
                    )
                )
            }
        }

        // App List or Empty State
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(DarkSurfaceCard, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CapsuleCyan,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Belum Ada Aplikasi di Ruang Kapsul",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Kloning aplikasi dari tab 'Mainland' untuk mengisolasi data, mengaktifkan akun ganda, atau membekukan proses latar belakang.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onNavigateToMainland,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CapsuleCyan,
                            contentColor = Color(0xFF090D16)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kloning Aplikasi Sekarang",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppItemCard(
                        app = app,
                        isCapsuleSpace = true,
                        onCardClick = { onAppClick(app) },
                        onCloneClick = {},
                        onLaunchClick = { onLaunchClick(app) },
                        onFreezeToggleClick = { onFreezeToggle(app) },
                        onOpsClick = { onOpsClick(app) }
                    )
                }
            }
        }
    }
}
