package com.example.clockgap

import com.example.clockgap.domain.ClockGapCalculator
import com.example.clockgap.domain.ClockGapFormatter
import com.example.clockgap.domain.ClockGapValidator
import com.example.clockgap.model.AppSettings
import com.example.clockgap.model.ClockGapHistory
import com.example.clockgap.model.ClockGapMode
import com.example.clockgap.model.ThemeMode
import com.example.clockgap.model.TimeRelationship
import com.example.clockgap.repository.ClockGapHistoryRepository
import com.example.clockgap.repository.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ClockGapComprehensiveTest {

    private lateinit var fakePrefsHistory: FakeSharedPreferences
    private lateinit var historyRepository: ClockGapHistoryRepository

    private lateinit var fakePrefsSettings: FakeSharedPreferences
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        fakePrefsHistory = FakeSharedPreferences()
        historyRepository = ClockGapHistoryRepository(fakePrefsHistory)

        fakePrefsSettings = FakeSharedPreferences()
        settingsRepository = SettingsRepository(fakePrefsSettings)
    }

    // 1. Identical times
    @Test
    fun test01_identicalTimes() {
        val timeA = LocalTime.of(10, 0)
        val timeB = LocalTime.of(10, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertTrue(result.isIdentical)
        assertEquals(0L, result.gapSeconds)
        assertEquals(TimeRelationship.IDENTICAL, result.relationship)
    }

    // 2. One minute
    @Test
    fun test02_oneMinute() {
        val timeA = LocalTime.of(10, 0)
        val timeB = LocalTime.of(10, 1)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(60L, result.gapSeconds)
        assertEquals(0L, result.hours)
        assertEquals(1L, result.minutes)
        assertEquals("1 minute", ClockGapFormatter.formatHumanReadableGap(result.hours, result.minutes))
    }

    // 3. Five minutes
    @Test
    fun test03_fiveMinutes() {
        val timeA = LocalTime.of(10, 0)
        val timeB = LocalTime.of(10, 5)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(300L, result.gapSeconds)
        assertEquals(5L, result.minutes)
        assertEquals("5 minutes", ClockGapFormatter.formatHumanReadableGap(result.hours, result.minutes))
    }

    // 4. One hour
    @Test
    fun test04_oneHour() {
        val timeA = LocalTime.of(10, 0)
        val timeB = LocalTime.of(11, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(3600L, result.gapSeconds)
        assertEquals(1L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals("1 hour", ClockGapFormatter.formatHumanReadableGap(result.hours, result.minutes))
    }

    // 5. Eight hours
    @Test
    fun test05_eightHours() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(17, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(28800L, result.gapSeconds)
        assertEquals(8L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals("8 hours", ClockGapFormatter.formatHumanReadableGap(result.hours, result.minutes))
    }

    // 6. Twelve hours
    @Test
    fun test06_twelveHours() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(21, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(12L * 3600L, result.gapSeconds)
        assertEquals(12L, result.hours)
        assertEquals(0L, result.minutes)
    }

    // 7. Twenty-three hours
    @Test
    fun test07_twentyThreeHours() {
        val timeA = LocalTime.of(1, 0)
        val timeB = LocalTime.of(0, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(1L * 3600L, result.gapSeconds) // Absolute difference |1 - 0| is 1h in Direct mode
        val resultReversed = ClockGapCalculator.calculate(LocalTime.of(0, 0), LocalTime.of(23, 0), ClockGapMode.DIRECT)
        assertEquals(23L * 3600L, resultReversed.gapSeconds)
        assertEquals(23L, resultReversed.hours)
    }

    // 8. Direct mode
    @Test
    fun test08_directMode() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(17, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(8L, result.hours)
        assertFalse(result.crossesMidnight)
        assertEquals(ClockGapMode.DIRECT, result.mode)
    }

    // 9. Across-midnight mode
    @Test
    fun test09_acrossMidnightMode() {
        val timeA = LocalTime.of(22, 0)
        val timeB = LocalTime.of(2, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.ACROSS_MIDNIGHT)
        assertEquals(4L, result.hours)
        assertEquals(0L, result.minutes)
        assertTrue(result.crossesMidnight)
        assertEquals(ClockGapMode.ACROSS_MIDNIGHT, result.mode)
    }

    // 10. Shortest-gap mode
    @Test
    fun test10_shortestGapMode() {
        val timeA = LocalTime.of(23, 0)
        val timeB = LocalTime.of(1, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.SHORTEST_GAP)
        assertEquals(2L, result.hours)
        assertEquals(0L, result.minutes)
        assertTrue(result.crossesMidnight)
    }

    // 11. A earlier than B
    @Test
    fun test11_aEarlierThanB() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(17, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(TimeRelationship.A_EARLIER, result.relationship)
        assertEquals(timeA, result.earlierTime)
        assertEquals(timeB, result.laterTime)
    }

    // 12. B earlier than A
    @Test
    fun test12_bEarlierThanA() {
        val timeA = LocalTime.of(17, 0)
        val timeB = LocalTime.of(9, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(TimeRelationship.B_EARLIER, result.relationship)
        assertEquals(timeB, result.earlierTime)
        assertEquals(timeA, result.laterTime)
    }

    // 13. 00:00
    @Test
    fun test13_zeroZero() {
        val timeA = LocalTime.of(0, 0)
        val timeB = LocalTime.of(0, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(0L, result.gapSeconds)
        assertTrue(result.isIdentical)
    }

    // 14. 00:01
    @Test
    fun test14_zeroOne() {
        val timeA = LocalTime.of(0, 0)
        val timeB = LocalTime.of(0, 1)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(60L, result.gapSeconds)
        assertEquals(1L, result.minutes)
        assertEquals(TimeRelationship.A_EARLIER, result.relationship)
    }

    // 15. 23:59
    @Test
    fun test15_twentyThreeFiftyNine() {
        val timeA = LocalTime.of(23, 59)
        val timeB = LocalTime.of(23, 59)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(0L, result.gapSeconds)
        assertTrue(result.isIdentical)
    }

    // 16. 23:59 → 00:00
    @Test
    fun test16_twentyThreeFiftyNineToZeroZero() {
        val timeA = LocalTime.of(23, 59)
        val timeB = LocalTime.of(0, 0)
        // Direct: 23 hours 59 minutes
        val direct = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(23L, direct.hours)
        assertEquals(59L, direct.minutes)

        // Across Midnight: 1 minute
        val acrossMidnight = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.ACROSS_MIDNIGHT)
        assertEquals(0L, acrossMidnight.hours)
        assertEquals(1L, acrossMidnight.minutes)
        assertTrue(acrossMidnight.crossesMidnight)

        // Shortest Gap: 1 minute
        val shortest = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.SHORTEST_GAP)
        assertEquals(0L, shortest.hours)
        assertEquals(1L, shortest.minutes)
        assertEquals(60L, shortest.gapSeconds)
        assertTrue(shortest.crossesMidnight)
    }

    // 17. 22:00 → 02:00
    @Test
    fun test17_twentyTwoToZeroTwo() {
        val timeA = LocalTime.of(22, 0)
        val timeB = LocalTime.of(2, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.ACROSS_MIDNIGHT)
        assertEquals(4L, result.hours)
        assertEquals(0L, result.minutes)
        assertTrue(result.crossesMidnight)
    }

    // 18. 02:00 → 22:00
    @Test
    fun test18_zeroTwoToTwentyTwo() {
        val timeA = LocalTime.of(2, 0)
        val timeB = LocalTime.of(22, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.ACROSS_MIDNIGHT)
        assertEquals(20L, result.hours)
        assertEquals(0L, result.minutes)
        assertFalse(result.crossesMidnight)
    }

    // 19. 09:00 → 17:00
    @Test
    fun test19_nineToSeventeen() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(17, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(8L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals(28800L, result.gapSeconds)
        assertEquals(TimeRelationship.A_EARLIER, result.relationship)
    }

    // 20. 17:00 → 09:00
    @Test
    fun test20_seventeenToNine() {
        val timeA = LocalTime.of(17, 0)
        val timeB = LocalTime.of(9, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(8L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals(28800L, result.gapSeconds)
        assertEquals(TimeRelationship.B_EARLIER, result.relationship)
    }

    // 21. Equal times Direct
    @Test
    fun test21_equalTimesDirect() {
        val time = LocalTime.of(10, 0)
        val result = ClockGapCalculator.calculate(time, time, ClockGapMode.DIRECT)
        assertEquals(0L, result.gapSeconds)
        assertEquals(0L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals("0 minutes", ClockGapFormatter.formatHumanReadableGap(result.hours, result.minutes))
    }

    // 22. Equal times Across Midnight
    @Test
    fun test22_equalTimesAcrossMidnight() {
        val time = LocalTime.of(10, 0)
        val result = ClockGapCalculator.calculate(time, time, ClockGapMode.ACROSS_MIDNIGHT)
        assertEquals(24L * 3600L, result.gapSeconds)
        assertEquals(24L, result.hours)
        assertEquals(0L, result.minutes)
        assertTrue(result.crossesMidnight)
        assertEquals("24 hours", ClockGapFormatter.formatHumanReadableGap(result.hours, result.minutes))
        assertEquals("SAME CLOCK TIME — NEXT DAY", ClockGapFormatter.formatRelationship(result.relationship, result.mode))
    }

    // 23. Equal times Shortest Gap
    @Test
    fun test23_equalTimesShortestGap() {
        val time = LocalTime.of(10, 0)
        val result = ClockGapCalculator.calculate(time, time, ClockGapMode.SHORTEST_GAP)
        assertEquals(0L, result.gapSeconds)
        assertEquals(0L, result.hours)
        assertEquals(0L, result.minutes)
        assertFalse(result.crossesMidnight)
        assertEquals("0 minutes", ClockGapFormatter.formatHumanReadableGap(result.hours, result.minutes))
    }

    // 24. Exact 12-hour difference
    @Test
    fun test24_exact12HourDifference() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(21, 0)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.SHORTEST_GAP)
        assertEquals(12L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals(12L * 3600L, result.gapSeconds)
    }

    // 25. 12-hour + 1 minute
    @Test
    fun test25_twelveHourPlusOneMinute() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(21, 1)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.SHORTEST_GAP)
        // Direct difference: 12h 1m = 721m = 43260s
        // 24h - direct = 11h 59m = 719m = 43140s
        assertEquals(11L, result.hours)
        assertEquals(59L, result.minutes)
        assertEquals(43140L, result.gapSeconds)
        assertTrue(result.crossesMidnight)
    }

    // 26. 24-hour circular calculation
    @Test
    fun test26_twentyFourHourCircularCalculation() {
        val timeA = LocalTime.of(1, 0)
        val timeB = LocalTime.of(23, 0)
        val shortest = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.SHORTEST_GAP)
        assertEquals(2L, shortest.hours)
        assertEquals(0L, shortest.minutes)
        assertEquals(7200L, shortest.gapSeconds)
    }

    // 27. Shortest-gap wraparound
    @Test
    fun test27_shortestGapWraparound() {
        val timeA = LocalTime.of(23, 30)
        val timeB = LocalTime.of(0, 30)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.SHORTEST_GAP)
        assertEquals(1L, result.hours)
        assertEquals(0L, result.minutes)
        assertTrue(result.crossesMidnight)
    }

    // 28. Total minutes
    @Test
    fun test28_totalMinutes() {
        val timeA = LocalTime.of(9, 15)
        val timeB = LocalTime.of(17, 45)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        // 8h 30m = 510 minutes
        assertEquals(510L, result.totalMinutes)
        assertEquals(8L, result.hours)
        assertEquals(30L, result.minutes)
    }

    // 29. Total seconds
    @Test
    fun test29_totalSeconds() {
        val timeA = LocalTime.of(9, 15)
        val timeB = LocalTime.of(17, 45)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        // 510 minutes * 60 = 30600 seconds
        assertEquals(30600L, result.gapSeconds)
    }

    // 30. Decimal hours
    @Test
    fun test30_decimalHours() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(10, 30)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(1.5, result.decimalHours, 0.001)
        assertEquals("1.5", ClockGapFormatter.formatDecimalHoursValue(result.decimalHours))
        assertEquals("1.5 hours", ClockGapFormatter.formatDecimalHours(result.decimalHours))
    }

    // 31. Relationship detection
    @Test
    fun test31_relationshipDetection() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(17, 0)

        val resA = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(TimeRelationship.A_EARLIER, resA.relationship)
        assertEquals("TIME A IS EARLIER", ClockGapFormatter.formatRelationship(resA.relationship))

        val resB = ClockGapCalculator.calculate(timeB, timeA, ClockGapMode.DIRECT)
        assertEquals(TimeRelationship.B_EARLIER, resB.relationship)
        assertEquals("TIME B IS EARLIER", ClockGapFormatter.formatRelationship(resB.relationship))

        val resEq = ClockGapCalculator.calculate(timeA, timeA, ClockGapMode.DIRECT)
        assertEquals(TimeRelationship.IDENTICAL, resEq.relationship)
        assertEquals("TIMES ARE IDENTICAL", ClockGapFormatter.formatRelationship(resEq.relationship))
    }

    // 32. Midnight detection
    @Test
    fun test32_midnightDetection() {
        // Direct never crosses midnight
        val direct = ClockGapCalculator.calculate(LocalTime.of(23, 0), LocalTime.of(1, 0), ClockGapMode.DIRECT)
        assertFalse(direct.crossesMidnight)

        // Across midnight crosses when B < A or B == A
        val across = ClockGapCalculator.calculate(LocalTime.of(23, 0), LocalTime.of(1, 0), ClockGapMode.ACROSS_MIDNIGHT)
        assertTrue(across.crossesMidnight)

        // Shortest gap crosses when wrapping around midnight
        val shortest = ClockGapCalculator.calculate(LocalTime.of(23, 0), LocalTime.of(1, 0), ClockGapMode.SHORTEST_GAP)
        assertTrue(shortest.crossesMidnight)
    }

    // 33. Singular/plural formatting
    @Test
    fun test33_singularPluralFormatting() {
        assertEquals("0 minutes", ClockGapFormatter.formatHumanReadableGap(0, 0))
        assertEquals("1 minute", ClockGapFormatter.formatHumanReadableGap(0, 1))
        assertEquals("45 minutes", ClockGapFormatter.formatHumanReadableGap(0, 45))
        assertEquals("1 hour", ClockGapFormatter.formatHumanReadableGap(1, 0))
        assertEquals("1 hour 1 minute", ClockGapFormatter.formatHumanReadableGap(1, 1))
        assertEquals("2 hours 30 minutes", ClockGapFormatter.formatHumanReadableGap(2, 30))
        assertEquals("8 hours", ClockGapFormatter.formatHumanReadableGap(8, 0))
        assertEquals("8 hours 30 minutes", ClockGapFormatter.formatHumanReadableGap(8, 30))
        assertEquals("24 hours", ClockGapFormatter.formatHumanReadableGap(24, 0))
    }

    // 34. Swap
    @Test
    fun test34_swap() {
        var timeA = LocalTime.of(9, 0)
        var timeB = LocalTime.of(17, 0)
        val initial = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(8L, initial.hours)
        assertEquals(TimeRelationship.A_EARLIER, initial.relationship)

        // Swap
        val temp = timeA
        timeA = timeB
        timeB = temp
        val swapped = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        assertEquals(8L, swapped.hours)
        assertEquals(TimeRelationship.B_EARLIER, swapped.relationship)
    }

    // 35. Quick gap presets
    @Test
    fun test35_quickGapPresets() {
        val baseTime = LocalTime.of(9, 0)
        val refDate = LocalDate.of(2000, 1, 1)

        val preset15m = LocalDateTime.of(refDate, baseTime).plusMinutes(15).toLocalTime()
        assertEquals(LocalTime.of(9, 15), preset15m)

        val preset2h = LocalDateTime.of(refDate, baseTime).plusMinutes(120).toLocalTime()
        assertEquals(LocalTime.of(11, 0), preset2h)

        val preset12h = LocalDateTime.of(refDate, baseTime).plusMinutes(720).toLocalTime()
        assertEquals(LocalTime.of(21, 0), preset12h)
    }

    // 36. History save
    @Test
    fun test36_historySave() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(17, 0)
        val list = historyRepository.addCalculation(timeA, timeB, ClockGapMode.DIRECT, 28800L)
        assertEquals(1, list.size)
        assertEquals(timeA, list[0].timeA)
        assertEquals(timeB, list[0].timeB)
        assertEquals(ClockGapMode.DIRECT, list[0].mode)
    }

    // 37. History load
    @Test
    fun test37_historyLoad() {
        historyRepository.addCalculation(LocalTime.of(9, 0), LocalTime.of(17, 0), ClockGapMode.DIRECT, 28800L)
        val loaded = historyRepository.getHistory()
        assertEquals(1, loaded.size)
        assertEquals(LocalTime.of(9, 0), loaded[0].timeA)
        assertEquals(LocalTime.of(17, 0), loaded[0].timeB)
    }

    // 38. History limit
    @Test
    fun test38_historyLimit() {
        for (i in 0 until 15) {
            historyRepository.addCalculation(
                LocalTime.of(i % 24, 0),
                LocalTime.of((i + 1) % 24, 0),
                ClockGapMode.DIRECT,
                3600L
            )
        }
        val history = historyRepository.getHistory()
        assertEquals(10, history.size)
        // Most recent should be front
        assertEquals(LocalTime.of(14, 0), history[0].timeA)
    }

    // 39. History deletion
    @Test
    fun test39_historyDeletion() {
        historyRepository.addCalculation(LocalTime.of(9, 0), LocalTime.of(17, 0), ClockGapMode.DIRECT, 28800L)
        val history = historyRepository.getHistory()
        assertEquals(1, history.size)
        val id = history[0].id

        val afterDelete = historyRepository.deleteItem(id)
        assertTrue(afterDelete.isEmpty())
        assertTrue(historyRepository.getHistory().isEmpty())
    }

    // 40. History clear
    @Test
    fun test40_historyClear() {
        historyRepository.addCalculation(LocalTime.of(9, 0), LocalTime.of(17, 0), ClockGapMode.DIRECT, 28800L)
        historyRepository.addCalculation(LocalTime.of(10, 0), LocalTime.of(18, 0), ClockGapMode.DIRECT, 28800L)
        assertEquals(2, historyRepository.getHistory().size)

        historyRepository.clearHistory()
        assertTrue(historyRepository.getHistory().isEmpty())
    }

    // 41. Settings persistence
    @Test
    fun test41_settingsPersistence() {
        val custom = AppSettings(
            themeMode = ThemeMode.DARK,
            dynamicColor = false,
            hapticFeedback = false,
            animationsEnabled = false,
            defaultGapMode = ClockGapMode.SHORTEST_GAP,
            confirmHistoryDelete = false,
            preserveLastTimes = false
        )
        settingsRepository.saveSettings(custom)
        val retrieved = settingsRepository.getSettings()

        assertEquals(ThemeMode.DARK, retrieved.themeMode)
        assertFalse(retrieved.dynamicColor)
        assertFalse(retrieved.hapticFeedback)
        assertFalse(retrieved.animationsEnabled)
        assertEquals(ClockGapMode.SHORTEST_GAP, retrieved.defaultGapMode)
        assertFalse(retrieved.confirmHistoryDelete)
        assertFalse(retrieved.preserveLastTimes)
    }

    // 42. Last-time persistence
    @Test
    fun test42_lastTimePersistence() {
        val settings = AppSettings(preserveLastTimes = true)
        settingsRepository.saveSettings(settings)

        val timeA = LocalTime.of(8, 15)
        val timeB = LocalTime.of(16, 45)
        val mode = ClockGapMode.SHORTEST_GAP
        settingsRepository.saveLastInputs(timeA, timeB, mode)

        val (restoredA, restoredB, restoredMode) = settingsRepository.getInitialInputs(settings)
        assertEquals(timeA, restoredA)
        assertEquals(timeB, restoredB)
        assertEquals(mode, restoredMode)
    }

    // 43. Reset
    @Test
    fun test43_reset() {
        // Save something in history first
        historyRepository.addCalculation(LocalTime.of(1, 0), LocalTime.of(2, 0), ClockGapMode.DIRECT, 3600L)

        // Reset inputs
        val defaultA = SettingsRepository.DEFAULT_TIME_A
        val defaultB = SettingsRepository.DEFAULT_TIME_B
        val defaultMode = SettingsRepository.DEFAULT_MODE

        assertEquals(LocalTime.of(9, 0), defaultA)
        assertEquals(LocalTime.of(17, 0), defaultB)
        assertEquals(ClockGapMode.DIRECT, defaultMode)

        // History remains untouched
        assertEquals(1, historyRepository.getHistory().size)
    }

    // 44. Copy formatting
    @Test
    fun test44_copyFormatting() {
        val timeA = LocalTime.of(9, 15)
        val timeB = LocalTime.of(17, 45)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        val copyText = ClockGapFormatter.formatCopyText(result, is24Hour = false)

        assertTrue(copyText.contains("ClockGap Result"))
        assertTrue(copyText.contains("Time A: 09:15 AM"))
        assertTrue(copyText.contains("Time B: 05:45 PM"))
        assertTrue(copyText.contains("Clock gap: 8 hours 30 minutes"))
        assertTrue(copyText.contains("Digital gap: 08:30:00"))
        assertTrue(copyText.contains("Total minutes: 510"))
        assertTrue(copyText.contains("Total seconds: 30600"))
        assertTrue(copyText.contains("Decimal hours: 8.5"))
        assertTrue(copyText.contains("Time A is earlier"))
        assertTrue(copyText.contains("Mode: Direct"))
    }

    // 45. Share formatting
    @Test
    fun test45_shareFormatting() {
        val timeA = LocalTime.of(9, 15)
        val timeB = LocalTime.of(17, 45)
        val result = ClockGapCalculator.calculate(timeA, timeB, ClockGapMode.DIRECT)
        val shareText = ClockGapFormatter.formatShareText(result, is24Hour = false)

        assertTrue(shareText.contains("ClockGap"))
        assertTrue(shareText.contains("09:15 AM ↔ 05:45 PM"))
        assertTrue(shareText.contains("8 hours 30 minutes apart"))
        assertTrue(shareText.contains("08:30:00"))
        assertTrue(shareText.contains("510 minutes"))
        assertTrue(shareText.contains("Time A is earlier"))
        assertTrue(shareText.contains("Direct mode"))

        // Shortest gap test
        val shortestRes = ClockGapCalculator.calculate(LocalTime.of(23, 0), LocalTime.of(1, 0), ClockGapMode.SHORTEST_GAP)
        val shortestShare = ClockGapFormatter.formatShareText(shortestRes, is24Hour = false)
        assertTrue(shortestShare.contains("Shortest clock gap"))
    }

    // 46. Rapid calculation protection
    @Test
    fun test46_rapidCalculationProtection() {
        val timeA = LocalTime.of(9, 0)
        val timeB = LocalTime.of(17, 0)
        val mode = ClockGapMode.DIRECT
        val gap = 28800L

        // Rapid duplicate calls
        val list1 = historyRepository.addCalculation(timeA, timeB, mode, gap)
        val list2 = historyRepository.addCalculation(timeA, timeB, mode, gap)
        val list3 = historyRepository.addCalculation(timeA, timeB, mode, gap)

        // Should still only have 1 entry
        assertEquals(1, list1.size)
        assertEquals(1, list2.size)
        assertEquals(1, list3.size)
        assertEquals(1, historyRepository.getHistory().size)
    }

    // Additional validation tests
    @Test
    fun testValidation() {
        val valid = ClockGapValidator.validate(LocalTime.of(9, 0), LocalTime.of(17, 0))
        assertTrue(valid is ClockGapValidator.ValidationResult.Valid)

        val invalidA = ClockGapValidator.validate(null, LocalTime.of(17, 0))
        assertTrue(invalidA is ClockGapValidator.ValidationResult.Invalid)

        val invalidB = ClockGapValidator.validate(LocalTime.of(9, 0), null)
        assertTrue(invalidB is ClockGapValidator.ValidationResult.Invalid)
    }
}
