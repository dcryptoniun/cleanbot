// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data.model

import androidx.compose.ui.graphics.Color

/**
 * Dock layout styling for the TV custom launcher.
 */
enum class TvDockStyle(val label: String, val description: String) {
    GLASS_FROST("Frosted Glass", "Translucent frosted dock with soft blur and glowing border"),
    FLOATING_PILL("Floating Pill", "Elevated rounded dock with ambient drop shadow"),
    OLED_MINIMAL("OLED Minimal", "True black borderless dock optimized for OLED panels"),
    MODERN_OUTLINE("Modern Outline", "Clean minimalist stroke outline with vibrant accent")
}

/**
 * Clock presentation on the TV ambient home screen.
 */
enum class TvClockStyle(val label: String) {
    TWELVE_HOUR("12-Hour (AM/PM)"),
    TWENTY_FOUR_HOUR("24-Hour Military"),
    MINIMAL_COMPACT("Minimal Compact"),
    SPLIT_FLAP("Retro Split-Flap")
}

/**
 * A curated TV UI Theme for CleanYourTV and modern Android TV launchers.
 */
data class TvTheme(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val backgroundColor: Color,
    val gradientColors: List<Color>,
    val dockStyle: TvDockStyle = TvDockStyle.GLASS_FROST,
    val dockOpacity: Float = 0.85f,
    val cornerRadiusDp: Int = 18,
    val clockStyle: TvClockStyle = TvClockStyle.TWENTY_FOUR_HOUR,
    val appLibraryPill: Boolean = true,
    val tileMenu: Boolean = true,
    val isOledOptimized: Boolean = false
) {
    /**
     * Serializes this theme into the standard CleanYourTV config.json format
     * compatible with tools/theme.sh and com.gathin.tvhome.
     */
    fun toCleanYourTvJson(): String {
        val pHex = String.format("#%06X", (0xFFFFFF and primaryAccent.value.toInt()))
        val sHex = String.format("#%06X", (0xFFFFFF and secondaryAccent.value.toInt()))
        val bHex = String.format("#%06X", (0xFFFFFF and backgroundColor.value.toInt()))

        return """
        {
          "schema": 1,
          "theme_name": "$id",
          "display_name": "$name",
          "accent_color": "$pHex",
          "secondary_accent": "$sHex",
          "background_tint": "$bHex",
          "dock_style": "${dockStyle.name.lowercase()}",
          "dock_opacity": $dockOpacity,
          "card_radius": $cornerRadiusDp,
          "clock_format": "${if (clockStyle == TvClockStyle.TWELVE_HOUR) "12h" else "24h"}",
          "tile_menu": $tileMenu,
          "app_library": $appLibraryPill,
          "oled_optimized": $isOledOptimized,
          "live_tv_package": "com.google.android.tv"
        }
        """.trimIndent()
    }

    companion object {
        /**
         * Curated collection of TV themes inspired by CleanYourTV and SHOWCASE.md.
         */
        val CURATED_THEMES: List<TvTheme> = listOf(
            TvTheme(
                id = "aurora",
                name = "Aurora Borealis",
                subtitle = "CleanYourTV Signature",
                description = "Luminescent polar aurora gradient with frosted glass dock and ambient cyan glow.",
                primaryAccent = Color(0xFF00E5FF),
                secondaryAccent = Color(0xFF00B0FF),
                backgroundColor = Color(0xFF06141D),
                gradientColors = listOf(Color(0xFF051B2C), Color(0xFF00363A), Color(0xFF004D40)),
                dockStyle = TvDockStyle.GLASS_FROST,
                dockOpacity = 0.85f,
                cornerRadiusDp = 20,
                isOledOptimized = false
            ),
            TvTheme(
                id = "oled_black",
                name = "OLED Pure Black",
                subtitle = "Zero Light Pollution",
                description = "True pitch-black #000000 background with high-contrast emerald icons. Saves energy on OLED TVs.",
                primaryAccent = Color(0xFF00E676),
                secondaryAccent = Color(0xFF69F0AE),
                backgroundColor = Color(0xFF000000),
                gradientColors = listOf(Color(0xFF000000), Color(0xFF050505), Color(0xFF000000)),
                dockStyle = TvDockStyle.OLED_MINIMAL,
                dockOpacity = 0.95f,
                cornerRadiusDp = 16,
                isOledOptimized = true
            ),
            TvTheme(
                id = "cyberpunk",
                name = "Cyberpunk Neon",
                subtitle = "Night City Horizon",
                description = "Electric neon cyan and hot magenta futuristic aesthetic with high-energy glowing borders.",
                primaryAccent = Color(0xFF00F5FF),
                secondaryAccent = Color(0xFFFF007F),
                backgroundColor = Color(0xFF0A0612),
                gradientColors = listOf(Color(0xFF130924), Color(0xFF280738), Color(0xFF0A0612)),
                dockStyle = TvDockStyle.MODERN_OUTLINE,
                dockOpacity = 0.90f,
                cornerRadiusDp = 14,
                isOledOptimized = false
            ),
            TvTheme(
                id = "nordic_frost",
                name = "Nordic Slate Frost",
                subtitle = "Scandinavian Minimalism",
                description = "Cool arctic blue highlights against muted slate gray stone. Balanced, peaceful, and refined.",
                primaryAccent = Color(0xFF88C0D0),
                secondaryAccent = Color(0xFF81A1C1),
                backgroundColor = Color(0xFF191D24),
                gradientColors = listOf(Color(0xFF242933), Color(0xFF2E3440), Color(0xFF1E222A)),
                dockStyle = TvDockStyle.FLOATING_PILL,
                dockOpacity = 0.88f,
                cornerRadiusDp = 22,
                isOledOptimized = false
            ),
            TvTheme(
                id = "sunset_ember",
                name = "Sunset Ember",
                subtitle = "Warm Living Room Dusk",
                description = "Deep dusk purple blending into golden sunset amber. Cozy and relaxing for evening movie watching.",
                primaryAccent = Color(0xFFFF9100),
                secondaryAccent = Color(0xFFFF5252),
                backgroundColor = Color(0xFF140A10),
                gradientColors = listOf(Color(0xFF260D1E), Color(0xFF381223), Color(0xFF140A10)),
                dockStyle = TvDockStyle.GLASS_FROST,
                dockOpacity = 0.85f,
                cornerRadiusDp = 20,
                isOledOptimized = false
            ),
            TvTheme(
                id = "tokyo_night",
                name = "Tokyo Night",
                subtitle = "Midnight Metro",
                description = "Deep indigo blue inspired by Shinjuku nightlights, accented by electric lavender and teal.",
                primaryAccent = Color(0xFF7AA2F7),
                secondaryAccent = Color(0xFFBB9AF7),
                backgroundColor = Color(0xFF10121C),
                gradientColors = listOf(Color(0xFF16192B), Color(0xFF1F2335), Color(0xFF10121C)),
                dockStyle = TvDockStyle.FLOATING_PILL,
                dockOpacity = 0.90f,
                cornerRadiusDp = 18,
                isOledOptimized = false
            ),
            TvTheme(
                id = "emerald_forest",
                name = "Emerald Forest",
                subtitle = "Calm Nature & Foliage",
                description = "Deep pine green and organic moss highlights. Organic warmth that blends naturally into living rooms.",
                primaryAccent = Color(0xFF26A69A),
                secondaryAccent = Color(0xFF80CBC4),
                backgroundColor = Color(0xFF071410),
                gradientColors = listOf(Color(0xFF0C241D), Color(0xFF13362B), Color(0xFF071410)),
                dockStyle = TvDockStyle.GLASS_FROST,
                dockOpacity = 0.85f,
                cornerRadiusDp = 20,
                isOledOptimized = false
            ),
            TvTheme(
                id = "retro_synthwave",
                name = "Retro Synthwave",
                subtitle = "Outrun 1984",
                description = "Vibrant synthwave sunset with neon violet grid gradients and energetic retro styling.",
                primaryAccent = Color(0xFFFF4081),
                secondaryAccent = Color(0xFF7C4DFF),
                backgroundColor = Color(0xFF120320),
                gradientColors = listOf(Color(0xFF2E0854), Color(0xFF4A148C), Color(0xFF120320)),
                dockStyle = TvDockStyle.MODERN_OUTLINE,
                dockOpacity = 0.90f,
                cornerRadiusDp = 16,
                isOledOptimized = false
            ),
            TvTheme(
                id = "clean_minimal",
                name = "Clean Minimalist",
                subtitle = "Apple TV Style",
                description = "Ultra-clean layout with refined typography, minimal dock, and focused distraction-free tiles.",
                primaryAccent = Color(0xFFE0E0E0),
                secondaryAccent = Color(0xFFBDBDBD),
                backgroundColor = Color(0xFF121212),
                gradientColors = listOf(Color(0xFF1E1E1E), Color(0xFF252525), Color(0xFF121212)),
                dockStyle = TvDockStyle.GLASS_FROST,
                dockOpacity = 0.80f,
                cornerRadiusDp = 24,
                isOledOptimized = false
            )
        )
    }
}
