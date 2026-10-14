package com.example.clockgap.domain

import com.example.clockgap.model.ClockGapMode
import com.example.clockgap.model.ClockGapResult
import com.example.clockgap.model.TimeRelationship
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ClockGapFormatter {

    private val TIME_FORMAT_12H: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    private val TIME_FORMAT_24H: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    fun formatTime(time: LocalTime, is24Hour: Boolean = false): String {
        val formatter = if (is24Hour) TIME_FORMAT_24H else TIME_FORMAT_12H
        return time.format(formatter)
    }

    fun formatHumanReadableGap(hours: Long, minutes: Long, seconds: Long = 0L): String {
        if (hours == 0L && minutes == 0L && seconds == 0L) {
            return "0 minutes"
        }

        val parts = mutableListOf<String>()

        if (hours > 0L) {
            parts.add(if (hours == 1L) "1 hour" else "$hours hours")
        }

        if (minutes > 0L) {
            parts.add(if (minutes == 1L) "1 minute" else "$minutes minutes")
        }

        if (seconds > 0L) {
            parts.add(if (seconds == 1L) "1 second" else "$seconds seconds")
        }

        return if (parts.isEmpty()) "0 minutes" else parts.joinToString(" ")
    }

    fun formatDigitalGap(hours: Long, minutes: Long, seconds: Long): String {
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatTotalMinutes(totalMinutes: Long): String {
        return String.format(Locale.US, "%,d minutes", totalMinutes)
    }

    fun formatTotalSeconds(gapSeconds: Long): String {
        return String.format(Locale.US, "%,d seconds", gapSeconds)
    }

    fun formatDecimalHours(decimalHours: Double): String {
        // Formats cleanly e.g. 8.5 or 8.0
        val formatted = String.format(Locale.US, "%.1f", decimalHours)
        return "$formatted hours"
    }

    fun formatDecimalHoursValue(decimalHours: Double): String {
        return String.format(Locale.US, "%.1f", decimalHours)
    }

    fun formatRelationship(relationship: TimeRelationship, mode: ClockGapMode = ClockGapMode.DIRECT): String {
        return when (relationship) {
            TimeRelationship.A_EARLIER -> "TIME A IS EARLIER"
            TimeRelationship.B_EARLIER -> "TIME B IS EARLIER"
            TimeRelationship.IDENTICAL -> {
                if (mode == ClockGapMode.ACROSS_MIDNIGHT) {
                    "SAME CLOCK TIME — NEXT DAY"
                } else {
                    "TIMES ARE IDENTICAL"
                }
            }
        }
    }

    fun formatRelationshipTitleCase(relationship: TimeRelationship, mode: ClockGapMode = ClockGapMode.DIRECT): String {
        return when (relationship) {
            TimeRelationship.A_EARLIER -> "Time A is earlier"
            TimeRelationship.B_EARLIER -> "Time B is earlier"
            TimeRelationship.IDENTICAL -> {
                if (mode == ClockGapMode.ACROSS_MIDNIGHT) {
                    "Same clock time — next day"
                } else {
                    "Times are identical"
                }
            }
        }
    }

    fun formatModeName(mode: ClockGapMode): String {
        return when (mode) {
            ClockGapMode.DIRECT -> "Direct"
            ClockGapMode.ACROSS_MIDNIGHT -> "Across Midnight"
            ClockGapMode.SHORTEST_GAP -> "Shortest Gap"
        }
    }

    fun formatCopyText(result: ClockGapResult, is24Hour: Boolean = false): String {
        val timeAStr = formatTime(result.timeA, is24Hour)
        val timeBStr = formatTime(result.timeB, is24Hour)
        val gapStr = formatHumanReadableGap(result.hours, result.minutes, result.seconds)
        val digitalStr = formatDigitalGap(result.hours, result.minutes, result.seconds)
        val decimalStr = formatDecimalHoursValue(result.decimalHours)
        val relStr = formatRelationshipTitleCase(result.relationship, result.mode)
        val modeStr = formatModeName(result.mode)

        return """
ClockGap Result

Time A: $timeAStr
Time B: $timeBStr

Clock gap: $gapStr
Digital gap: $digitalStr
Total minutes: ${result.totalMinutes}
Total seconds: ${result.gapSeconds}
Decimal hours: $decimalStr

$relStr
Mode: $modeStr
""".trimIndent()
    }

    fun formatShareText(result: ClockGapResult, is24Hour: Boolean = false): String {
        val timeAStr = formatTime(result.timeA, is24Hour)
        val timeBStr = formatTime(result.timeB, is24Hour)
        val gapStr = formatHumanReadableGap(result.hours, result.minutes, result.seconds)
        val digitalStr = formatDigitalGap(result.hours, result.minutes, result.seconds)
        val relStr = formatRelationshipTitleCase(result.relationship, result.mode)

        val modeNote = when (result.mode) {
            ClockGapMode.DIRECT -> "Direct mode"
            ClockGapMode.ACROSS_MIDNIGHT -> "Across Midnight mode"
            ClockGapMode.SHORTEST_GAP -> "Shortest clock gap"
        }

        return """
ClockGap

$timeAStr ↔ $timeBStr

$gapStr apart
$digitalStr
${result.totalMinutes} minutes

$relStr
$modeNote
""".trimIndent()
    }
}
