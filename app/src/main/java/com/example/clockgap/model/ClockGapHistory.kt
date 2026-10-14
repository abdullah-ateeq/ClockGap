package com.example.clockgap.model

import java.time.LocalTime

data class ClockGapHistory(
    val id: String,
    val timeA: LocalTime,
    val timeB: LocalTime,
    val mode: ClockGapMode,
    val gapSeconds: Long,
    val calculatedAt: Long
)
