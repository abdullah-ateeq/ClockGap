package com.example.clockgap.ui.home

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clockgap.domain.ClockGapFormatter
import com.example.clockgap.model.ClockGapMode
import com.example.clockgap.model.ClockGapResult
import com.example.clockgap.model.ClockGapUiState
import com.example.clockgap.model.TimeRelationship
import com.example.clockgap.ui.ClockGapViewModel
import com.example.clockgap.ui.home.components.Circular24HourVisual
import com.example.clockgap.ui.home.components.ClockGapTimePickerDialog
import com.example.clockgap.ui.home.components.HorizontalGapVisual
import com.example.clockgap.ui.theme.CrossMidnightBadgeBg
import com.example.clockgap.ui.theme.CrossMidnightBadgeBgDark
import com.example.clockgap.ui.theme.CrossMidnightBadgeText
import com.example.clockgap.ui.theme.CrossMidnightBadgeTextDark
import com.example.clockgap.util.ClipboardHelper
import com.example.clockgap.util.HapticHelper
import com.example.clockgap.util.ShareHelper
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: ClockGapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val is24Hour = DateFormat.is24HourFormat(context)

    var showTimePickerForA by remember { mutableStateOf(false) }
    var showTimePickerForB by remember { mutableStateOf(false) }
    var showModeExplainer by remember { mutableStateOf(false) }

    // Dialog for Time A
    if (showTimePickerForA) {
        ClockGapTimePickerDialog(
            title = "Set Time A",
            initialTime = uiState.timeA,
            is24Hour = is24Hour,
            onConfirm = { newTime ->
                HapticHelper.trigger(haptics, settings.hapticFeedback)
                viewModel.setTimeA(newTime)
                showTimePickerForA = false
            },
            onDismiss = { showTimePickerForA = false }
        )
    }

    // Dialog for Time B
    if (showTimePickerForB) {
        ClockGapTimePickerDialog(
            title = "Set Time B",
            initialTime = uiState.timeB,
            is24Hour = is24Hour,
            onConfirm = { newTime ->
                HapticHelper.trigger(haptics, settings.hapticFeedback)
                viewModel.setTimeB(newTime)
                showTimePickerForB = false
            },
            onDismiss = { showTimePickerForB = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "ClockGap",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Find the difference between two clock times.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // TIME A Input Card
            TimeInputCard(
                label = "TIME A",
                time = uiState.timeA,
                is24Hour = is24Hour,
                onCardClick = {
                    HapticHelper.trigger(haptics, settings.hapticFeedback)
                    showTimePickerForA = true
                },
                onNowClick = {
                    HapticHelper.trigger(haptics, settings.hapticFeedback)
                    viewModel.setTimeA(LocalTime.now().withSecond(0).withNano(0))
                }
            )

            // SWAP Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = {
                        HapticHelper.trigger(haptics, settings.hapticFeedback)
                        viewModel.swapTimes()
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.size(44.dp),
                    tonalElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Swap Time A and Time B",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // TIME B Input Card
            TimeInputCard(
                label = "TIME B",
                time = uiState.timeB,
                is24Hour = is24Hour,
                onCardClick = {
                    HapticHelper.trigger(haptics, settings.hapticFeedback)
                    showTimePickerForB = true
                },
                onNowClick = {
                    HapticHelper.trigger(haptics, settings.hapticFeedback)
                    viewModel.setTimeB(LocalTime.now().withSecond(0).withNano(0))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // MODE Selector Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CALCULATION MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                IconButton(
                    onClick = { showModeExplainer = !showModeExplainer },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "Explain calculation modes",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mode Selector Segmented Buttons
            val modes = listOf(
                ClockGapMode.DIRECT to "Direct",
                ClockGapMode.ACROSS_MIDNIGHT to "Across Midnight",
                ClockGapMode.SHORTEST_GAP to "Shortest Gap"
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                modes.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                        onClick = {
                            HapticHelper.trigger(haptics, settings.hapticFeedback)
                            viewModel.setMode(mode)
                        },
                        selected = uiState.mode == mode
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (uiState.mode == mode) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mode Explanatory Subtitle
            val modeDescription = when (uiState.mode) {
                ClockGapMode.DIRECT -> "Compare both times on the same clock day."
                ClockGapMode.ACROSS_MIDNIGHT -> "Treat Time B as occurring after Time A."
                ClockGapMode.SHORTEST_GAP -> "Find the smallest distance around a 24-hour clock."
            }
            Text(
                text = modeDescription,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            // Collapsible Mode Helper Info
            AnimatedVisibility(
                visible = showModeExplainer,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Mode Differences (e.g. 11:00 PM → 01:00 AM)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Direct: 22 hours (absolute difference on the same 24h clock day).\n" +
                                    "• Across Midnight: 2 hours (Time B occurs after Time A on the next day).\n" +
                                    "• Shortest Gap: 2 hours (smallest circular distance around a 24-hour clock).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Gaps Presets
            Text(
                text = "QUICK GAPS (ADJUST TIME B)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val quickPresets = listOf(
                15L to "15m",
                30L to "30m",
                60L to "1h",
                120L to "2h",
                240L to "4h",
                360L to "6h",
                720L to "12h"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPresets.forEach { (minutes, label) ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            HapticHelper.trigger(haptics, settings.hapticFeedback)
                            viewModel.applyQuickPreset(minutes)
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Action Buttons: FIND CLOCK GAP & RESET
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        HapticHelper.trigger(haptics, settings.hapticFeedback)
                        viewModel.calculateGap()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "FIND CLOCK GAP",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                OutlinedButton(
                    onClick = {
                        HapticHelper.trigger(haptics, settings.hapticFeedback)
                        viewModel.resetToDefaults()
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset inputs",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RESET",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Error display if any
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // RESULT SECTION (Animated 300-500ms)
            AnimatedVisibility(
                visible = uiState.result != null,
                enter = if (settings.animationsEnabled) {
                    fadeIn(animationSpec = tween(400)) + slideInVertically(
                        animationSpec = tween(400),
                        initialOffsetY = { 40 }
                    )
                } else fadeIn(),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                uiState.result?.let { result ->
                    ResultCard(
                        result = result,
                        is24Hour = is24Hour,
                        onCopy = {
                            HapticHelper.trigger(haptics, settings.hapticFeedback)
                            val text = ClockGapFormatter.formatCopyText(result, is24Hour)
                            ClipboardHelper.copyToClipboard(context, text)
                            scope.launch {
                                snackbarHostState.showSnackbar("Result copied to clipboard")
                            }
                        },
                        onShare = {
                            HapticHelper.trigger(haptics, settings.hapticFeedback)
                            val text = ClockGapFormatter.formatShareText(result, is24Hour)
                            ShareHelper.shareText(context, text)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TimeInputCard(
    label: String,
    time: LocalTime,
    is24Hour: Boolean,
    onCardClick: () -> Unit,
    onNowClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ClockGapFormatter.formatTime(time, is24Hour),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            FilledTonalButton(
                onClick = onNowClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Set to Current Time",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "NOW",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: ClockGapResult,
    is24Hour: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.run {
        (red * 0.299 + green * 0.587 + blue * 0.114) < 0.5
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Prominent Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CLOCK GAP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Primary result: e.g. "8 HOURS 30 MINUTES"
                val primaryGapText = ClockGapFormatter.formatHumanReadableGap(
                    result.hours,
                    result.minutes,
                    result.seconds
                ).uppercase()

                Text(
                    text = primaryGapText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Total Minutes Apart (e.g. 510 MINUTES APART)
                Text(
                    text = "${result.totalMinutes} MINUTES APART",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Digital format: 08:30:00
                Text(
                    text = ClockGapFormatter.formatDigitalGap(result.hours, result.minutes, result.seconds),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Relationship Badge & Midnight Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val relationshipText = ClockGapFormatter.formatRelationship(result.relationship, result.mode)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = relationshipText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    if (result.crossesMidnight) {
                        val badgeBg = if (isDark) CrossMidnightBadgeBgDark else CrossMidnightBadgeBg
                        val badgeText = if (isDark) CrossMidnightBadgeTextDark else CrossMidnightBadgeText
                        val label = if (result.hours == 24L && result.isIdentical) "FULL 24-HOUR GAP" else "CROSSES MIDNIGHT"

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = badgeText
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Horizontal Gap Visualizer
                HorizontalGapVisual(
                    result = result,
                    is24Hour = is24Hour
                )

                // Circular 24-Hour Visualizer (Shown for Shortest Gap mode)
                if (result.mode == ClockGapMode.SHORTEST_GAP) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Circular24HourVisual(
                        timeA = result.timeA,
                        timeB = result.timeB,
                        result = result
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secondary Details Grid Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "RESULT DETAILS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(label = "Clock gap", value = ClockGapFormatter.formatHumanReadableGap(result.hours, result.minutes, result.seconds))
                DetailRow(label = "Total minutes", value = "${result.totalMinutes}")
                DetailRow(label = "Total seconds", value = String.format("%,d", result.gapSeconds))
                DetailRow(label = "Decimal hours", value = ClockGapFormatter.formatDecimalHoursValue(result.decimalHours))
                DetailRow(label = "Calculation mode", value = ClockGapFormatter.formatModeName(result.mode))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Copy & Share Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = onCopy,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy result",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "COPY RESULT",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            FilledTonalButton(
                onClick = onShare,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share result",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SHARE RESULT",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
