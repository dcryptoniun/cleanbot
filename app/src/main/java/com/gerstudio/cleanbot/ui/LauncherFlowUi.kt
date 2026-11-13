// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gerstudio.cleanbot.R
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.CLEAN_YOUR_TV
import com.gerstudio.cleanbot.data.CLEAN_YOUR_TV_VERSION
import com.gerstudio.cleanbot.data.Device
import com.gerstudio.cleanbot.data.isFireOs
import com.gerstudio.cleanbot.data.resolve
import com.gerstudio.cleanbot.ui.theme.CleanBotPalette
import com.gerstudio.cleanbot.ui.theme.OliveHeroCard
import com.gerstudio.cleanbot.ui.theme.SandCard
import com.gerstudio.cleanbot.ui.theme.SandOutlinedButton
import com.gerstudio.cleanbot.ui.theme.SignalButton
import com.gerstudio.cleanbot.ui.theme.SignalProgressBar
import com.gerstudio.cleanbot.ui.theme.TacticalPulseBeacon
import com.gerstudio.cleanbot.ui.theme.rememberCleanBotHaptics

const val PLAY_STORE = "com.android.vending"

internal fun hasPlayStore(apps: List<AppInfo>) = apps.any { it.packageName == PLAY_STORE && it.enabled }

internal fun hasAddedLauncher(apps: List<AppInfo>) =
    apps.any { it.listedLauncher || (it.removable && (it.home || it.answersHome)) }

internal fun fireInstall(device: Device, apps: List<AppInfo>, recommended: List<Recommended> = RECOMMENDED) =
    isFireOs(device, apps) && recommended.any { it.pkg == CLEAN_YOUR_TV }

internal fun offerChoose(apps: List<AppInfo>, fire: Boolean = false) =
    (hasPlayStore(apps) || fire) && !hasAddedLauncher(apps)

internal fun offerLauncher(apps: List<AppInfo>, hidden: Boolean, fire: Boolean = false) =
    !hidden && offerChoose(apps, fire) && apps.none { it.homeInstead != null || it.homeBack != null }

internal fun downloadable(pkg: String) = pkg == CLEAN_YOUR_TV

internal fun installedLauncher(apps: List<AppInfo>, pkg: String) =
    apps.firstOrNull { it.packageName == pkg && it.enabled }

internal fun otherLaunchers(apps: List<AppInfo>) =
    apps.filter { a -> a.enabled && a.answersHome && !a.builtInLauncher && RECOMMENDED.none { it.pkg == a.packageName } }

data class LauncherActions(
    val close: () -> Unit = {},
    val installBundled: (enableKeyServices: Boolean) -> Unit = {},
    val pickApk: () -> Unit = {},
    val dismissInstall: () -> Unit = {},
    val switch: (service: Boolean) -> Unit = {},
    val makeHome: (pkg: String, service: Boolean) -> Unit = { _, _ -> },
    val open: (String) -> Unit = {},
    val later: () -> Unit = {},
)

@Composable
fun LauncherCard(current: String?, onNotNow: () -> Unit, onChoose: () -> Unit, fire: Boolean = false) {
    HomeNoteCard(
        icon = Icons.Outlined.Home,
        title = Copy.BETTER_HOME.resolve(),
        text = Copy.betterHomeText(current, fire).resolve(),
        dismissLabel = Copy.NOT_NOW.resolve(),
        onDismiss = onNotNow,
        actionLabel = Copy.INSTALL_CLEAN_YOUR_TV.resolve(),
        onAction = onChoose,
    )
}

/**
 * Dead-simple 1-tap CleanYourTV Launcher v3.4 installer & Home switcher screen.
 * Installs directly from the verified bundled `assets/tvhome.apk` across Android TV, Google TV,
 * and Fire OS, with key-service setup (`HomeKeyService` & `InputKeyService`) and instant Home swap.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(
    device: Device,
    flow: LauncherFlow,
    apps: List<AppInfo>,
    actions: LauncherActions,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
) {
    val haptics = rememberCleanBotHaptics()
    var enableKeyServices by rememberSaveable { mutableStateOf(true) }
    var askService by rememberSaveable(flow.step) { mutableStateOf(false) }

    val cleanYourTvApp = installedLauncher(apps, CLEAN_YOUR_TV)
    val currentHome = apps.firstOrNull { it.home }
    val scrollState = rememberScrollState()
    val isInstalling = flow.step == LauncherFlow.Step.Installing

    if (askService && cleanYourTvApp != null) {
        ServiceDialog(
            app = cleanYourTvApp.label,
            other = currentHome?.label ?: Copy.TV_OWN_LAUNCHER.resolve(),
            onTurnOn = { askService = false; actions.switch(true) },
            dismissLabel = Copy.SWITCH_WITHOUT.resolve(),
            onOther = { askService = false; actions.switch(false) },
            onDismiss = { askService = false },
        )
    }

    Scaffold(
        containerColor = CleanBotPalette.DesertSand,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanBotPalette.DesertSand,
                    titleContentColor = CleanBotPalette.SandInk,
                ),
                title = {
                    Text(
                        text = Copy.INSTALL_CLEAN_YOUR_TV.resolve(),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            haptics.tap()
                            actions.close()
                        },
                        enabled = !isInstalling,
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.launcher_close),
                            tint = CleanBotPalette.SandInk,
                        )
                    }
                },
            )
        },
        bottomBar = {
            Surface(color = CleanBotPalette.DesertSand) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (flow.step) {
                        LauncherFlow.Step.Pick -> {
                            if (cleanYourTvApp != null && !cleanYourTvApp.home) {
                                SandOutlinedButton(
                                    onClick = { actions.open(CLEAN_YOUR_TV) },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text("Open Preview")
                                }
                                SignalButton(
                                    onClick = {
                                        if (cleanYourTvApp.homeService) askService = true
                                        else actions.makeHome(CLEAN_YOUR_TV, enableKeyServices)
                                    },
                                    modifier = Modifier.weight(1.3f),
                                ) {
                                    Icon(Icons.Default.Home, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Set as Home")
                                }
                            } else {
                                SandOutlinedButton(
                                    onClick = actions.pickApk,
                                    modifier = Modifier.weight(0.9f),
                                ) {
                                    Icon(Icons.Default.FolderOpen, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Local APK")
                                }
                                SignalButton(
                                    onClick = { actions.installBundled(enableKeyServices) },
                                    modifier = Modifier.weight(1.4f),
                                ) {
                                    Text("Install CleanYourTV v$CLEAN_YOUR_TV_VERSION")
                                }
                            }
                        }

                        LauncherFlow.Step.Installing -> {
                            Text(
                                text = "Installing CleanYourTV onto ${name(device)}…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CleanBotPalette.SandMutedInk,
                            )
                        }

                        LauncherFlow.Step.Installed -> {
                            TextButton(onClick = actions.later) {
                                Text(Copy.NOT_NOW.resolve(), color = CleanBotPalette.OliveDrab)
                            }
                            SignalButton(
                                onClick = { actions.switch(enableKeyServices) },
                            ) {
                                Icon(Icons.Default.Home, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Activate CleanYourTV Home")
                            }
                        }
                    }
                }
            }
        },
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OliveHeroCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    TacticalPulseBeacon(
                        active = isInstalling || cleanYourTvApp?.home == true,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CleanYourTV Launcher v$CLEAN_YOUR_TV_VERSION",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CleanBotPalette.OnOliveDrab,
                        )
                        Text(
                            text = "Bundled open-source TV home • Zero ads • Fast offline ADB setup",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanBotPalette.DesertSand,
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = flow.step to flow.bundled?.phase,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "launcherInstallPhase",
            ) { (step, phase) ->
                when (step) {
                    LauncherFlow.Step.Installing -> {
                        val bundled = flow.bundled
                        val progressValue = bundled?.progress?.let { p ->
                            p.total?.takeIf { it > 0L }?.let { (p.done.toFloat() / it.toFloat()).coerceIn(0f, 1f) }
                        }
                        SandCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = when (phase) {
                                        CleanYourTvInstallState.Phase.Preparing -> "Verifying bundled tvhome.apk SHA-256…"
                                        CleanYourTvInstallState.Phase.Installing -> "Pushing & installing APK over ADB…"
                                        CleanYourTvInstallState.Phase.Configuring -> "Optimizing ART profile & key services…"
                                        else -> "Working…"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CleanBotPalette.OliveDrab,
                                )
                                SignalProgressBar(progress = progressValue ?: 0.5f)
                            }
                        }
                    }

                    LauncherFlow.Step.Installed -> {
                        SandCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CleanBotPalette.OliveDrab,
                                    modifier = Modifier.size(32.dp),
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "CleanYourTV Launcher v$CLEAN_YOUR_TV_VERSION Ready",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Tap Activate below to set it as your TV's default Home screen.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CleanBotPalette.SandMutedInk,
                                    )
                                }
                            }
                        }
                    }

                    LauncherFlow.Step.Pick -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val err = flow.bundled?.error?.resolve()
                            if (phase == CleanYourTvInstallState.Phase.InvalidApk || phase == CleanYourTvInstallState.Phase.Refused) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = MaterialTheme.shapes.large,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                                        Text(
                                            text = err ?: Copy.NOT_CLEAN_YOUR_TV_TEXT.resolve(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                        )
                                    }
                                }
                            }

                            SandCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Icon(Icons.Default.Key, null, tint = CleanBotPalette.OliveDrab)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Enable Home & Inputs Button Guard",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            Text(
                                                text = "Activates HomeKeyService & InputKeyService so your remote Home button always opens CleanYourTV.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CleanBotPalette.SandMutedInk,
                                            )
                                        }
                                        Checkbox(
                                            checked = enableKeyServices,
                                            onCheckedChange = {
                                                haptics.toggle(it)
                                                enableKeyServices = it
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = CleanBotPalette.OliveDrab),
                                        )
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = Copy.SIGNED_CHECK.resolve(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CleanBotPalette.SandMutedInk,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
