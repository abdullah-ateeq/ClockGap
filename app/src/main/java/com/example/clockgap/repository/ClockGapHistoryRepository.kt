package com.example.clockgap.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.clockgap.model.ClockGapHistory
import com.example.clockgap.model.ClockGapMode
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime
import java.util.UUID

class ClockGapHistoryRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    companion object {
        private const val PREFS_NAME = "clock_gap_history_prefs"
        private const val KEY_HISTORY = "history_entries"
        const val MAX_HISTORY_ITEMS = 10
    }

    @Synchronized
    fun getHistory(): List<ClockGapHistory> {
        val rawJson = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<ClockGapHistory>()
        try {
            val jsonArray = JSONArray(rawJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val item = ClockGapHistory(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    timeA = LocalTime.parse(obj.getString("timeA")),
                    timeB = LocalTime.parse(obj.getString("timeB")),
                    mode = ClockGapMode.valueOf(obj.getString("mode")),
                    gapSeconds = obj.getLong("gapSeconds"),
                    calculatedAt = obj.getLong("calculatedAt")
                )
                list.add(item)
            }
        } catch (_: Exception) {
            // Corrupt or outdated data fallback
        }
        return list
    }

    @Synchronized
    fun addCalculation(
        timeA: LocalTime,
        timeB: LocalTime,
        mode: ClockGapMode,
        gapSeconds: Long
    ): List<ClockGapHistory> {
        val currentList = getHistory().toMutableList()

        // Rapid input & duplicate protection:
        // Do not add if identical to the most recent calculation
        if (currentList.isNotEmpty()) {
            val latest = currentList.first()
            if (latest.timeA == timeA &&
                latest.timeB == timeB &&
                latest.mode == mode &&
                latest.gapSeconds == gapSeconds
            ) {
                return currentList
            }
        }

        val newItem = ClockGapHistory(
            id = UUID.randomUUID().toString(),
            timeA = timeA,
            timeB = timeB,
            mode = mode,
            gapSeconds = gapSeconds,
            calculatedAt = System.currentTimeMillis()
        )

        // Add to front (newest first)
        currentList.add(0, newItem)

        // Enforce limit of 10 items (remove oldest from end)
        while (currentList.size > MAX_HISTORY_ITEMS) {
            currentList.removeAt(currentList.lastIndex)
        }

        saveList(currentList)
        return currentList
    }

    @Synchronized
    fun deleteItem(id: String): List<ClockGapHistory> {
        val currentList = getHistory().toMutableList()
        currentList.removeAll { it.id == id }
        saveList(currentList)
        return currentList
    }

    @Synchronized
    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveList(list: List<ClockGapHistory>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("timeA", item.timeA.toString())
                put("timeB", item.timeB.toString())
                put("mode", item.mode.name)
                put("gapSeconds", item.gapSeconds)
                put("calculatedAt", item.calculatedAt)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }
}
