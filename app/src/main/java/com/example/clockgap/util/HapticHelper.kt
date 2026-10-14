package com.example.clockgap.util

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticHelper {
    fun trigger(haptics: HapticFeedback?, enabled: Boolean) {
        if (enabled && haptics != null) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
