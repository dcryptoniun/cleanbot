// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.SignalButton
import com.gerstudio.cleanbot.ui.theme.TacticalPulseBeacon
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

/**
 * Top titlebar displaying the cLeanTV app name, TV connectivity status beacon,
 * and a direct Donate button opening the tip/support screen.
 */
@Composable
fun CleanBotTitleBar(
    appName: String = "cLeanTV",
    selectedDeviceName: String? = null,
    onOpenDonate: () -> Unit,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()

    Surface(
        color = CleanBotPalette.DesertSandBright,
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left: App Logo + Name + Connected Device Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CleanBotPalette.OliveDrab)
                        .border(1.dp, CleanBotPalette.SignalYellow.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = CleanBotPalette.OnOliveDrab,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = CleanBotPalette.SandInk,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CleanBotPalette.SignalYellow.copy(alpha = 0.25f))
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = "v1.4.2",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CleanBotPalette.OliveDrabDeep,
                                fontSize = 10.sp,
                            )
                        }
                    }

                    if (selectedDeviceName != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp),
                        ) {
                            TacticalPulseBeacon(active = true, size = 6.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedDeviceName,
                                style = MaterialTheme.typography.labelSmall,
                                color = CleanBotPalette.SandMutedInk,
                                maxLines = 1,
                            )
                        }
                    } else {
                        Text(
                            text = "No TV Connected",
                            style = MaterialTheme.typography.labelSmall,
                            color = CleanBotPalette.SandMutedInk,
                        )
                    }
                }
            }

            // Right: Donate CTA + Settings
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SignalButton(
                    onClick = {
                        haptics.confirm()
                        onOpenDonate()
                    },
                    modifier = Modifier.height(34.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Donate",
                        modifier = Modifier.size(15.dp),
                        tint = CleanBotPalette.OnSignalYellow,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Donate",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (onOpenSettings != null) {
                    IconButton(
                        onClick = {
                            haptics.tap()
                            onOpenSettings()
                        },
                        modifier = Modifier.size(34.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CleanBotPalette.OliveDrab,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
