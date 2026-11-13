// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.theme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Centralized haptic controller respecting the user's haptics preference.
 */
class CleanBotHaptics(
    private val haptic: HapticFeedback,
    private val enabled: Boolean,
) {
    fun tap() {
        if (enabled) haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
    }

    fun tick() {
        if (enabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun confirm() {
        if (enabled) haptic.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    fun reject() {
        if (enabled) haptic.performHapticFeedback(HapticFeedbackType.Reject)
    }

    fun toggle(on: Boolean) {
        if (enabled) {
            haptic.performHapticFeedback(
                if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
            )
        }
    }
}

@Composable
fun rememberCleanBotHaptics(): CleanBotHaptics {
    val haptic = LocalHapticFeedback.current
    val enabled = LocalHapticsEnabled.current
    return remember(haptic, enabled) { CleanBotHaptics(haptic, enabled) }
}

/**
 * Tactile spring bounce scale when pressed.
 */
@Composable
fun Modifier.tactilePressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.965f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "tactileScale",
    )
    return this.scale(scale)
}

/**
 * High-contrast Signal Yellow CTA (10% palette accent) with spring press & confirmation haptic.
 */
@Composable
fun SignalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val tokens = LocalCleanBotTokens.current
    val haptics = rememberCleanBotHaptics()
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = {
            haptics.confirm()
            onClick()
        },
        modifier = modifier.tactilePressScale(interaction),
        enabled = enabled,
        interactionSource = interaction,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = tokens.signalYellow,
            contentColor = tokens.onSignalYellow,
            disabledContainerColor = tokens.sandBorder.copy(alpha = 0.4f),
            disabledContentColor = tokens.sandMutedInk.copy(alpha = 0.6f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
        content = content,
    )
}

/**
 * Olive Drab structural button (30% palette surface) with tactile bounce & tap haptic.
 */
@Composable
fun OliveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val tokens = LocalCleanBotTokens.current
    val haptics = rememberCleanBotHaptics()
    val interaction = remember { MutableInteractionSource() }
    FilledTonalButton(
        onClick = {
            haptics.tap()
            onClick()
        },
        modifier = modifier.tactilePressScale(interaction),
        enabled = enabled,
        interactionSource = interaction,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = tokens.oliveDrab,
            contentColor = tokens.onOliveDrab,
        ),
        content = content,
    )
}

/**
 * High-contrast outlined button on Desert Sand surfaces.
 */
@Composable
fun SandOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val tokens = LocalCleanBotTokens.current
    val haptics = rememberCleanBotHaptics()
    val interaction = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = {
            haptics.tap()
            onClick()
        },
        modifier = modifier.tactilePressScale(interaction),
        enabled = enabled,
        interactionSource = interaction,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (enabled) tokens.oliveDrab else tokens.sandBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.oliveDrabDeep),
        content = content,
    )
}

/**
 * Elevated Desert Sand card (60% canvas tone) with crisp sand border.
 */
@Composable
fun SandCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    containerColor: Color = LocalCleanBotTokens.current.desertSandCard,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = LocalCleanBotTokens.current
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = tokens.sandInk,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        content = content,
    )
}

/**
 * Deep Olive Drab hero / structural card (30% palette tone) with warm sand ink.
 */
@Composable
fun OliveHeroCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = LocalCleanBotTokens.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = tokens.oliveDrabDeep,
            contentColor = tokens.onOliveDrab,
        ),
        border = BorderStroke(1.dp, tokens.oliveDrabSoft.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        content = content,
    )
}

/**
 * Animated pulsing status beacon (Signal Yellow when active/live, soft sand when idle).
 */
@Composable
fun TacticalPulseBeacon(
    active: Boolean,
    size: Dp = 10.dp,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalCleanBotTokens.current
    val transition = rememberInfiniteTransition(label = "beacon")
    val pulseScale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "beaconScale",
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "beaconAlpha",
    )
    val dotColor by animateColorAsState(
        targetValue = if (active) tokens.signalYellow else tokens.sandMutedInk,
        animationSpec = tween(300),
        label = "beaconColor",
    )
    Box(modifier = modifier.size(size * 2), contentAlignment = Alignment.Center) {
        if (active) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = pulseAlpha)),
            )
        }
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(dotColor),
        )
    }
}

/**
 * Staggered entrance animation for cards and list sections.
 */
@Composable
fun StaggeredCardEntrance(
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(delayMillis == 0) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(260)) + slideInVertically(
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
            initialOffsetY = { it / 6 },
        ) + scaleIn(initialScale = 0.97f, animationSpec = tween(260)),
    ) {
        content()
    }
}

/**
 * Animated tactile progress bar with Signal Yellow fill over an Olive Drab rail.
 */
@Composable
fun SignalProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
) {
    val tokens = LocalCleanBotTokens.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "signalProgress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(tokens.oliveDrab.copy(alpha = 0.25f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(height)
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(tokens.signalYellow, tokens.signalYellowBright),
                    ),
                ),
        )
    }
}

/**
 * Smooth expressive container transition for state/step switching.
 */
@Composable
fun <S> ExpressiveStepContent(
    targetState: S,
    modifier: Modifier = Modifier,
    content: @Composable (S) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(220)) + slideInVertically { it / 10 }) togetherWith fadeOut(tween(150))
        },
        label = "stepTransition",
    ) { state ->
        content(state)
    }
}
