package com.mustfa.mediaexplorer.settings

import android.content.Context

enum class ThemePreference { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val showHiddenFiles: Boolean = false,
    val autoplayNext: Boolean = true,
    val useFolderCovers: Boolean = true
)

class SettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences("mustfa_settings", Context.MODE_PRIVATE)

    fun read(): AppSettings = AppSettings(
        theme = runCatching { ThemePreference.valueOf(preferences.getString("theme", ThemePreference.SYSTEM.name)!!) }.getOrDefault(ThemePreference.SYSTEM),
        showHiddenFiles = preferences.getBoolean("show_hidden", false),
        autoplayNext = preferences.getBoolean("autoplay_next", true),
        useFolderCovers = preferences.getBoolean("folder_covers", true)
    )

    fun setTheme(value: ThemePreference) { preferences.edit().putString("theme", value.name).apply() }
    fun setShowHiddenFiles(value: Boolean) { preferences.edit().putBoolean("show_hidden", value).apply() }
    fun setAutoplayNext(value: Boolean) { preferences.edit().putBoolean("autoplay_next", value).apply() }
    fun setUseFolderCovers(value: Boolean) { preferences.edit().putBoolean("folder_covers", value).apply() }
}
