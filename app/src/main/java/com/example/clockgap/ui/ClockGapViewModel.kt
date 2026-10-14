package com.example.clockgap.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clockgap.domain.ClockGapCalculator
import com.example.clockgap.domain.ClockGapValidator
import com.example.clockgap.model.AppSettings
import com.example.clockgap.model.CalculationState
import com.example.clockgap.model.ClockGapHistory
import com.example.clockgap.model.ClockGapMode
import com.example.clockgap.model.ClockGapResult
import com.example.clockgap.model.ClockGapUiState
import com.example.clockgap.repository.ClockGapHistoryRepository
import com.example.clockgap.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ClockGapViewModel(
    application: Application,
    private val historyRepository: ClockGapHistoryRepository = ClockGapHistoryRepository(application),
    private val settingsRepository: SettingsRepository = SettingsRepository(application)
) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow(settingsRepository.getSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _uiState = MutableStateFlow(
        createInitialState()
    )
    val uiState: StateFlow<ClockGapUiState> = _uiState.asStateFlow()

    private val _calculationState = MutableStateFlow<CalculationState>(CalculationState.Idle)
    val calculationState: StateFlow<CalculationState> = _calculationState.asStateFlow()

    private fun createInitialState(): ClockGapUiState {
        val initialSettings = settingsRepository.getSettings()
        val (initA, initB, initMode) = settingsRepository.getInitialInputs(initialSettings)
        val initialHistory = historyRepository.getHistory()

        // Calculate initial result so the user immediately sees the comparison
        val initialResult = ClockGapCalculator.calculate(initA, initB, initMode)

        return ClockGapUiState(
            timeA = initA,
            timeB = initB,
            mode = initMode,
            result = initialResult,
            history = initialHistory,
            errorMessage = null,
            isCalculating = false
        )
    }

    fun setTimeA(time: LocalTime) {
        val current = _uiState.value
        if (current.timeA == time) return

        // Recalculate result for new time A so results are never stale
        val newResult = ClockGapCalculator.calculate(time, current.timeB, current.mode)
        _uiState.update {
            it.copy(
                timeA = time,
                result = newResult,
                errorMessage = null
            )
        }
        persistCurrentInputsIfEnabled()
    }

    fun setTimeB(time: LocalTime) {
        val current = _uiState.value
        if (current.timeB == time) return

        // Recalculate result for new time B so results are never stale
        val newResult = ClockGapCalculator.calculate(current.timeA, time, current.mode)
        _uiState.update {
            it.copy(
                timeB = time,
                result = newResult,
                errorMessage = null
            )
        }
        persistCurrentInputsIfEnabled()
    }

    fun setMode(mode: ClockGapMode) {
        val current = _uiState.value
        if (current.mode == mode) return

        val newResult = ClockGapCalculator.calculate(current.timeA, current.timeB, mode)
        _uiState.update {
            it.copy(
                mode = mode,
                result = newResult,
                errorMessage = null
            )
        }
        persistCurrentInputsIfEnabled()
    }

    fun swapTimes() {
        val current = _uiState.value
        val newA = current.timeB
        val newB = current.timeA
        val newResult = ClockGapCalculator.calculate(newA, newB, current.mode)

        _uiState.update {
            it.copy(
                timeA = newA,
                timeB = newB,
                result = newResult,
                errorMessage = null
            )
        }
        persistCurrentInputsIfEnabled()
    }

    fun applyQuickPreset(minutesToAdd: Long) {
        val current = _uiState.value
        val refDate = LocalDate.of(2000, 1, 1)
        val refDt = LocalDateTime.of(refDate, current.timeA).plusMinutes(minutesToAdd)
        val newTimeB = refDt.toLocalTime()

        val newResult = ClockGapCalculator.calculate(current.timeA, newTimeB, current.mode)
        _uiState.update {
            it.copy(
                timeB = newTimeB,
                result = newResult,
                errorMessage = null
            )
        }
        persistCurrentInputsIfEnabled()
    }

    fun calculateGap() {
        val current = _uiState.value
        if (current.isCalculating) return

        val validation = ClockGapValidator.validate(current.timeA, current.timeB)
        if (validation is ClockGapValidator.ValidationResult.Invalid) {
            _uiState.update { it.copy(errorMessage = validation.message) }
            _calculationState.value = CalculationState.Error(validation.message)
            return
        }

        _uiState.update { it.copy(isCalculating = true) }

        val result = ClockGapCalculator.calculate(current.timeA, current.timeB, current.mode)

        // Save to history (with duplicate & rapid-click protection handled in repo)
        val updatedHistory = historyRepository.addCalculation(
            timeA = current.timeA,
            timeB = current.timeB,
            mode = current.mode,
            gapSeconds = result.gapSeconds
        )

        persistCurrentInputsIfEnabled()

        _uiState.update {
            it.copy(
                result = result,
                history = updatedHistory,
                errorMessage = null,
                isCalculating = false
            )
        }
        _calculationState.value = CalculationState.Calculated
    }

    fun resetToDefaults() {
        val defaultA = SettingsRepository.DEFAULT_TIME_A
        val defaultB = SettingsRepository.DEFAULT_TIME_B
        val defaultMode = ClockGapMode.DIRECT

        _uiState.update {
            it.copy(
                timeA = defaultA,
                timeB = defaultB,
                mode = defaultMode,
                result = null,
                errorMessage = null,
                isCalculating = false
            )
        }
        _calculationState.value = CalculationState.Idle

        if (_settings.value.preserveLastTimes) {
            settingsRepository.saveLastInputs(defaultA, defaultB, defaultMode)
        }
    }

    fun restoreFromHistory(history: ClockGapHistory) {
        val result = ClockGapCalculator.calculate(history.timeA, history.timeB, history.mode)
        _uiState.update {
            it.copy(
                timeA = history.timeA,
                timeB = history.timeB,
                mode = history.mode,
                result = result,
                errorMessage = null
            )
        }
        persistCurrentInputsIfEnabled()
        _calculationState.value = CalculationState.Calculated
    }

    fun deleteHistoryItem(id: String) {
        val updated = historyRepository.deleteItem(id)
        _uiState.update { it.copy(history = updated) }
    }

    fun clearAllHistory() {
        historyRepository.clearHistory()
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun updateSettings(newSettings: AppSettings) {
        settingsRepository.saveSettings(newSettings)
        _settings.value = newSettings
    }

    fun resetSettings() {
        val reset = settingsRepository.resetSettings()
        _settings.value = reset
    }

    private fun persistCurrentInputsIfEnabled() {
        val current = _uiState.value
        if (_settings.value.preserveLastTimes) {
            settingsRepository.saveLastInputs(current.timeA, current.timeB, current.mode)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ClockGapViewModel(application) as T
                }
            }
    }
}
