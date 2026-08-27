package com.mahaesuvidha.chandrapanchangalarm.model

import android.content.Context

data class BirthProfile(
    val name: String,
    val birthDate: String,
    val gender: String,
    val birthTime: String,
    val birthPlace: String,
    val birthMoonRashi: String,
    val birthNakshatra: String = ""
)

object BirthProfileStore {
    private const val PREFS = "birth_profile"
    private const val KEY_NAME = "name"
    private const val KEY_DATE = "birth_date"
    private const val KEY_GENDER = "gender"
    private const val KEY_TIME = "birth_time"
    private const val KEY_PLACE = "birth_place"
    private const val KEY_MOON_RASHI = "birth_moon_rashi"
    private const val KEY_NAKSHATRA = "birth_nakshatra"

    fun save(context: Context, profile: BirthProfile) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAME, profile.name)
            .putString(KEY_DATE, profile.birthDate)
            .putString(KEY_GENDER, profile.gender)
            .putString(KEY_TIME, profile.birthTime)
            .putString(KEY_PLACE, profile.birthPlace)
            .putString(KEY_MOON_RASHI, profile.birthMoonRashi)
            .putString(KEY_NAKSHATRA, profile.birthNakshatra)
            .apply()
    }

    fun load(context: Context): BirthProfile? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = p.getString(KEY_NAME, null) ?: return null
        val date = p.getString(KEY_DATE, null) ?: return null
        val gender = p.getString(KEY_GENDER, null) ?: return null
        val time = p.getString(KEY_TIME, null) ?: return null
        val place = p.getString(KEY_PLACE, null) ?: return null
        val moonRashi = p.getString(KEY_MOON_RASHI, null) ?: return null
        val nakshatra = p.getString(KEY_NAKSHATRA, null) ?: ""
        return BirthProfile(name, date, gender, time, place, moonRashi, nakshatra)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
