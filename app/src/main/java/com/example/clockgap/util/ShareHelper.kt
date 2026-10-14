package com.example.clockgap.util

import android.content.Context
import android.content.Intent

object ShareHelper {
    fun shareText(context: Context, text: String, title: String = "Share ClockGap Result") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }
}
