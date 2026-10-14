package com.example.clockgap.domain

import com.example.clockgap.model.ClockGapMode
import com.example.clockgap.model.ClockGapResult
import com.example.clockgap.model.TimeRelationship
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.min

object ClockGapCalculator {

    private val REFERENCE_DATE: LocalDate = LocalDate.of(2000, 1, 1)
    private const val SECONDS_IN_DAY: Long = 24 * 3600L

    fun calculate(
        timeA: LocalTime,
        timeB: LocalTime,
        mode: ClockGapMode = ClockGapMode.DIRECT
    ): ClockGapResult {
        val dtA = LocalDateTime.of(REFERENCE_DATE, timeA)
        val isIdentical = (timeA == timeB)

        // Normal clock ordering relationship (independent of gap mode)
        val (relationship, earlierTime, laterTime) = when {
            timeA.isBefore(timeB) -> Triple(TimeRelationship.A_EARLIER, timeA, timeB)
            timeB.isBefore(timeA) -> Triple(TimeRelationship.B_EARLIER, timeB, timeA)
            else -> Triple(TimeRelationship.IDENTICAL, null, null)
        }

        val (gapSeconds, crossesMidnight) = when (mode) {
            ClockGapMode.DIRECT -> {
                val dtB = LocalDateTime.of(REFERENCE_DATE, timeB)
                val direct = abs(Duration.between(dtA, dtB).seconds)
                Pair(direct, false)
            }
            ClockGapMode.ACROSS_MIDNIGHT -> {
                // If B > A, on the same day.
                // If B < A, moves to next day.
                // If B == A, exact 24 hours (moves to next day).
                val dtB = if (timeB.isAfter(timeA)) {
                    LocalDateTime.of(REFERENCE_DATE, timeB)
                } else {
                    LocalDateTime.of(REFERENCE_DATE.plusDays(1), timeB)
                }
                val duration = Duration.between(dtA, dtB).seconds
                val crosses = !timeB.isAfter(timeA) // crosses midnight if B <= A
                Pair(duration, crosses)
            }
            ClockGapMode.SHORTEST_GAP -> {
                if (isIdentical) {
                    Pair(0L, false)
                } else {
                    val dtB = LocalDateTime.of(REFERENCE_DATE, timeB)
                    val direct = abs(Duration.between(dtA, dtB).seconds)
                    val wrap = SECONDS_IN_DAY - direct
                    if (wrap < direct) {
                        Pair(wrap, true)
                    } else {
                        // When direct <= wrap (including exact 12 hours)
                        Pair(direct, false)
                    }
                }
            }
        }

        // Safe positive check
        val safeGapSeconds = if (gapSeconds < 0L) 0L else gapSeconds
        val hours = safeGapSeconds / 3600L
        val minutes = (safeGapSeconds % 3600L) / 60L
        val seconds = safeGapSeconds % 60L
        val totalMinutes = safeGapSeconds / 60L
        val decimalHours = safeGapSeconds / 3600.0

        return ClockGapResult(
            timeA = timeA,
            timeB = timeB,
            gapSeconds = safeGapSeconds,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalMinutes = totalMinutes,
            decimalHours = decimalHours,
            earlierTime = earlierTime,
            laterTime = laterTime,
            relationship = relationship,
            mode = mode,
            crossesMidnight = crossesMidnight,
            isIdentical = isIdentical
        )
    }
}
