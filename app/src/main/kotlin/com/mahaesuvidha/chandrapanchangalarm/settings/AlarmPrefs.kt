package com.mahaesuvidha.chandrapanchangalarm.settings

import android.content.Context

class AlarmPrefs(
    context: Context
) {

    private val p =
        context.getSharedPreferences(
            "alarm_prefs",
            Context.MODE_PRIVATE
        )

    // ==========================================
    // MOON / SUN
    // ==========================================

    var moon: Boolean
        get() = p.getBoolean(
            "moon",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "moon",
                    value
                )
                .apply()
        }

    var sun: Boolean
        get() = p.getBoolean(
            "sun",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "sun",
                    value
                )
                .apply()
        }

    // ==========================================
    // PLANET CHANGE ALARMS
    // ==========================================

    var rashi: Boolean
        get() = p.getBoolean(
            "rashi",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "rashi",
                    value
                )
                .apply()
        }

    var nak: Boolean
        get() = p.getBoolean(
            "nak",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "nak",
                    value
                )
                .apply()
        }

    var pada: Boolean
        get() = p.getBoolean(
            "pada",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "pada",
                    value
                )
                .apply()
        }

    // ==========================================
    // PANCHANG MASTER
    // ==========================================

    var panchang: Boolean
        get() = p.getBoolean(
            "panchang",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "panchang",
                    value
                )
                .apply()
        }

    // ==========================================
    // TITHI
    // ==========================================

    var tithi: Boolean
        get() = p.getBoolean(
            "tithi",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "tithi",
                    value
                )
                .apply()
        }

    // ==========================================
    // YOGA
    // ==========================================

    var yoga: Boolean
        get() = p.getBoolean(
            "yoga",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "yoga",
                    value
                )
                .apply()
        }

    // ==========================================
    // KARANA
    // ==========================================

    var karana: Boolean
        get() = p.getBoolean(
            "karana",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "karana",
                    value
                )
                .apply()
        }

    // ==========================================
    // PAKSHA
    // ==========================================

    var paksha: Boolean
        get() = p.getBoolean(
            "paksha",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "paksha",
                    value
                )
                .apply()
        }

    // ==========================================
    // MASA
    // ==========================================

    var masa: Boolean
        get() = p.getBoolean(
            "masa",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "masa",
                    value
                )
                .apply()
        }

    // ==========================================
    // PRAHAR
    // ==========================================

    var prahar: Boolean
        get() = p.getBoolean(
            "prahar",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "prahar",
                    value
                )
                .apply()
        }

    // ==========================================
    // LAGNA
    // ==========================================

    var lagna: Boolean
        get() = p.getBoolean(
            "lagna",
            true
        )
        set(value) {
            p.edit()
                .putBoolean(
                    "lagna",
                    value
                )
                .apply()
        }
    // ==========================================
    // 12 INDIVIDUAL ALARM SWITCHES
    // ==========================================

    var moonRashi: Boolean
        get() = p.getBoolean("moon_rashi", rashi)
        set(value) { p.edit().putBoolean("moon_rashi", value).apply() }

    var moonNakshatra: Boolean
        get() = p.getBoolean("moon_nakshatra", nak)
        set(value) { p.edit().putBoolean("moon_nakshatra", value).apply() }

    var moonCharan: Boolean
        get() = p.getBoolean("moon_charan", pada)
        set(value) { p.edit().putBoolean("moon_charan", value).apply() }

    var sunRashi: Boolean
        get() = p.getBoolean("sun_rashi", rashi)
        set(value) { p.edit().putBoolean("sun_rashi", value).apply() }

    var sunNakshatra: Boolean
        get() = p.getBoolean("sun_nakshatra", nak)
        set(value) { p.edit().putBoolean("sun_nakshatra", value).apply() }

    var sunCharan: Boolean
        get() = p.getBoolean("sun_charan", pada)
        set(value) { p.edit().putBoolean("sun_charan", value).apply() }

    var tithiAlarm: Boolean
        get() = p.getBoolean("tithi_alarm", tithi)
        set(value) { p.edit().putBoolean("tithi_alarm", value).apply() }

    var yogaAlarm: Boolean
        get() = p.getBoolean("yoga_alarm", yoga)
        set(value) { p.edit().putBoolean("yoga_alarm", value).apply() }

    var karanaAlarm: Boolean
        get() = p.getBoolean("karana_alarm", karana)
        set(value) { p.edit().putBoolean("karana_alarm", value).apply() }

    var pakshaAlarm: Boolean
        get() = p.getBoolean("paksha_alarm", paksha)
        set(value) { p.edit().putBoolean("paksha_alarm", value).apply() }

    var praharAlarm: Boolean
        get() = p.getBoolean("prahar_alarm", prahar)
        set(value) { p.edit().putBoolean("prahar_alarm", value).apply() }

    var lagnaAlarm: Boolean
        get() = p.getBoolean("lagna_alarm", lagna)
        set(value) { p.edit().putBoolean("lagna_alarm", value).apply() }

}
