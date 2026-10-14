package com.example.clockgap.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.clockgap.model.AppSettings
import com.example.clockgap.model.ClockGapMode
import com.example.clockgap.model.ThemeMode
import java.time.LocalTime

class SettingsRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    companion object {
        private const val PREFS_NAME = "clock_gap_settings_prefs"
        private const val KEY_THEME = "setting_theme"
        private const val KEY_DYNAMIC_COLOR = "setting_dynamic_color"
        private const val KEY_HAPTIC_FEEDBACK = "setting_haptic_feedback"
        private const val KEY_ANIMATIONS = "setting_animations"
        private const val KEY_DEFAULT_GAP_MODE = "setting_default_gap_mode"
        private const val KEY_CONFIRM_HISTORY_DELETE = "setting_confirm_history_delete"
        private const val KEY_PRESERVE_LAST_TIMES = "setting_preserve_last_times"
        private const val KEY_LAST_TIME_A = "last_time_a"
        private const val KEY_LAST_TIME_B = "last_time_b"
        private const val KEY_LAST_MODE = "last_mode"

        val DEFAULT_TIME_A: LocalTime = LocalTime.of(9, 0)
        val DEFAULT_TIME_B: LocalTime = LocalTime.of(17, 0)
        val DEFAULT_MODE: ClockGapMode = ClockGapMode.DIRECT
    }

    fun getSettings(): AppSettings {
        val themeStr = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val hapticFeedback = prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true)
        val animationsEnabled = prefs.getBoolean(KEY_ANIMATIONS, true)

        val modeStr = prefs.getString(KEY_DEFAULT_GAP_MODE, ClockGapMode.DIRECT.name) ?: ClockGapMode.DIRECT.name
        val defaultGapMode = try {
            ClockGapMode.valueOf(modeStr)
        } catch (_: Exception) {
            ClockGapMode.DIRECT
        }

        val confirmHistoryDelete = prefs.getBoolean(KEY_CONFIRM_HISTORY_DELETE, true)
        val preserveLastTimes = prefs.getBoolean(KEY_PRESERVE_LAST_TIMES, true)

        return AppSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            hapticFeedback = hapticFeedback,
            animationsEnabled = animationsEnabled,
            defaultGapMode = defaultGapMode,
            confirmHistoryDelete = confirmHistoryDelete,
            preserveLastTimes = preserveLastTimes
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedback)
            .putBoolean(KEY_ANIMATIONS, settings.animationsEnabled)
            .putString(KEY_DEFAULT_GAP_MODE, settings.defaultGapMode.name)
            .putBoolean(KEY_CONFIRM_HISTORY_DELETE, settings.confirmHistoryDelete)
            .putBoolean(KEY_PRESERVE_LAST_TIMES, settings.preserveLastTimes)
            .apply()
    }

    fun resetSettings(): AppSettings {
        val defaultSettings = AppSettings()
        saveSettings(defaultSettings)
        return defaultSettings
    }

    fun saveLastInputs(timeA: LocalTime, timeB: LocalTime, mode: ClockGapMode) {
        prefs.edit()
            .putString(KEY_LAST_TIME_A, timeA.toString())
            .putString(KEY_LAST_TIME_B, timeB.toString())
            .putString(KEY_LAST_MODE, mode.name)
            .apply()
    }

    fun getInitialInputs(settings: AppSettings): Triple<LocalTime, LocalTime, ClockGapMode> {
        if (!settings.preserveLastTimes) {
            return Triple(DEFAULT_TIME_A, DEFAULT_TIME_B, settings.defaultGapMode)
        }

        val timeAStr = prefs.getString(KEY_LAST_TIME_A, null)
        val timeBStr = prefs.getString(KEY_LAST_TIME_B, null)
        val modeStr = prefs.getString(KEY_LAST_MODE, null)

        val timeA = timeAStr?.let {
            try {
                LocalTime.parse(it)
            } catch (_: Exception) {
                null
            }
        } ?: DEFAULT_TIME_A

        val timeB = timeBStr?.let {
            try {
                LocalTime.parse(it)
            } catch (_: Exception) {
                null
            }
        } ?: DEFAULT_TIME_B

        val mode = modeStr?.let {
            try {
                ClockGapMode.valueOf(it)
            } catch (_: Exception) {
                null
            }
        } ?: settings.defaultGapMode

        return Triple(timeA, timeB, mode)
    }
}
