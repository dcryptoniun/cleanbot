package com.gerstudio.cleanbot.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gerstudio.cleanbot.data.model.PackageActionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cleanbot_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class ColorPalette {
    CLEANBOT_EMERALD,
    DYNAMIC_MATERIAL_YOU,
    AMOLED_PITCH_BLACK,
    CATPPUCCIN_MOCHA,
    DRACULA,
    NORD
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorPalette: ColorPalette = ColorPalette.CLEANBOT_EMERALD,
    val livingRoomAutoDim: Boolean = true,
    val defaultActionMode: PackageActionMode = PackageActionMode.DISABLE,
    val backupApkBeforeUninstall: Boolean = true,
    val keepDataOnUninstall: Boolean = false
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
        val LIVING_ROOM_AUTO_DIM = booleanPreferencesKey("living_room_auto_dim")
        val DEFAULT_ACTION_MODE = stringPreferencesKey("default_action_mode")
        val BACKUP_APK_BEFORE_UNINSTALL = booleanPreferencesKey("backup_apk_before_uninstall")
        val KEEP_DATA_ON_UNINSTALL = booleanPreferencesKey("keep_data_on_uninstall")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val modeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val paletteStr = prefs[Keys.COLOR_PALETTE] ?: ColorPalette.CLEANBOT_EMERALD.name
        val actionStr = prefs[Keys.DEFAULT_ACTION_MODE] ?: PackageActionMode.DISABLE.name

        UserPreferences(
            themeMode = runCatching { ThemeMode.valueOf(modeStr) }.getOrDefault(ThemeMode.SYSTEM),
            colorPalette = runCatching { ColorPalette.valueOf(paletteStr) }.getOrDefault(ColorPalette.CLEANBOT_EMERALD),
            livingRoomAutoDim = prefs[Keys.LIVING_ROOM_AUTO_DIM] ?: true,
            defaultActionMode = runCatching { PackageActionMode.valueOf(actionStr) }.getOrDefault(PackageActionMode.DISABLE),
            backupApkBeforeUninstall = prefs[Keys.BACKUP_APK_BEFORE_UNINSTALL] ?: true,
            keepDataOnUninstall = prefs[Keys.KEEP_DATA_ON_UNINSTALL] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setColorPalette(palette: ColorPalette) {
        context.dataStore.edit { it[Keys.COLOR_PALETTE] = palette.name }
    }

    suspend fun setLivingRoomAutoDim(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LIVING_ROOM_AUTO_DIM] = enabled }
    }

    suspend fun setDefaultActionMode(mode: PackageActionMode) {
        context.dataStore.edit { it[Keys.DEFAULT_ACTION_MODE] = mode.name }
    }

    suspend fun setBackupApkBeforeUninstall(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BACKUP_APK_BEFORE_UNINSTALL] = enabled }
    }

    suspend fun setKeepDataOnUninstall(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_DATA_ON_UNINSTALL] = enabled }
    }
}
