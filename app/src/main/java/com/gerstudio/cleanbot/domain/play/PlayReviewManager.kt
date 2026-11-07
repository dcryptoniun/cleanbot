// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.play

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Manages Google Play In-App Review requests compliant with official Google Play Review API guidelines.
 *
 * Rules followed:
 * 1. No rating gatekeepers or pre-prompts asking "Do you love the app?".
 * 2. Only triggered at natural, positive milestones (successful debloat, theme push, or tip).
 * 3. Enforces rate limits locally to avoid unnecessary API invocations.
 * 4. Flow proceeds cleanly regardless of whether Play Store shows the dialog or suppresses it due to quotas.
 */
class PlayReviewManager(
    private val context: Context,
    private val reviewManager: ReviewManager = ReviewManagerFactory.create(context)
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Records a positive user milestone (e.g., successful debloat or theme applied).
     */
    fun recordMilestone() {
        val count = prefs.getInt(KEY_POSITIVE_ACTIONS, 0) + 1
        prefs.edit().putInt(KEY_POSITIVE_ACTIONS, count).apply()
    }

    /**
     * Evaluates whether the app should request an in-app review based on milestone engagement.
     */
    fun shouldPromptReview(): Boolean {
        val actions = prefs.getInt(KEY_POSITIVE_ACTIONS, 0)
        val lastPromptTime = prefs.getLong(KEY_LAST_PROMPT_TIME, 0L)
        val now = System.currentTimeMillis()

        // Require at least 2 positive milestones and minimum 14 days between review requests
        val hasEnoughActions = actions >= 2
        val hasPassedCooldown = (now - lastPromptTime) > MIN_INTERVAL_MS

        return hasEnoughActions && hasPassedCooldown
    }

    /**
     * Requests and launches the Google Play In-App Review flow if eligible or forced.
     * Always calls [onComplete] upon completion or failure.
     */
    fun requestReviewFlow(activity: Activity, force: Boolean = false, onComplete: () -> Unit = {}) {
        if (!force && !shouldPromptReview()) {
            onComplete()
            return
        }

        prefs.edit()
            .putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
            .putInt(KEY_POSITIVE_ACTIONS, 0)
            .apply()

        try {
            val requestTask = reviewManager.requestReviewFlow()
            requestTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo: ReviewInfo = task.result
                    val flowTask = reviewManager.launchReviewFlow(activity, reviewInfo)
                    flowTask.addOnCompleteListener {
                        onComplete()
                    }
                } else {
                    onComplete()
                }
            }
        } catch (_: Exception) {
            onComplete()
        }
    }

    companion object {
        private const val PREFS_NAME = "cleanbot_play_review"
        private const val KEY_POSITIVE_ACTIONS = "positive_actions_count"
        private const val KEY_LAST_PROMPT_TIME = "last_review_prompt_time"
        private const val MIN_INTERVAL_MS = 14L * 24L * 60L * 60L * 1000L // 14 days
    }
}
