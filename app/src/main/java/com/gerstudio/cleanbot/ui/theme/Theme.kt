// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gerstudio.cleanbot.data.datastore.ColorPalette
import com.gerstudio.cleanbot.data.datastore.ThemeMode
import com.gerstudio.cleanbot.data.datastore.UserPreferences

// 1. cLeanBot Signature Emerald / Cyan (Light & Dark)
val EmeraldLight = lightColorScheme(
    primary = Color(0xFF00796B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB2DFDB),
    onPrimaryContainer = Color(0xFF00251A),
    inversePrimary = Color(0xFF80CBC4),
    secondary = Color(0xFF00838F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB2EBF2),
    onSecondaryContainer = Color(0xFF00272B),
    tertiary = Color(0xFF00695C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF80CBC4),
    onTertiaryContainer = Color(0xFF001E17),
    background = Color(0xFFF2FBF9),
    onBackground = Color(0xFF101C1A),
    surface = Color(0xFFF2FBF9),
    onSurface = Color(0xFF101C1A),
    surfaceVariant = Color(0xFFD6E8E5),
    onSurfaceVariant = Color(0xFF334A47),
    outline = Color(0xFF6B8682),
    outlineVariant = Color(0xFFB8CECB),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFE4F3F0),
    surfaceContainerHigh = Color(0xFFD9ECE8),
    surfaceContainerHighest = Color(0xFFCCE4E0),
    surfaceContainerLow = Color(0xFFEDF8F6),
    surfaceContainerLowest = Color(0xFFFFFFFF)
)

val EmeraldDark = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFA7FFEB),
    inversePrimary = Color(0xFF00695C),
    secondary = Color(0xFF4DD0E1),
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF004D53),
    onSecondaryContainer = Color(0xFFE0F7FA),
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF00332C),
    tertiaryContainer = Color(0xFF004D44),
    onTertiaryContainer = Color(0xFFB2DFDB),
    background = Color(0xFF081211),
    onBackground = Color(0xFFE0ECE9),
    surface = Color(0xFF081211),
    onSurface = Color(0xFFE0ECE9),
    surfaceVariant = Color(0xFF1C2C2A),
    onSurfaceVariant = Color(0xFFB2C8C5),
    outline = Color(0xFF758F8C),
    outlineVariant = Color(0xFF2B3F3C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    surfaceContainer = Color(0xFF0E1C1A),
    surfaceContainerHigh = Color(0xFF152523),
    surfaceContainerHighest = Color(0xFF1D302D),
    surfaceContainerLow = Color(0xFF0A1615),
    surfaceContainerLowest = Color(0xFF040A09)
)

// 2. True AMOLED Pitch Black
val AmoledPitchBlack = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = Color(0xFF84FFFF),
    inversePrimary = Color(0xFF00838F),
    secondary = Color(0xFF1DE9B6),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF00382E),
    onSecondaryContainer = Color(0xFFA7FFEB),
    tertiary = Color(0xFF64FFDA),
    onTertiary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF121212),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF383838),
    outlineVariant = Color(0xFF222222),
    surfaceContainer = Color(0xFF080808),
    surfaceContainerHigh = Color(0xFF121212),
    surfaceContainerHighest = Color(0xFF1E1E1E),
    surfaceContainerLow = Color(0xFF040404),
    surfaceContainerLowest = Color(0xFF000000)
)

// 3. Catppuccin Mocha
val CatppuccinMocha = darkColorScheme(
    primary = Color(0xFFCBA6F7), // Mauve
    onPrimary = Color(0xFF11111B),
    primaryContainer = Color(0xFF313244),
    onPrimaryContainer = Color(0xFFCDD6F4),
    secondary = Color(0xFF94E2D5), // Teal
    onSecondary = Color(0xFF11111B),
    secondaryContainer = Color(0xFF181825),
    onSecondaryContainer = Color(0xFF89DCEB),
    tertiary = Color(0xFF89B4FA), // Blue
    onTertiary = Color(0xFF11111B),
    background = Color(0xFF1E1E2E), // Base
    onBackground = Color(0xFFCDD6F4), // Text
    surface = Color(0xFF1E1E2E),
    onSurface = Color(0xFFCDD6F4),
    surfaceVariant = Color(0xFF313244), // Surface0
    onSurfaceVariant = Color(0xFFA6ADC8), // Subtext0
    outline = Color(0xFF585B70), // Surface2
    outlineVariant = Color(0xFF45475A), // Surface1
    surfaceContainer = Color(0xFF181825), // Mantle
    surfaceContainerHigh = Color(0xFF313244),
    surfaceContainerHighest = Color(0xFF45475A),
    surfaceContainerLow = Color(0xFF11111B), // Crust
    surfaceContainerLowest = Color(0xFF11111B)
)

// 4. Dracula
val DraculaPalette = darkColorScheme(
    primary = Color(0xFFBD93F9), // Purple
    onPrimary = Color(0xFF282A36),
    primaryContainer = Color(0xFF44475A),
    onPrimaryContainer = Color(0xFFF8F8F2),
    secondary = Color(0xFF8BE9FD), // Cyan
    onSecondary = Color(0xFF282A36),
    secondaryContainer = Color(0xFF383A59),
    onSecondaryContainer = Color(0xFF50FA7B),
    tertiary = Color(0xFFFF79C6), // Pink
    onTertiary = Color(0xFF282A36),
    background = Color(0xFF282A36),
    onBackground = Color(0xFFF8F8F2),
    surface = Color(0xFF282A36),
    onSurface = Color(0xFFF8F8F2),
    surfaceVariant = Color(0xFF44475A),
    onSurfaceVariant = Color(0xFF6272A4), // Comment
    outline = Color(0xFF6272A4),
    outlineVariant = Color(0xFF383A59),
    surfaceContainer = Color(0xFF21222C),
    surfaceContainerHigh = Color(0xFF343746),
    surfaceContainerHighest = Color(0xFF44475A),
    surfaceContainerLow = Color(0xFF191A21),
    surfaceContainerLowest = Color(0xFF14151B)
)

// 5. Nord
val NordPalette = darkColorScheme(
    primary = Color(0xFF88C0D0), // Frost
    onPrimary = Color(0xFF2E3440),
    primaryContainer = Color(0xFF3B4252),
    onPrimaryContainer = Color(0xFFECEFF4),
    secondary = Color(0xFF81A1C1),
    onSecondary = Color(0xFF2E3440),
    secondaryContainer = Color(0xFF434C5E),
    onSecondaryContainer = Color(0xFF8FBCBB),
    tertiary = Color(0xFF5E81AC),
    onTertiary = Color(0xFFECEFF4),
    background = Color(0xFF2E3440), // Polar Night
    onBackground = Color(0xFFECEFF4), // Snow Storm
    surface = Color(0xFF2E3440),
    onSurface = Color(0xFFECEFF4),
    surfaceVariant = Color(0xFF3B4252),
    onSurfaceVariant = Color(0xFFD8DEE9),
    outline = Color(0xFF4C566A),
    outlineVariant = Color(0xFF3B4252),
    surfaceContainer = Color(0xFF272C36),
    surfaceContainerHigh = Color(0xFF353C4A),
    surfaceContainerHighest = Color(0xFF3B4252),
    surfaceContainerLow = Color(0xFF242933),
    surfaceContainerLowest = Color(0xFF1E222A)
)

val StatNumber = TextStyle(fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 34.sp, fontFeatureSettings = "tnum")
val SmallNumber = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontFeatureSettings = "tnum")

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CleanBotTheme(
    preferences: UserPreferences = UserPreferences(),
    forceAmoled: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (preferences.themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme: ColorScheme = when {
        forceAmoled -> AmoledPitchBlack
        preferences.colorPalette == ColorPalette.AMOLED_PITCH_BLACK && isDark -> AmoledPitchBlack
        preferences.colorPalette == ColorPalette.CATPPUCCIN_MOCHA && isDark -> CatppuccinMocha
        preferences.colorPalette == ColorPalette.DRACULA && isDark -> DraculaPalette
        preferences.colorPalette == ColorPalette.NORD && isDark -> NordPalette
        preferences.colorPalette == ColorPalette.DYNAMIC_MATERIAL_YOU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        isDark -> EmeraldDark
        else -> EmeraldLight
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = Typography(),
        content = content
    )
}

// Backward compatibility for existing screens
@Composable
fun CleanBotTheme(
    dark: Boolean = isSystemInDarkTheme(),
    dynamic: Boolean = true,
    content: @Composable () -> Unit
) {
    CleanBotTheme(
        preferences = UserPreferences(
            themeMode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT,
            colorPalette = if (dynamic) ColorPalette.DYNAMIC_MATERIAL_YOU else ColorPalette.CLEANBOT_EMERALD
        ),
        content = content
    )
}
