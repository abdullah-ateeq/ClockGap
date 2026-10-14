package com.example.clockgap.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animationsEnabled: Boolean = true,
    val defaultGapMode: ClockGapMode = ClockGapMode.DIRECT,
    val confirmHistoryDelete: Boolean = true,
    val preserveLastTimes: Boolean = true
)
