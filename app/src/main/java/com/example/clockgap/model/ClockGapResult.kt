package com.example.clockgap.model

import java.time.LocalTime

data class ClockGapResult(
    val timeA: LocalTime,
    val timeB: LocalTime,
    val gapSeconds: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalMinutes: Long,
    val decimalHours: Double,
    val earlierTime: LocalTime?,
    val laterTime: LocalTime?,
    val relationship: TimeRelationship,
    val mode: ClockGapMode,
    val crossesMidnight: Boolean,
    val isIdentical: Boolean
)
