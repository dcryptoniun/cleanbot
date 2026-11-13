// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data.model

import androidx.compose.ui.graphics.Color
import org.json.JSONObject

/**
 * Dock layout styling mapped onto CleanYourTV v3.4 (`Config.java`) properties.
 */
enum class TvDockStyle(val label: String, val description: String) {
    GLASS_FROST("Frosted Glass", "Translucent frosted dock with ambient blur and soft glow"),
    FLOATING_PILL("Floating Pill", "Clean solid dock band with rounded focus highlight"),
    OLED_MINIMAL("OLED Minimal", "True pitch-black background with borderless floating tiles"),
    MODERN_OUTLINE("Rim Focus", "Sharp rim-only highlight contour with focused app glow"),
}

/**
 * Clock presentation supported by CleanYourTV v3.4 (`ClockFormat.java`).
 */
enum class TvClockStyle(val label: String, val configValue: String, val idleClockMinutes: Int) {
    TWELVE_HOUR("12-Hour (AM/PM)", "12", 5),
    TWENTY_FOUR_HOUR("24-Hour Clock", "24", 5),
    SYSTEM_DEFAULT("Follow TV Clock", "system", 3),
}

/**
 * A curated TV UI Theme targeting CleanYourTV Launcher (`com.gathin.tvhome` v3.4).
 * All emitted keys correspond directly to `Config.java` in `Gathin23/cleanyourtv`.
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
    val columns: Int = 5,
    val rowsBelowDock: Int = 1,
    val skyIntensity: Int = 0,
    val appLibraryPill: Boolean = true,
    val tileMenu: Boolean = true,
    val isOledOptimized: Boolean = false,
) {
    /**
     * Serializes this theme into valid `config.json` read by CleanYourTV `Config.java` / `ThemeProvider.java`.
     */
    fun toCleanYourTvJson(): String {
        val bgHex = if (isOledOptimized) "#000000" else String.format("#%06X", 0xFFFFFF and backgroundColor.value.toInt())
        val opacityPct = (dockOpacity * 100f).toInt().coerceIn(30, 100)
        val glassStyle = when (dockStyle) {
            TvDockStyle.MODERN_OUTLINE -> "rim"
            TvDockStyle.FLOATING_PILL -> "halo"
            else -> "glass"
        }
        return JSONObject().apply {
            put("background_color", bgHex)
            put("sky_intensity", skyIntensity.coerceIn(0, 100))
            put("columns", columns.coerceIn(1, 8))
            put("rows_below_dock", rowsBelowDock.coerceIn(0, 3))
            put("dock_row", true)
            put("dock_title", true)
            put("dock_glass", dockStyle == TvDockStyle.GLASS_FROST || dockStyle == TvDockStyle.FLOATING_PILL)
            put("dock_band", dockStyle != TvDockStyle.OLED_MINIMAL)
            put("dock_even_padding", dockStyle == TvDockStyle.FLOATING_PILL)
            put("dock_opacity", opacityPct)
            put("cinematic_home", !isOledOptimized && dockStyle == TvDockStyle.GLASS_FROST)
            put("ambient_glow", !isOledOptimized)
            put("app_glow", dockStyle == TvDockStyle.MODERN_OUTLINE)
            put("glow_style", if (dockStyle == TvDockStyle.FLOATING_PILL) "horizon" else "radial")
            put("glass_style", glassStyle)
            put("icon_only_buttons", true)
            put("always_show_names", false)
            put("uniform_tiles", false)
            put("app_library", appLibraryPill)
            put("tile_menu", tileMenu)
            put("clock_format", clockStyle.configValue)
            put("home_idle_clock_minutes", clockStyle.idleClockMinutes)
            put("animations", true)
            put("focus_overshoot", !isOledOptimized)
            put("reduced_motion", false)
            put("show_greeting", true)
        }.toString(2)
    }

    companion object {
        val CURATED_THEMES: List<TvTheme> = listOf(
            TvTheme(
                id = "cLeanBot_tactical",
                name = "Desert & Olive Signature",
                subtitle = "cLeanBot Tactical Edition",
                description = "Warm olive twilight canvas with signal-gold focus glow, 5-column glass dock, and idle clock screensaver.",
                primaryAccent = Color(0xFFF5B700),
                secondaryAccent = Color(0xFF5E6D36),
                backgroundColor = Color(0xFF1A1F12),
                gradientColors = listOf(Color(0xFF232B17), Color(0xFF343F1B), Color(0xFF1A1F12)),
                dockStyle = TvDockStyle.GLASS_FROST,
                dockOpacity = 0.85f,
                columns = 5,
                rowsBelowDock = 1,
                skyIntensity = 45,
            ),
            TvTheme(
                id = "aurora",
                name = "CleanYourTV Aurora",
                subtitle = "Upstream Official Preset",
                description = "Deep navy horizon with subtle evening sky gradient, 6-column frosted dock, and atmospheric glow.",
                primaryAccent = Color(0xFF00E5FF),
                secondaryAccent = Color(0xFF4F8FF7),
                backgroundColor = Color(0xFF06141D),
                gradientColors = listOf(Color(0xFF051B2C), Color(0xFF0A2A3B), Color(0xFF06141D)),
                dockStyle = TvDockStyle.GLASS_FROST,
                dockOpacity = 0.80f,
                columns = 6,
                rowsBelowDock = 1,
                skyIntensity = 60,
            ),
            TvTheme(
                id = "oled_black",
                name = "OLED Minimal Black",
                subtitle = "Zero Glow & Pure Black",
                description = "True #000000 pixel-off canvas with rim focus highlight and no ambient backdrop fade for OLED TVs.",
                primaryAccent = Color(0xFFF5B700),
                secondaryAccent = Color(0xFF8C9E5E),
                backgroundColor = Color(0xFF000000),
                gradientColors = listOf(Color(0xFF000000), Color(0xFF050505), Color(0xFF000000)),
                dockStyle = TvDockStyle.OLED_MINIMAL,
                dockOpacity = 0.90f,
                columns = 6,
                rowsBelowDock = 0,
                isOledOptimized = true,
            ),
            TvTheme(
                id = "cinematic",
                name = "Cinematic Lounge",
                subtitle = "Hero Artwork Shelf",
                description = "Wide 6-column frosted dock with cinematic hero focus band and 2-minute idle ambient clock.",
                primaryAccent = Color(0xFFFFCF2B),
                secondaryAccent = Color(0xFF7D9248),
                backgroundColor = Color(0xFF0D0E12),
                gradientColors = listOf(Color(0xFF171922), Color(0xFF20232F), Color(0xFF0D0E12)),
                dockStyle = TvDockStyle.FLOATING_PILL,
                dockOpacity = 0.72f,
                columns = 6,
                rowsBelowDock = 1,
                clockStyle = TvClockStyle.SYSTEM_DEFAULT,
            ),
            TvTheme(
                id = "rim_graphite",
                name = "Quiet Rim Focus",
                subtitle = "Distraction-Free Grid",
                description = "Muted graphite background with clean rim-only card outline, 5 columns, and calm zero-overshoot glide.",
                primaryAccent = Color(0xFFE5D7C3),
                secondaryAccent = Color(0xFF9E947E),
                backgroundColor = Color(0xFF141513),
                gradientColors = listOf(Color(0xFF1D1F1B), Color(0xFF262923), Color(0xFF141513)),
                dockStyle = TvDockStyle.MODERN_OUTLINE,
                dockOpacity = 0.78f,
                columns = 5,
                rowsBelowDock = 1,
            ),
            TvTheme(
                id = "ridgelines_dusk",
                name = "Sunset Horizon",
                subtitle = "Warm Evening Sky",
                description = "Warm dusk sky gradient under a 5-column dock built for cozy evening viewing.",
                primaryAccent = Color(0xFFFF9100),
                secondaryAccent = Color(0xFFC27229),
                backgroundColor = Color(0xFF170E0A),
                gradientColors = listOf(Color(0xFF2B1810), Color(0xFF3E2216), Color(0xFF170E0A)),
                dockStyle = TvDockStyle.GLASS_FROST,
                dockOpacity = 0.85f,
                columns = 5,
                rowsBelowDock = 1,
                skyIntensity = 75,
            ),
        )
    }
}
