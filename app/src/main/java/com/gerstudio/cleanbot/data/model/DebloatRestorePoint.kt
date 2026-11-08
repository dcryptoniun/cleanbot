// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data.model

/**
 * State snapshot recorded before executing any debloat action.
 * Allows instant, 1-tap undo to restore all disabled system packages back to active state.
 */
data class DebloatRestorePoint(
    val id: String,
    val timestamp: Long,
    val tvBrand: String,
    val presetName: String,
    val disabledPackages: List<String>,
    val active: Boolean = true
)
