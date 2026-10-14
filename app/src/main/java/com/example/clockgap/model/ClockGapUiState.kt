package com.example.clockgap.model

import java.time.LocalTime

data class ClockGapUiState(
    val timeA: LocalTime,
    val timeB: LocalTime,
    val mode: ClockGapMode,
    val result: ClockGapResult?,
    val history: List<ClockGapHistory>,
    val errorMessage: String?,
    val isCalculating: Boolean
)
