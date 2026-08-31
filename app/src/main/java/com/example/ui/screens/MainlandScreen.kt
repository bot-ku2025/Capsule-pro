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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import com.example.ui.MainlandFilter
import com.example.ui.components.AppItemCard
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.CapsuleTealContainer
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun MainlandScreen(
    apps: List<AppItem>,
    currentFilter: MainlandFilter,
    onFilterSelect: (MainlandFilter) -> Unit,
    onAppClick: (AppItem) -> Unit,
    onCloneClick: (AppItem) -> Unit,
    onLaunchClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = currentFilter == MainlandFilter.ALL,
                    onClick = { onFilterSelect(MainlandFilter.ALL) },
                    label = { Text("Semua (${apps.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CapsuleCyan,
                        selectedLabelColor = Color(0xFF090D16),
                        containerColor = DarkSurfaceCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = currentFilter == MainlandFilter.ALL,
                        borderColor = DarkBorder,
                        selectedBorderColor = CapsuleCyan
                    )
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == MainlandFilter.NOT_IN_CAPSULE,
                    onClick = { onFilterSelect(MainlandFilter.NOT_IN_CAPSULE) },
                    label = { Text("Belum Dikloning", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CapsuleCyan,
                        selectedLabelColor = Color(0xFF090D16),
                        containerColor = DarkSurfaceCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = currentFilter == MainlandFilter.NOT_IN_CAPSULE,
                        borderColor = DarkBorder,
                        selectedBorderColor = CapsuleCyan
                    )
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == MainlandFilter.USER_ONLY,
                    onClick = { onFilterSelect(MainlandFilter.USER_ONLY) },
                    label = { Text("Aplikasi Pengguna", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CapsuleCyan,
                        selectedLabelColor = Color(0xFF090D16),
                        containerColor = DarkSurfaceCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = currentFilter == MainlandFilter.USER_ONLY,
                        borderColor = DarkBorder,
                        selectedBorderColor = CapsuleCyan
                    )
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == MainlandFilter.SYSTEM_ONLY,
                    onClick = { onFilterSelect(MainlandFilter.SYSTEM_ONLY) },
                    label = { Text("Sistem", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CapsuleCyan,
                        selectedLabelColor = Color(0xFF090D16),
                        containerColor = DarkSurfaceCard,
                        labelColor = TextSecondaryDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = currentFilter == MainlandFilter.SYSTEM_ONLY,
                        borderColor = DarkBorder,
                        selectedBorderColor = CapsuleCyan
                    )
                )
            }
        }

        // Info Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = CapsuleCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Mainland (Ruang Utama): Pilih aplikasi untuk dikloning ke dalam Capsule Sandbox agar terisolasi dari data pribadi.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark,
                    lineHeight = 14.sp
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
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak Ada Aplikasi Ditemukan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Coba ubah kata kunci pencarian atau filter di atas.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppItemCard(
                        app = app,
                        isCapsuleSpace = false,
                        onCardClick = { onAppClick(app) },
                        onCloneClick = { onCloneClick(app) },
                        onLaunchClick = { onLaunchClick(app) },
                        onFreezeToggleClick = {},
                        onOpsClick = {}
                    )
                }
            }
        }
    }
}
