// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gerstudio.cleanbot.data.Action
import com.gerstudio.cleanbot.data.AppInfo
import com.gerstudio.cleanbot.data.DeviceList
import com.gerstudio.cleanbot.data.HistoryEntry
import com.gerstudio.cleanbot.data.Suggested
import com.gerstudio.cleanbot.data.SuggestedGroup
import com.gerstudio.cleanbot.data.Suggestions
import com.gerstudio.cleanbot.ui.AppRow
import com.gerstudio.cleanbot.ui.Copy
import com.gerstudio.cleanbot.ui.DebloatScreen
import com.gerstudio.cleanbot.ui.DeviceActions
import com.gerstudio.cleanbot.ui.DisableDialog
import com.gerstudio.cleanbot.ui.HistoryList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BusyGatingTest {
    @get:Rule val compose = createComposeRule()

    private val pkg = "com.example"
    private val app = AppInfo(pkg, "Example", null, false, true, null, "1.0", removable = true)

    @Test fun appButtonsWaitWhileTheTvIsBusy() {
        compose.setContent { AppRow(app, emptyList(), null, busy = false, actions = DeviceActions(), locked = true) }
        compose.onNodeWithText("Example").performClick()
        compose.onNodeWithText(Copy.button(Action.Disable).text()).assertIsNotEnabled()
        compose.onNodeWithText(Copy.button(Action.Uninstall).text()).assertIsNotEnabled()
    }

    @Test fun offlineRowSaysNothingAndWontOpen() {
        // Away, what the row last heard is stale: no status line, and a tap opens nothing.
        compose.setContent { AppRow(app, emptyList(), null, busy = false, actions = DeviceActions(), offline = true) }
        compose.onNodeWithText("Example").performClick()
        compose.onAllNodesWithText(Copy.button(Action.Disable).text()).assertCountEquals(0)
        compose.onAllNodesWithText("Not running").assertCountEquals(0)
    }

    @Test fun confirmWaitsWhileTheTvIsBusy() {
        // Proposed before a restore started: the explanation stays, its button waits.
        compose.setContent { AppRow(app, emptyList(), pkg to Action.Disable, busy = false, actions = DeviceActions(), locked = true) }
        compose.onNodeWithText(Copy.confirm(Action.Disable).text()).assertIsNotEnabled()
    }

    @Test fun undoAndRestoreWaitWhileTheTvIsBusy() {
        val history = listOf(HistoryEntry(1, "tv", 0L, Action.Disable, pkg, "Example", Action.Enable, null))
        compose.setContent { HistoryList(history, 0L, onUndo = {}, busy = true) }
        compose.onNodeWithText(Copy.undo(Action.Enable).text()).assertIsNotEnabled()
        compose.onNodeWithText("Restore everything (1)").assertIsNotEnabled()
    }

    @Test fun restoreKeepsItsOwnLabel() {
        val history = listOf(HistoryEntry(1, "tv", 0L, Action.Disable, pkg, "Example", Action.Enable, null))
        compose.setContent { HistoryList(history, 0L, onUndo = {}, restoring = true) }
        compose.onNodeWithText("Turning apps back on…").assertIsNotEnabled()
        compose.onNodeWithText(Copy.undo(Action.Enable).text()).assertIsNotEnabled()
    }

    @Test fun idleUndoStillWorks() {
        val history = listOf(HistoryEntry(1, "tv", 0L, Action.Disable, pkg, "Example", Action.Enable, null))
        compose.setContent { HistoryList(history, 0L, onUndo = {}) }
        compose.onNodeWithText(Copy.undo(Action.Enable).text()).assertIsEnabled()
    }

    private val lists = Suggestions.Ready(
        null,
        DeviceList("test-tv", "Test TV", null, null, emptySet(), emptyList()),
        null,
        listOf(SuggestedGroup("Extras", listOf(Suggested(pkg, "Example", null, optional = false, ticked = true)))),
        emptyList(),
    )

    @Test fun debloatWaitsWhileTheTvIsBusy() {
        // A Disable started on the Apps tab is still running when Debloat opens.
        compose.setContent { DebloatScreen(lists, {}, {}, {}, busy = true) }
        compose.onNodeWithText("Disable 1 app").assertIsNotEnabled()
    }

    @Test fun debloatConfirmWaitsWhileTheTvIsBusy() {
        val apps = listOf(Suggested(pkg, "Example", null, optional = false, ticked = true))
        compose.setContent { DisableDialog(apps, onDisable = {}, onDismiss = {}, enabled = false) }
        compose.onNodeWithText("Yes, disable them").assertIsNotEnabled()
    }

    @Test fun idleDebloatStillWorks() {
        compose.setContent { DebloatScreen(lists, {}, {}, {}) }
        compose.onNodeWithText("Disable 1 app").assertIsEnabled()
    }
}
