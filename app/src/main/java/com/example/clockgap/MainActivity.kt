package com.example.clockgap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.clockgap.ui.ClockGapViewModel
import com.example.clockgap.ui.navigation.ClockGapApp
import com.example.clockgap.ui.theme.ClockGapTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ClockGapViewModel by viewModels {
        ClockGapViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()

            ClockGapTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                ClockGapApp(viewModel = viewModel)
            }
        }
    }
}