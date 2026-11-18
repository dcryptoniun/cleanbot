// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.OliveButton
import com.gerstudio.cleanbot.ui.theme.SandOutlinedButton
import com.gerstudio.cleanbot.ui.theme.SignalButton
import com.gerstudio.cleanbot.ui.theme.SignalProgressBar
import com.gerstudio.cleanbot.ui.theme.TacticalPulseBeacon
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

/** Visual icon categorization for operations. */
enum class OperationIcon {
    DEBLOAT,
    LAUNCHER,
    APP_INSTALL,
    THEME,
    RESTORE,
    GENERIC,
}

/**
 * State representing an active background operation on the TV.
 */
data class OperationProcessingState(
    val title: String,
    val details: String = "",
    val progress: Float? = null,
    val step: Int? = null,
    val total: Int? = null,
    val iconType: OperationIcon = OperationIcon.GENERIC,
)

/**
 * Polite completion prompt asking user to consider a tip/donation.
 */
data class PoliteTipPrompt(
    val title: String,
    val message: String,
)

/**
 * High-visibility processing modal showing animated progress and tactical effects
 * while debloating, installing CleanYourTV launcher, or sideloading apps.
 */
@Composable
fun OperationProcessingOverlay(
    state: OperationProcessingState,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "processingSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "iconRotation",
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )

    val icon: ImageVector = when (state.iconType) {
        OperationIcon.DEBLOAT -> Icons.Default.AutoFixHigh
        OperationIcon.LAUNCHER -> Icons.Default.Download
        OperationIcon.APP_INSTALL -> Icons.Default.InstallMobile
        OperationIcon.THEME -> Icons.Default.Palette
        OperationIcon.RESTORE -> Icons.Default.SettingsBackupRestore
        OperationIcon.GENERIC -> Icons.Default.Sync
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = CleanBotPalette.DesertSandBright,
                contentColor = CleanBotPalette.SandInk,
            ),
            border = BorderStroke(2.dp, CleanBotPalette.OliveDrab),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Tactical Glowing Operation Icon
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .scale(glowPulse)
                            .clip(CircleShape)
                            .background(CleanBotPalette.SignalYellow.copy(alpha = 0.22f)),
                    )
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CleanBotPalette.OliveDrab),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = CleanBotPalette.SignalYellow,
                            modifier = Modifier
                                .size(28.dp)
                                .rotate(if (state.iconType == OperationIcon.GENERIC) rotation else 0f),
                        )
                    }
                }

                // Title & Details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TacticalPulseBeacon(active = true, size = 9.dp)
                        Text(
                            text = state.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = CleanBotPalette.OliveDrabDeep,
                            textAlign = TextAlign.Center,
                        )
                    }

                    if (state.details.isNotEmpty()) {
                        Text(
                            text = state.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanBotPalette.SandMutedInk,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }

                // Progress Indicator
                if (state.progress != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SignalProgressBar(
                            progress = state.progress,
                            modifier = Modifier.fillMaxWidth(),
                            height = 10.dp,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "${(state.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CleanBotPalette.OliveDrab,
                            )
                            if (state.step != null && state.total != null) {
                                Text(
                                    text = "Step ${state.step} of ${state.total}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CleanBotPalette.SandMutedInk,
                                )
                            }
                        }
                    }
                } else {
                    // Indeterminate Pulsing Indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CleanBotPalette.SandOutline),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CleanBotPalette.SignalYellow),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Polite modal dialog shown after an operation finishes, thanking the user
 * and offering an optional one-tap pathway to tip/donate.
 */
@Composable
fun PoliteTipDialog(
    prompt: PoliteTipPrompt,
    onOpenDonate: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PoliteTipDialog(
        completedTitle = prompt.title,
        message = prompt.message,
        onOpenDonate = onOpenDonate,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@Composable
fun PoliteTipDialog(
    completedTitle: String,
    message: String,
    onOpenDonate: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberCleanBotHaptics()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = CleanBotPalette.DesertSandBright,
        titleContentColor = CleanBotPalette.SandInk,
        textContentColor = CleanBotPalette.SandInk,
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(CleanBotPalette.SignalYellow.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = CleanBotPalette.OliveDrab,
                    modifier = Modifier.size(30.dp),
                )
            }
        },
        title = {
            Text(
                text = completedTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CleanBotPalette.SandInk,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "cLeanTV is 100% ad-free and open source. If this saved you time or improved your living room, consider supporting with a tip!",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleanBotPalette.SandMutedInk,
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            SignalButton(
                onClick = {
                    haptics.confirm()
                    onOpenDonate()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Support & Donate")
            }
        },
        dismissButton = {
            SandOutlinedButton(
                onClick = {
                    haptics.tap()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Done / Maybe Later")
            }
        },
        modifier = modifier.padding(16.dp),
    )
}
