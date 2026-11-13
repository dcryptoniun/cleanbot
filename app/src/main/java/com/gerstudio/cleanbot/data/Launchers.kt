// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** CleanYourTV launcher package from Gathin23/cleanyourtv. */
const val CLEAN_YOUR_TV_PKG = "com.gathin.tvhome"

/** Built-in fallback entry for CleanYourTV Launcher v3.4 so the picker always has it offline or online. */
val CLEAN_YOUR_TV_KNOWN = KnownLauncher(
    pkg = CLEAN_YOUR_TV_PKG,
    name = "CleanYourTV Launcher",
    about = "Open-source, ad-free Android TV home from cleanyourtv with customizable glass themes and quick inputs.",
)

/**
 * Launchers that keep Home with their own accessibility service (for TVs whose hardware Home/Input
 * buttons bypass default Home selection). Only CleanYourTV Launcher (`com.gathin.tvhome`) is permitted.
 */
val ACCESSIBILITY_LAUNCHERS = mapOf(
    CLEAN_YOUR_TV_PKG to "3.4",
)

/**
 * A third-party launcher cLeanBot knows, from `launchers.json`. [accessibility] is whether it is
 * one of [ACCESSIBILITY_LAUNCHERS], never what the list says. [about] is the line Install a home
 * screen shows; a launcher without one isn't offered there.
 */
data class KnownLauncher(
    val pkg: String,
    val name: String,
    val about: String? = null,
) {
    val accessibility get() = pkg in ACCESSIBILITY_LAUNCHERS
}

/** Empty unless it is schema 1; entries that can't be read or aren't package names are dropped. */
fun parseLaunchers(text: String): List<KnownLauncher> = try {
    val o = jsonObject(text)
    if (o.optInt("schema") != 1) emptyList() else o.getJSONArray("launchers").objectsOrSkip { l ->
        KnownLauncher(
            pkg = l.getString("package"),
            name = plainText(l.getString("name"), NAME_MAX),
            about = l.optStringOrNull("about")?.let { plainText(it, NOTE_MAX) }?.ifEmpty { null },
        ).takeIf { PACKAGE.matches(it.pkg) && it.name.isNotEmpty() }
    }
} catch (_: org.json.JSONException) {
    emptyList()
}

/**
 * Package to display name, from `labels.json`; its groups are only there for
 * people reading the file. Empty unless it is schema 1; names under something that isn't a package
 * are dropped.
 */
fun parseLabels(text: String): Map<String, String> = try {
    val o = jsonObject(text)
    if (o.optInt("schema") != 1) emptyMap() else buildMap {
        o.getJSONArray("groups").objectsOrSkip { it.getJSONObject("labels") }.forEach { labels ->
            for (pkg in labels.keys()) {
                if (pkg != "android" && !PACKAGE.matches(pkg)) continue
                labels.optStringOrNull(pkg)?.let { plainText(it, NAME_MAX) }?.ifEmpty { null }?.let { put(pkg, it) }
            }
        }
    }
} catch (_: org.json.JSONException) {
    emptyMap()
}

/** The launcher, app name and streaming lists, as [SharedLists] holds them. */
data class Shared(
    val launchers: List<KnownLauncher> = emptyList(),
    val labels: Map<String, String> = emptyMap(),
    val streaming: List<StreamingService> = emptyList(),
)

/**
 * The lists anyone can change without an app release, from the list source next to the debloat
 * lists ([DeviceLists]): the last cached copy at launch, then the source's. A list the source
 * can't give keeps what was there. The app bundles none, so before the first fetch they are empty;
 * a build with `fixtures/` among its Java resources (the tests) starts from those instead.
 */
object SharedLists {
    private val _state = MutableStateFlow(seed())
    val state: StateFlow<Shared> = _state.asStateFlow()
    val launchers get() = _state.value.launchers
    val labels get() = _state.value.labels
    val streaming get() = _state.value.streaming

    @Volatile private var source: DeviceLists? = null

    /** The cached lists from [lists], then the source's; [fresh] drops the old ones first, for a new source. */
    suspend fun load(lists: DeviceLists, fresh: Boolean = false) {
        source = lists
        if (fresh) _state.value = seed() else apply(lists.cachedShared())
        apply(lists.shared())
    }

    /** The source's lists again; one already downloaded is read from the cache, so this is cheap once online. */
    suspend fun refresh() {
        source?.let { apply(it.shared()) }
    }

    /** A logo's PNG, at four times its size in dp; null until it can be had. */
    suspend fun logo(name: String): ByteArray? = source?.logo(name)

    private fun apply(f: SharedFiles) {
        _state.update { now ->
            // Enforce CleanYourTV-only launcher policy even if an upstream remote list includes other launchers.
            val filteredLaunchers = f.launchers
                ?.let(::parseLaunchers)
                ?.filter { it.pkg == CLEAN_YOUR_TV_PKG }
                ?.ifEmpty { null }
            Shared(
                launchers = filteredLaunchers ?: now.launchers.ifEmpty { listOf(CLEAN_YOUR_TV_KNOWN) },
                labels = f.labels?.let(::parseLabels)?.ifEmpty { null } ?: now.labels,
                streaming = f.streaming?.let(::parseStreaming)?.ifEmpty { null } ?: now.streaming,
            )
        }
    }

    private fun seed() = Shared(
        launchers = resource("/fixtures/launchers.json")
            ?.let { parseLaunchers(it.decodeToString()) }
            ?.filter { it.pkg == CLEAN_YOUR_TV_PKG }
            ?.ifEmpty { null }
            ?: listOf(CLEAN_YOUR_TV_KNOWN),
        labels = resource("/fixtures/labels.json")?.let { parseLabels(it.decodeToString()) }.orEmpty(),
        streaming = resource("/fixtures/streaming.json")?.let { parseStreaming(it.decodeToString()) }.orEmpty(),
    )

    private fun resource(path: String): ByteArray? =
        SharedLists::class.java.getResourceAsStream(path)?.use { it.readBytes() }
}
