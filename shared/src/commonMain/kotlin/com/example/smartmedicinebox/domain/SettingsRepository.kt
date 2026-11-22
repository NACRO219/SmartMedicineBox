package com.example.smartmedicinebox.domain

import com.example.smartmedicinebox.data.AppDatabase
import com.example.smartmedicinebox.data.Setting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Light / dark appearance. [System] follows the phone's setting. */
enum class ThemeMode(val storedValue: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    companion object {
        fun fromStored(value: String?): ThemeMode =
            entries.firstOrNull { it.storedValue == value } ?: System
    }
}

/** App preferences, saved in the database (kept across restarts and backups). */
class SettingsRepository(database: AppDatabase) {

    private val dao = database.settingDao()

    val themeMode: Flow<ThemeMode> =
        dao.observe(KEY_THEME).map(ThemeMode::fromStored)

    suspend fun setThemeMode(mode: ThemeMode) {
        dao.put(Setting(KEY_THEME, mode.storedValue))
    }

    private companion object {
        const val KEY_THEME = "theme"
    }
}
