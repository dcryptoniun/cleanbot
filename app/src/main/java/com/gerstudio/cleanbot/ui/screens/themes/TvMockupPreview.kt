// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.screens.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.model.TvClockStyle
import com.gerstudio.cleanbot.data.model.TvDockStyle
import com.gerstudio.cleanbot.data.model.TvTheme

@Composable
fun TvMockupPreview(
    theme: TvTheme,
    modifier: Modifier = Modifier
) {
    // Outer TV chassis with ultra-slim bezel and subtle living room backlight
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101317)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFF262E35), RoundedCornerShape(20.dp))
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = theme.primaryAccent.copy(alpha = 0.35f),
                spotColor = theme.secondaryAccent.copy(alpha = 0.45f)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 16:9 Screen panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = theme.gradientColors,
                            radius = 600f
                        )
                    )
            ) {
                // TV Status Header (Clock & Status Icons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clock
                    val timeStr = when (theme.clockStyle) {
                        TvClockStyle.TWELVE_HOUR -> "08:45 PM"
                        TvClockStyle.TWENTY_FOUR_HOUR -> "20:45"
                        TvClockStyle.SYSTEM_DEFAULT -> "20:45"
                    }
                    Text(
                        text = timeStr,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(theme.primaryAccent.copy(alpha = 0.25f))
                                .border(0.5.dp, theme.primaryAccent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "4K HDR",
                                color = theme.primaryAccent,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            Icons.Default.Wifi,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                // Top Shelf Showcase (Focused item banner preview)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(52.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(theme.cornerRadiusDp.dp / 2))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    theme.secondaryAccent.copy(alpha = 0.25f),
                                    theme.primaryAccent.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            1.dp,
                            theme.primaryAccent.copy(alpha = 0.4f),
                            RoundedCornerShape(theme.cornerRadiusDp.dp / 2)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primaryAccent.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Movie,
                                contentDescription = null,
                                tint = theme.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CleanYourTV Experience",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Zero Ads • Maximum Speed • ${theme.name}",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 8.sp
                            )
                        }
                    }
                }

                // Simulated App Dock at the bottom
                val dockModifier = when (theme.dockStyle) {
                    TvDockStyle.GLASS_FROST -> Modifier
                        .fillMaxWidth(0.88f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(theme.cornerRadiusDp.dp))
                        .background(Color(0xFF1E222B).copy(alpha = theme.dockOpacity))
                        .border(1.dp, theme.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(theme.cornerRadiusDp.dp))
                    TvDockStyle.FLOATING_PILL -> Modifier
                        .fillMaxWidth(0.84f)
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF14171E).copy(alpha = theme.dockOpacity))
                        .shadow(4.dp, CircleShape)
                        .border(1.dp, theme.secondaryAccent.copy(alpha = 0.4f), CircleShape)
                    TvDockStyle.OLED_MINIMAL -> Modifier
                        .fillMaxWidth(0.88f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.95f))
                        .border(0.5.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
                    TvDockStyle.MODERN_OUTLINE -> Modifier
                        .fillMaxWidth(0.86f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F0B18).copy(alpha = theme.dockOpacity))
                        .border(1.5.dp, theme.primaryAccent, RoundedCornerShape(12.dp))
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                        .then(dockModifier),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val mockIcons = listOf(
                            Icons.Default.Tv,
                            Icons.Default.Videocam,
                            Icons.Default.MusicNote,
                            Icons.Default.Movie,
                            Icons.Default.Cast,
                            Icons.Default.Settings
                        )

                        mockIcons.forEachIndexed { index, icon ->
                            val isFocused = index == 0
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isFocused) theme.primaryAccent
                                        else Color.White.copy(alpha = 0.12f)
                                    )
                                    .border(
                                        if (isFocused) 1.dp else 0.dp,
                                        if (isFocused) Color.White else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isFocused) Color.Black else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        if (theme.appLibraryPill) {
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .clip(CircleShape)
                                    .background(theme.secondaryAccent.copy(alpha = 0.25f))
                                    .border(0.5.dp, theme.secondaryAccent, CircleShape)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Apps",
                                    color = theme.secondaryAccent,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // TV bottom chin bezel with standby power LED
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(theme.primaryAccent)
                )
            }
        }
    }
}
