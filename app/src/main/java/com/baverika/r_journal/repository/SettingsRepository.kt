package com.baverika.r_journal.repository

import android.content.Context
import android.content.SharedPreferences
import com.baverika.r_journal.ui.theme.AppTheme

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("r_journal_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_APP_THEME = "app_theme"
    }

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()
        }

    var appTheme: AppTheme
        get() {
            val themeName = prefs.getString(KEY_APP_THEME, AppTheme.MIDNIGHT.name) ?: AppTheme.MIDNIGHT.name
            return try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.MIDNIGHT
            }
        }
        set(value) {
            prefs.edit().putString(KEY_APP_THEME, value.name).apply()
        }
}
