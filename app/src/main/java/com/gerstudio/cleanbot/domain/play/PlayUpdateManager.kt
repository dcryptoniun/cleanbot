// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.domain.play

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State representing Google Play In-App Update status.
 */
sealed interface PlayUpdateStatus {
    data object Idle : PlayUpdateStatus
    data class UpdateAvailable(val info: AppUpdateInfo, val isImmediate: Boolean) : PlayUpdateStatus
    data class Downloading(val bytesDownloaded: Long, val totalBytesToDownload: Long) : PlayUpdateStatus
    data object Downloaded : PlayUpdateStatus
    data class Failed(val errorCode: Int) : PlayUpdateStatus
}

/**
 * Manages Google Play In-App Updates adhering to Google Play policies.
 * Supports background flexible updates with user notification when ready to install.
 */
class PlayUpdateManager(
    private val context: Context,
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
) {
    private val _updateStatus = MutableStateFlow<PlayUpdateStatus>(PlayUpdateStatus.Idle)
    val updateStatus: StateFlow<PlayUpdateStatus> = _updateStatus.asStateFlow()

    private val installListener = InstallStateUpdatedListener { state: InstallState ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                _updateStatus.value = PlayUpdateStatus.Downloading(
                    bytesDownloaded = state.bytesDownloaded(),
                    totalBytesToDownload = state.totalBytesToDownload()
                )
            }
            InstallStatus.DOWNLOADED -> {
                _updateStatus.value = PlayUpdateStatus.Downloaded
            }
            InstallStatus.FAILED -> {
                _updateStatus.value = PlayUpdateStatus.Failed(state.installErrorCode())
            }
            InstallStatus.CANCELED -> {
                _updateStatus.value = PlayUpdateStatus.Idle
            }
            else -> Unit
        }
    }

    init {
        appUpdateManager.registerListener(installListener)
    }

    /**
     * Checks if a new version is available on Google Play.
     */
    fun checkForUpdate(onAvailable: ((AppUpdateInfo) -> Unit)? = null) {
        try {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    val isFlexible = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    val isImmediate = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                    if (isFlexible || isImmediate) {
                        _updateStatus.value = PlayUpdateStatus.UpdateAvailable(info, isImmediate = !isFlexible && isImmediate)
                        onAvailable?.invoke(info)
                    }
                } else if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    _updateStatus.value = PlayUpdateStatus.Downloaded
                }
            }
        } catch (_: Exception) {
            // Non-Play Store builds or offline environments fail gracefully
        }
    }

    /**
     * Launches the flexible in-app update flow.
     */
    fun startUpdate(activity: Activity, info: AppUpdateInfo, requestCode: Int = REQUEST_CODE_UPDATE): Boolean {
        return try {
            val updateType = if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                AppUpdateType.FLEXIBLE
            } else {
                AppUpdateType.IMMEDIATE
            }
            val options = AppUpdateOptions.newBuilder(updateType).build()
            appUpdateManager.startUpdateFlowForResult(info, activity, options, requestCode)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Completes the flexible update by restarting the application and installing the new APK.
     */
    fun completeUpdate() {
        try {
            appUpdateManager.completeUpdate()
        } catch (_: Exception) {
            // Silently fallback if impossible
        }
    }

    /**
     * Resumes any in-flight download or downloaded update on activity resume.
     */
    fun onResume(activity: Activity) {
        try {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    _updateStatus.value = PlayUpdateStatus.Downloaded
                } else if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    // Resume immediate update flow
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        REQUEST_CODE_UPDATE
                    )
                }
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    /**
     * Cleans up listeners.
     */
    fun destroy() {
        try {
            appUpdateManager.unregisterListener(installListener)
        } catch (_: Exception) {
            // Ignore
        }
    }

    fun dismissUpdateStatus() {
        _updateStatus.value = PlayUpdateStatus.Idle
    }

    companion object {
        const val REQUEST_CODE_UPDATE = 8821
    }
}
