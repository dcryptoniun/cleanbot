// SPDX-License-Identifier: GPL-3.0-only

package com.gerstudio.cleanbot.data

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.gerstudio.cleanbot.adb.AdbSession
import com.gerstudio.cleanbot.adb.ApkInfo
import com.gerstudio.cleanbot.data.model.TvLauncherInfo
import com.gerstudio.cleanbot.domain.engine.MacroManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/** CleanYourTV Launcher package and bundled APK metadata from `Gathin23/cleanyourtv`. */
const val CLEAN_YOUR_TV = TvLauncherInfo.CLEAN_YOUR_TV_PKG
const val CLEAN_YOUR_TV_ACTIVITY = TvLauncherInfo.CLEAN_YOUR_TV_ACTIVITY
const val CLEAN_YOUR_TV_ASSET = TvLauncherInfo.CLEAN_YOUR_TV_ASSET
const val CLEAN_YOUR_TV_SHA256 = TvLauncherInfo.CLEAN_YOUR_TV_SHA256
val CLEAN_YOUR_TV_VERSION = TvLauncherInfo.CLEAN_YOUR_TV.versionName
val CLEAN_YOUR_TV_VERSION_CODE = TvLauncherInfo.CLEAN_YOUR_TV.versionCode
val CLEAN_YOUR_TV_MIN_SDK = TvLauncherInfo.CLEAN_YOUR_TV.minSdk

private const val MAX_CLEAN_YOUR_TV_APK_BYTES = 25L * 1024L * 1024L

/** Thrown when an asset or user-picked APK does not match CleanYourTV Launcher (`com.gathin.tvhome`). */
class NotCleanYourTv(reason: String = "not CleanYourTV Launcher") : IOException(reason)

/** Thrown when the connected TV is below CleanYourTV's minSdk (Android 8.0 / API 26). */
class UnsupportedTvSdkException(val sdk: Int) :
    IOException("CleanYourTV Launcher requires Android 8.0+ (API $CLEAN_YOUR_TV_MIN_SDK), TV has API $sdk")

/** Outcome of `build-and-install.sh` style post-install setup on the connected TV. */
data class CleanYourTvInstallReport(
    val sdkLevel: Int?,
    val launched: Boolean,
    val locationPermissionGranted: Boolean,
    val aotCompiled: Boolean,
    val keyServicesEnabled: Boolean,
)

/**
 * Prepares, validates, and post-configures CleanYourTV Launcher (`com.gathin.tvhome`) from the
 * bundled offline APK (`assets/tvhome.apk`), or an optional local APK selected by the user.
 */
interface CleanYourTvInstaller {
    /** Extracts `assets/tvhome.apk` into app cache and verifies its SHA-256 checksum. */
    suspend fun prepareBundled(): PickedApp

    /** Reads a user-picked APK and verifies its package identity is `com.gathin.tvhome`. */
    suspend fun openPicked(uri: Uri): PickedApp

    /**
     * Checks TV SDK level (`>= 26`), runs warm-start (`am start -W`), location grant, AOT speed
     * compile, and optionally enables `HomeKeyService` / `InputKeyService` accessibility services.
     */
    suspend fun verifyAndPostSetup(
        session: AdbSession?,
        enableKeyServices: Boolean,
    ): CleanYourTvInstallReport

    /** Ensures connected TV meets API 26 before pushing `tvhome.apk`. */
    suspend fun checkTvCompatibility(session: AdbSession?)
}

class BundledCleanYourTvInstaller(context: Context) : CleanYourTvInstaller {
    private val ctx = context.applicationContext
    private val cacheDir get() = File(ctx.cacheDir, "cleanyourtv")

    override suspend fun prepareBundled(): PickedApp = withContext(Dispatchers.IO) {
        cacheDir.mkdirs()
        val outFile = File(cacheDir, CLEAN_YOUR_TV_ASSET)
        val digest = MessageDigest.getInstance("SHA-256")
        ctx.assets.open(CLEAN_YOUR_TV_ASSET).use { input ->
            outFile.outputStream().use { out ->
                val buf = ByteArray(32 * 1024)
                while (true) {
                    ensureActive()
                    val n = input.read(buf)
                    if (n < 0) break
                    digest.update(buf, 0, n)
                    out.write(buf, 0, n)
                }
            }
        }
        val actualSha = digest.digest().joinToString("") { "%02x".format(it) }
        if (!actualSha.equals(CLEAN_YOUR_TV_SHA256, ignoreCase = true)) {
            outFile.delete()
            throw NotCleanYourTv("Bundled tvhome.apk checksum mismatch: $actualSha")
        }
        readAndValidateArchive(outFile, expectedVersion = CLEAN_YOUR_TV_VERSION, expectedCode = CLEAN_YOUR_TV_VERSION_CODE.toLong())
    }

    override suspend fun openPicked(uri: Uri): PickedApp = withContext(Dispatchers.IO) {
        cacheDir.mkdirs()
        val outFile = File(cacheDir, "picked-$CLEAN_YOUR_TV_ASSET")
        val stream = ctx.contentResolver.openInputStream(uri) ?: throw IOException("Cannot read $uri")
        stream.use { input ->
            outFile.outputStream().use { out ->
                val buf = ByteArray(32 * 1024)
                var total = 0L
                while (true) {
                    ensureActive()
                    val n = input.read(buf)
                    if (n < 0) break
                    total += n
                    if (total > MAX_CLEAN_YOUR_TV_APK_BYTES) {
                        outFile.delete()
                        throw NotCleanYourTv("APK exceeds maximum expected size")
                    }
                    out.write(buf, 0, n)
                }
            }
        }
        readAndValidateArchive(outFile, expectedVersion = null, expectedCode = null)
    }

    override suspend fun checkTvCompatibility(session: AdbSession?) {
        if (session == null) return
        withContext(Dispatchers.IO) {
            val rawSdk = runCatching { session.shell("getprop ro.build.version.sdk").output.trim() }.getOrNull()
            val sdk = rawSdk?.toIntOrNull()
            if (sdk != null && sdk < CLEAN_YOUR_TV_MIN_SDK) {
                throw UnsupportedTvSdkException(sdk)
            }
        }
    }

    override suspend fun verifyAndPostSetup(
        session: AdbSession?,
        enableKeyServices: Boolean,
    ): CleanYourTvInstallReport = withContext(Dispatchers.IO) {
        if (session == null) {
            return@withContext CleanYourTvInstallReport(
                sdkLevel = null,
                launched = false,
                locationPermissionGranted = false,
                aotCompiled = false,
                keyServicesEnabled = false,
            )
        }
        val sdk = runCatching { session.shell("getprop ro.build.version.sdk").output.trim().toIntOrNull() }.getOrNull()
        val launchRes = runCatching {
            session.shell("am start -W -n $CLEAN_YOUR_TV_ACTIVITY 2>&1").output
        }.getOrDefault("")
        val launched = !launchRes.contains("Error", ignoreCase = true) &&
            !launchRes.contains("Exception", ignoreCase = true)

        val locRes = runCatching {
            session.shell("pm grant $CLEAN_YOUR_TV android.permission.ACCESS_FINE_LOCATION 2>&1").output
        }.getOrDefault("")
        val locGranted = locRes.isBlank() || !locRes.contains("Exception", ignoreCase = true)

        val aotCompiled = if (sdk == null || sdk >= 24) {
            runCatching {
                val out = session.shell("cmd package compile -m speed -f $CLEAN_YOUR_TV 2>&1").output
                out.contains("Success", ignoreCase = true) || out.isBlank()
            }.getOrDefault(false)
        } else {
            false
        }

        val keyServicesOk = if (enableKeyServices) {
            runCatching {
                MacroManager(session).enableCleanYourTvKeyServices(homeKey = true, inputKey = true).isSuccess
            }.getOrDefault(false)
        } else {
            false
        }

        CleanYourTvInstallReport(
            sdkLevel = sdk,
            launched = launched,
            locationPermissionGranted = locGranted,
            aotCompiled = aotCompiled,
            keyServicesEnabled = keyServicesOk,
        )
    }

    private fun readAndValidateArchive(
        file: File,
        expectedVersion: String?,
        expectedCode: Long?,
    ): PickedApp {
        val pm = ctx.packageManager
        val pkg = if (Build.VERSION.SDK_INT >= 28) {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(file.path, PackageManager.GET_SIGNING_CERTIFICATES)
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(file.path, PackageManager.GET_SIGNATURES)
        } ?: throw NotCleanYourTv("Unreadable Android package")

        if (pkg.packageName != CLEAN_YOUR_TV) {
            file.delete()
            throw NotCleanYourTv("Expected $CLEAN_YOUR_TV, got ${pkg.packageName}")
        }
        val signers = if (Build.VERSION.SDK_INT >= 28) {
            pkg.signingInfo?.let {
                if (it.hasMultipleSigners()) it.apkContentsSigners.toList() else it.signingCertificateHistory.takeLast(1)
            }
        } else {
            @Suppress("DEPRECATION")
            pkg.signatures?.toList()
        }.orEmpty()
        if (signers.isEmpty()) {
            file.delete()
            throw NotCleanYourTv("APK has no valid signing certificate")
        }
        val code = expectedCode ?: PackageInfoCompat.getLongVersionCode(pkg)
        val verName = expectedVersion ?: pkg.versionName ?: CLEAN_YOUR_TV_VERSION
        return PickedApp(
            files = listOf(file),
            info = ApkInfo(pkg.packageName, code, verName),
            label = TvLauncherInfo.CLEAN_YOUR_TV.name,
        )
    }
}
