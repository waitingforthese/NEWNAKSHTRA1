package com.mahaesuvidha.chandrapanchangalarm.settings

import android.content.Context

class LocationPrefs(
    context: Context
) {
    private val p =
        context.getSharedPreferences(
            "life_alarm_location",
            Context.MODE_PRIVATE
        )

    var latitude: Double
        get() = p.getString("latitude", null)?.toDoubleOrNull()
            ?: 18.5204
        set(value) {
            p.edit().putString("latitude", value.toString()).apply()
        }

    var longitude: Double
        get() = p.getString("longitude", null)?.toDoubleOrNull()
            ?: 73.8567
        set(value) {
            p.edit().putString("longitude", value.toString()).apply()
        }

    var hasLiveLocation: Boolean
        get() = p.getBoolean("has_live_location", false)
        set(value) {
            p.edit().putBoolean("has_live_location", value).apply()
        }
}
