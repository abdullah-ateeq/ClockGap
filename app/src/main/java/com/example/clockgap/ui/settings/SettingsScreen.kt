package com.example.clockgap.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clockgap.model.ClockGapMode
import com.example.clockgap.model.ThemeMode
import com.example.clockgap.ui.ClockGapViewModel
import com.example.clockgap.util.HapticHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: ClockGapViewModel,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val settings by viewModel.settings.collectAsState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Clear all recent comparisons?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticHelper.trigger(haptics, settings.hapticFeedback)
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                        scope.launch { snackbarHostState.showSnackbar("History cleared") }
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset Settings Dialog
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Reset all preferences to default values?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticHelper.trigger(haptics, settings.hapticFeedback)
                        viewModel.resetSettings()
                        showResetSettingsDialog = false
                        scope.launch { snackbarHostState.showSnackbar("Settings reset to defaults") }
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Preferences and appearance",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // APPEARANCE SECTION
            SettingsSectionHeader(title = "APPEARANCE")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val themes = listOf(
                        ThemeMode.SYSTEM to "System",
                        ThemeMode.LIGHT to "Light",
                        ThemeMode.DARK to "Dark"
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themes.forEachIndexed { index, (mode, label) ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index, themes.size),
                                onClick = {
                                    HapticHelper.trigger(haptics, settings.hapticFeedback)
                                    viewModel.updateSettings(settings.copy(themeMode = mode))
                                },
                                selected = settings.themeMode == mode
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSwitchRow(
                        title = "Dynamic Color",
                        subtitle = "Apply device wallpaper colors (Android 12+)",
                        checked = settings.dynamicColor,
                        onCheckedChange = { checked ->
                            HapticHelper.trigger(haptics, settings.hapticFeedback)
                            viewModel.updateSettings(settings.copy(dynamicColor = checked))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BEHAVIOR SECTION
            SettingsSectionHeader(title = "BEHAVIOR & FEEDBACK")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on button clicks and actions",
                        checked = settings.hapticFeedback,
                        onCheckedChange = { checked ->
                            HapticHelper.trigger(haptics, checked)
                            viewModel.updateSettings(settings.copy(hapticFeedback = checked))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSwitchRow(
                        title = "Animations",
                        subtitle = "Smooth transitions when results appear",
                        checked = settings.animationsEnabled,
                        onCheckedChange = { checked ->
                            HapticHelper.trigger(haptics, settings.hapticFeedback)
                            viewModel.updateSettings(settings.copy(animationsEnabled = checked))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSwitchRow(
                        title = "Preserve Last Times",
                        subtitle = "Restore your previous times on app launch",
                        checked = settings.preserveLastTimes,
                        onCheckedChange = { checked ->
                            HapticHelper.trigger(haptics, settings.hapticFeedback)
                            viewModel.updateSettings(settings.copy(preserveLastTimes = checked))
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSwitchRow(
                        title = "Confirm History Delete",
                        subtitle = "Show confirmation dialog before clearing",
                        checked = settings.confirmHistoryDelete,
                        onCheckedChange = { checked ->
                            HapticHelper.trigger(haptics, settings.hapticFeedback)
                            viewModel.updateSettings(settings.copy(confirmHistoryDelete = checked))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DEFAULTS SECTION
            SettingsSectionHeader(title = "CALCULATION DEFAULTS")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Default Gap Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val modes = listOf(
                        ClockGapMode.DIRECT to "Direct",
                        ClockGapMode.ACROSS_MIDNIGHT to "Across Midnight",
                        ClockGapMode.SHORTEST_GAP to "Shortest Gap"
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        modes.forEachIndexed { index, (mode, label) ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                                onClick = {
                                    HapticHelper.trigger(haptics, settings.hapticFeedback)
                                    viewModel.updateSettings(settings.copy(defaultGapMode = mode))
                                },
                                selected = settings.defaultGapMode == mode
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ACTIONS SECTION
            SettingsSectionHeader(title = "ACTIONS")

            OutlinedButton(
                onClick = {
                    HapticHelper.trigger(haptics, settings.hapticFeedback)
                    if (settings.confirmHistoryDelete) {
                        showClearHistoryDialog = true
                    } else {
                        viewModel.clearAllHistory()
                        scope.launch { snackbarHostState.showSnackbar("History cleared") }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Clear History")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    HapticHelper.trigger(haptics, settings.hapticFeedback)
                    showResetSettingsDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Reset Settings")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
