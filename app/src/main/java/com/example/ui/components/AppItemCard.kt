package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.ui.theme.AutoFreezeAmber
import com.example.ui.theme.CapsuleCyan
import com.example.ui.theme.CapsuleCyanDark
import com.example.ui.theme.CapsuleTealContainer
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlacierBlue
import com.example.ui.theme.GlacierBlueContainer
import com.example.ui.theme.SandboxGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun AppItemCard(
    app: AppItem,
    isCapsuleSpace: Boolean,
    onCardClick: () -> Unit,
    onCloneClick: () -> Unit,
    onLaunchClick: () -> Unit,
    onFreezeToggleClick: () -> Unit,
    onOpsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBorder = if (app.isFrozen) {
        androidx.compose.foundation.BorderStroke(1.dp, GlacierBlue.copy(alpha = 0.5f))
    } else if (app.isCloned) {
        androidx.compose.foundation.BorderStroke(1.dp, CapsuleCyan.copy(alpha = 0.35f))
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    }

    val cardBg = if (app.isFrozen) {
        Color(0xFF0F1B2E)
    } else {
        DarkSurfaceCard
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with badge
                Box {
                    AppIconImage(
                        drawable = app.icon,
                        appName = app.appName,
                        size = 50.dp,
                        shapeRadius = 12.dp
                    )

                    if (app.isCloned) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(CapsuleCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Cloned",
                                tint = Color(0xFF070B14),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // App Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = app.appName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Status Badge
                        if (isCapsuleSpace) {
                            if (app.isFrozen) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GlacierBlueContainer,
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, GlacierBlue)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AcUnit,
                                            contentDescription = null,
                                            tint = GlacierBlue,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "FROZEN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GlacierBlue
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF00381B),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, SandboxGreen)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = SandboxGreen,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SandboxGreen
                                        )
                                    }
                                }
                            }
                        } else if (app.isCloned) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CapsuleTealContainer,
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, CapsuleCyan)
                            ) {
                                Text(
                                    text = "IN CAPSULE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CapsuleCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Chips row (vX.X, Ram, System, Auto-Freeze)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DarkSurfaceVariant
                        ) {
                            Text(
                                text = "v${app.versionName}",
                                fontSize = 9.sp,
                                color = TextSecondaryDark,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DarkSurfaceVariant
                        ) {
                            Text(
                                text = "~${app.estimatedRamMb} MB",
                                fontSize = 9.sp,
                                color = if (app.isFrozen) GlacierBlue else TextSecondaryDark,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        if (app.isAutoFreeze) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF3E2723)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockClock,
                                        contentDescription = null,
                                        tint = AutoFreezeAmber,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${app.autoFreezeDelaySeconds}s",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AutoFreezeAmber
                                    )
                                }
                            }
                        }

                        if (app.isSystem) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = DarkSurfaceVariant
                            ) {
                                Text(
                                    text = "System",
                                    fontSize = 9.sp,
                                    color = Color(0xFFFFA726),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCapsuleSpace) {
                    // Freeze / Defrost Button
                    Button(
                        onClick = onFreezeToggleClick,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (app.isFrozen) SandboxGreen else GlacierBlue,
                            contentColor = Color(0xFF090D16)
                        )
                    ) {
                        Icon(
                            imageVector = if (app.isFrozen) Icons.Default.Bolt else Icons.Default.AcUnit,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (app.isFrozen) "Cairkan" else "Bekukan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Launch in Capsule Button
                    Button(
                        onClick = onLaunchClick,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CapsuleCyan,
                            contentColor = Color(0xFF090D16)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Buka",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Privacy Ops Button
                    OutlinedButton(
                        onClick = onOpsClick,
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CapsuleCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "App Ops",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    // Mainland Actions
                    if (!app.isCloned) {
                        Button(
                            onClick = onCloneClick,
                            modifier = Modifier.weight(1.3f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CapsuleCyan,
                                contentColor = Color(0xFF090D16)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Klon ke Kapsul",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onCardClick,
                            modifier = Modifier.weight(1.3f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CapsuleCyan
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CapsuleCyan.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Terkonfigurasi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mainland direct launch
                    OutlinedButton(
                        onClick = onLaunchClick,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimaryDark
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Buka Utama",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
