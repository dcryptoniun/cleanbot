// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.ui

import com.gerstudio.cleanbot.data.AppNames
import com.gerstudio.cleanbot.data.CLEAN_YOUR_TV_KNOWN
import com.gerstudio.cleanbot.data.CLEAN_YOUR_TV_PKG
import com.gerstudio.cleanbot.data.SharedLists

/**
 * A launcher Install a home screen offers (restricted exclusively to CleanYourTV Launcher).
 */
data class Recommended(val pkg: String, val name: String, val about: String, val accessibility: Boolean)

/** Only CleanYourTV Launcher is recommended and offered. */
val RECOMMENDED: List<Recommended>
    get() = SharedLists.launchers
        .filter { it.pkg == CLEAN_YOUR_TV_PKG }
        .mapNotNull { l -> l.about?.let { Recommended(l.pkg, l.name, it, l.accessibility) } }
        .ifEmpty {
            listOf(
                Recommended(
                    pkg = CLEAN_YOUR_TV_KNOWN.pkg,
                    name = CLEAN_YOUR_TV_KNOWN.name,
                    about = CLEAN_YOUR_TV_KNOWN.about.orEmpty(),
                    accessibility = CLEAN_YOUR_TV_KNOWN.accessibility,
                )
            )
        }

fun launcherName(pkg: String) = RECOMMENDED.firstOrNull { it.pkg == pkg }?.name ?: AppNames.fallback(pkg)
