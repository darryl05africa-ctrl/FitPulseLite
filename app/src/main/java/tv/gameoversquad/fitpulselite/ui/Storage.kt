package tv.gameoversquad.fitpulselite.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class LogItem(
    val title: String,
    val minutes: Int,
    val timestampMs: Long
)

private const val PREF = "fitpulse_lite"
private const val KEY_GOAL = "daily_goal_minutes"
private const val KEY_LOGS = "logs_json"

fun loadGoalMinutes(context: Context): Int {
    val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    return sp.getInt(KEY_GOAL, 30)
}

fun saveGoalMinutes(context: Context, value: Int) {
    val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    sp.edit().putInt(KEY_GOAL, value).apply()
}

fun loadLogs(context: Context): List<LogItem> {
    val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    val raw = sp.getString(KEY_LOGS, "[]") ?: "[]"
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            LogItem(
                title = o.optString("title", "Workout"),
                minutes = o.optInt("minutes", 0),
                timestampMs = o.optLong("ts", 0L)
            )
        }.sortedByDescending { it.timestampMs }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveLogs(context: Context, logs: List<LogItem>) {
    val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
    val arr = JSONArray()
    logs.forEach {
        val o = JSONObject()
        o.put("title", it.title)
        o.put("minutes", it.minutes)
        o.put("ts", it.timestampMs)
        arr.put(o)
    }
    sp.edit().putString(KEY_LOGS, arr.toString()).apply()
}
