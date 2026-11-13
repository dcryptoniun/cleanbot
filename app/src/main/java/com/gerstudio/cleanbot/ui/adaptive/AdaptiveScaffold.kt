// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.CleanBotTheme
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

/**
 * Multi-form-factor preview annotation covering phone, foldable, tablet, and desktop windows.
 */
@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Foldable", device = Devices.FOLDABLE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Preview(name = "Desktop", device = Devices.DESKTOP, showBackground = true)
annotation class FormFactorPreviews

enum class AppDestination(val label: String, val icon: ImageVector) {
    CleanTv("Clean", Icons.Default.AutoFixHigh),
    Apps("Apps", Icons.Default.Apps),
    Remote("Remote", Icons.Default.SettingsRemote),
    Tools("Toolkit", Icons.Default.Tune),
}

@Composable
fun CleanBotAdaptiveScaffold(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    isNavBarVisible: Boolean = true,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    val haptics = rememberCleanBotHaptics()
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
    val scaffoldVisibilityState = rememberNavigationSuiteScaffoldState()

    LaunchedEffect(isNavBarVisible) {
        if (isNavBarVisible) scaffoldVisibilityState.show() else scaffoldVisibilityState.hide()
    }

    val itemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = CleanBotPalette.OnSignalYellow,
            selectedTextColor = CleanBotPalette.SandInk,
            indicatorColor = CleanBotPalette.SignalYellow,
            unselectedIconColor = CleanBotPalette.SandMutedInk,
            unselectedTextColor = CleanBotPalette.SandMutedInk,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = CleanBotPalette.OnSignalYellow,
            selectedTextColor = CleanBotPalette.SandInk,
            indicatorColor = CleanBotPalette.SignalYellow,
            unselectedIconColor = CleanBotPalette.SandMutedInk,
            unselectedTextColor = CleanBotPalette.SandMutedInk,
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = CleanBotPalette.SignalYellow,
            selectedIconColor = CleanBotPalette.OnSignalYellow,
            selectedTextColor = CleanBotPalette.SandInk,
            unselectedIconColor = CleanBotPalette.SandMutedInk,
            unselectedTextColor = CleanBotPalette.SandMutedInk,
        ),
    )

    NavigationSuiteScaffold(
        layoutType = layoutType,
        state = scaffoldVisibilityState,
        containerColor = CleanBotPalette.DesertSand,
        contentColor = CleanBotPalette.SandInk,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = CleanBotPalette.DesertSandBright,
            navigationBarContentColor = CleanBotPalette.SandInk,
            navigationRailContainerColor = CleanBotPalette.DesertSandBright,
            navigationRailContentColor = CleanBotPalette.SandInk,
            navigationDrawerContainerColor = CleanBotPalette.DesertSandBright,
            navigationDrawerContentColor = CleanBotPalette.SandInk,
        ),
        navigationSuiteItems = {
            AppDestination.entries.forEach { destination ->
                val selected = currentDestination == destination
                item(
                    selected = selected,
                    onClick = {
                        haptics.tap()
                        onNavigate(destination)
                    },
                    colors = itemColors,
                    icon = { Icon(destination.icon, contentDescription = destination.label) },
                    label = {
                        Text(
                            text = destination.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    },
                    alwaysShowLabel = true,
                )
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content(Modifier.fillMaxSize())
            snackbarHost()
        }
    }
}

@FormFactorPreviews
@Composable
private fun CleanBotAdaptiveScaffoldPreview() {
    CleanBotTheme {
        CleanBotAdaptiveScaffold(
            currentDestination = AppDestination.CleanTv,
            onNavigate = {},
        ) {
            Box(Modifier.fillMaxSize())
        }
    }
}
