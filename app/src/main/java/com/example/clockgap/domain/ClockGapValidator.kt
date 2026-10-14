package com.example.clockgap.domain

import java.time.LocalTime

object ClockGapValidator {

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val message: String) : ValidationResult
    }

    fun validate(timeA: LocalTime?, timeB: LocalTime?): ValidationResult {
        if (timeA == null || timeB == null) {
            return ValidationResult.Invalid("Please select both times.")
        }
        return ValidationResult.Valid
    }
}
