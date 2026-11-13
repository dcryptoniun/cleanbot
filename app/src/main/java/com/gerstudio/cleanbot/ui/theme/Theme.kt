// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.datastore.UserPreferences

/**
 * Single fixed palette following the 60 / 30 / 10 visual rule:
 * - 60% Desert Sand: calm warm canvas, cards, and primary surface layers
 * - 30% Olive Drab: hero panels, navigation chrome, remote deck, structural containers
 * - 10% Signal Yellow: primary CTAs, active indicators, progress bars, focus highlights
 */
object CleanBotPalette {
    // 60% Desert Sand tones
    val DesertSand = Color(0xFFE6D8C3)
    val DesertSandBright = Color(0xFFF3EBDF)
    val DesertSandLow = Color(0xFFEDE1CE)
    val DesertSandCard = Color(0xFFDDD0B9)
    val DesertSandHigh = Color(0xFFD3C1A4)
    val DesertSandHighest = Color(0xFFC7B392)
    val SandInk = Color(0xFF1D1B13)
    val SandMutedInk = Color(0xFF4B4637)
    val SandOutline = Color(0xFF7B725B)
    val SandBorder = Color(0xFFAFA186)

    // 30% Olive Drab tones
    val OliveDrab = Color(0xFF4B5828)
    val OliveDrabDeep = Color(0xFF343F1B)
    val OliveDrabSoft = Color(0xFF5E6D36)
    val OnOliveDrab = Color(0xFFF7EFE2)
    val OnOliveMuted = Color(0xFFD9CFB9)

    // 10% Signal Yellow accents
    val SignalYellow = Color(0xFFF5B700)
    val SignalYellowBright = Color(0xFFFFD02B)
    val SignalYellowSoft = Color(0xFFFFE58A)
    val OnSignalYellow = Color(0xFF19170B)

    // Semantic status harmonization
    val StatusSafe = Color(0xFF3B6B28)
    val StatusWarn = Color(0xFFB35F00)
    val StatusDanger = Color(0xFFA8241C)
    val OnDanger = Color(0xFFFFF8F6)
}

@Immutable
data class TacticalTokens(
    val desertSand: Color = CleanBotPalette.DesertSand,
    val desertSandCard: Color = CleanBotPalette.DesertSandBright,
    val desertSandElevated: Color = CleanBotPalette.DesertSandLow,
    val sandInk: Color = CleanBotPalette.SandInk,
    val sandMutedInk: Color = CleanBotPalette.SandMutedInk,
    val sandBorder: Color = CleanBotPalette.SandBorder,
    val oliveDrab: Color = CleanBotPalette.OliveDrab,
    val oliveDrabDeep: Color = CleanBotPalette.OliveDrabDeep,
    val oliveDrabSoft: Color = CleanBotPalette.OliveDrabSoft,
    val onOliveDrab: Color = CleanBotPalette.OnOliveDrab,
    val onOliveMuted: Color = CleanBotPalette.OnOliveMuted,
    val signalYellow: Color = CleanBotPalette.SignalYellow,
    val signalYellowBright: Color = CleanBotPalette.SignalYellowBright,
    val signalYellowSoft: Color = CleanBotPalette.SignalYellowSoft,
    val onSignalYellow: Color = CleanBotPalette.OnSignalYellow,
    val statusSafe: Color = CleanBotPalette.StatusSafe,
    val statusWarn: Color = CleanBotPalette.StatusWarn,
    val statusDanger: Color = CleanBotPalette.StatusDanger,
)

val LocalCleanBotTokens = staticCompositionLocalOf { TacticalTokens() }
val LocalHapticsEnabled = staticCompositionLocalOf { true }

val CleanBotColorScheme: ColorScheme = lightColorScheme(
    primary = CleanBotPalette.OliveDrab,
    onPrimary = CleanBotPalette.OnOliveDrab,
    primaryContainer = CleanBotPalette.SignalYellow,
    onPrimaryContainer = CleanBotPalette.OnSignalYellow,
    inversePrimary = CleanBotPalette.SignalYellowBright,
    secondary = CleanBotPalette.OliveDrabSoft,
    onSecondary = CleanBotPalette.OnOliveDrab,
    secondaryContainer = CleanBotPalette.OliveDrabDeep,
    onSecondaryContainer = CleanBotPalette.OnOliveDrab,
    tertiary = CleanBotPalette.SignalYellow,
    onTertiary = CleanBotPalette.OnSignalYellow,
    tertiaryContainer = CleanBotPalette.SignalYellowSoft,
    onTertiaryContainer = CleanBotPalette.OnSignalYellow,
    background = CleanBotPalette.DesertSand,
    onBackground = CleanBotPalette.SandInk,
    surface = CleanBotPalette.DesertSand,
    onSurface = CleanBotPalette.SandInk,
    surfaceVariant = CleanBotPalette.DesertSandCard,
    onSurfaceVariant = CleanBotPalette.SandMutedInk,
    outline = CleanBotPalette.SandOutline,
    outlineVariant = CleanBotPalette.SandBorder,
    error = CleanBotPalette.StatusDanger,
    onError = CleanBotPalette.OnDanger,
    errorContainer = Color(0xFFF6D4CE),
    onErrorContainer = Color(0xFF470B07),
    surfaceContainerLowest = CleanBotPalette.DesertSandBright,
    surfaceContainerLow = CleanBotPalette.DesertSandLow,
    surfaceContainer = CleanBotPalette.DesertSandCard,
    surfaceContainerHigh = CleanBotPalette.DesertSandHigh,
    surfaceContainerHighest = CleanBotPalette.DesertSandHighest,
)

val CleanBotShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

val CleanBotTypography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CleanBotTheme(
    preferences: UserPreferences = UserPreferences(),
    content: @Composable () -> Unit,
) {
    val tokens = TacticalTokens()
    CompositionLocalProvider(
        LocalCleanBotTokens provides tokens,
        LocalHapticsEnabled provides preferences.hapticsEnabled,
    ) {
        MaterialExpressiveTheme(
            colorScheme = CleanBotColorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = CleanBotShapes,
            typography = CleanBotTypography,
            content = content,
        )
    }
}

// Overload preserved for unit and screenshot tests (always uses the single fixed Desert Sand / Olive Drab / Signal Yellow palette).
@Suppress("UNUSED_PARAMETER")
@Composable
fun CleanBotTheme(
    dark: Boolean = false,
    dynamic: Boolean = false,
    content: @Composable () -> Unit,
) = CleanBotTheme(preferences = UserPreferences(), content = content)

/** Large metric numbers across Advanced, Connect, and Welcome screens (tabular numerals). */
val StatNumber = TextStyle(
    fontSize = 22.sp,
    lineHeight = 26.sp,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum",
)

/** Secondary monospace/tabular numeric labels (CPU %, memory, timings, transfer counts). */
val SmallNumber = TextStyle(
    fontSize = 12.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Medium,
    fontFeatureSettings = "tnum",
)
