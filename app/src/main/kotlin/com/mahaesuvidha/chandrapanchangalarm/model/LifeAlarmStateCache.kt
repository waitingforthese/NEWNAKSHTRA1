package com.mahaesuvidha.chandrapanchangalarm.model

import android.content.Context
import org.json.JSONObject

/**
 * Small on-device cache used only to render the last successful Panchang state
 * immediately while the fresh Swiss Ephemeris calculation runs in background.
 * The cache is never used as the source for new alarm scheduling.
 */
object LifeAlarmStateCache {

    private const val PREFS = "life_alarm_state_cache"
    private const val KEY_STATE = "state_json"

    data class CachedState(
        val moon: MoonState,
        val sun: SunState,
        val panchang: PanchangState
    )

    fun load(context: Context): CachedState? {
        return try {
            val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_STATE, null) ?: return null
            val root = JSONObject(raw)
            CachedState(
                moon = readMoon(root.getJSONObject("moon")),
                sun = readSun(root.getJSONObject("sun")),
                panchang = readPanchang(root.getJSONObject("panchang"))
            )
        } catch (_: Throwable) {
            null
        }
    }

    fun save(context: Context, moon: MoonState, sun: SunState, panchang: PanchangState) {
        try {
            val root = JSONObject()
                .put("moon", writeMoon(moon))
                .put("sun", writeSun(sun))
                .put("panchang", writePanchang(panchang))

            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_STATE, root.toString())
                .apply()
        } catch (_: Throwable) {
            // Cache must never affect the live calculation or app startup.
        }
    }

    private fun writeMoon(v: MoonState) = JSONObject()
        .put("location", v.location)
        .put("rashi", v.rashi.name)
        .put("nakshatra", v.nakshatra.name)
        .put("pada", v.pada)
        .put("nextRashi", v.nextRashi)
        .put("nextRashiTime", v.nextRashiTime)
        .put("nextRashiMillis", v.nextRashiMillis)
        .put("nakshatraStartTime", v.nakshatraStartTime)
        .put("nextNakshatra", v.nextNakshatra)
        .put("nextNakshatraTime", v.nextNakshatraTime)
        .put("nextNakshatraMillis", v.nextNakshatraMillis)
        .put("nextCharan", v.nextCharan)
        .put("nextCharanTime", v.nextCharanTime)
        .put("nextCharanMillis", v.nextCharanMillis)

    private fun readMoon(o: JSONObject) = MoonState(
        location = o.getString("location"),
        rashi = Rashi.valueOf(o.getString("rashi")),
        nakshatra = Nakshatra.valueOf(o.getString("nakshatra")),
        pada = o.getInt("pada"),
        nextRashi = o.getString("nextRashi"),
        nextRashiTime = o.getString("nextRashiTime"),
        nextRashiMillis = o.getLong("nextRashiMillis"),
        nakshatraStartTime = o.optString("nakshatraStartTime", "—"),
        nextNakshatra = o.getString("nextNakshatra"),
        nextNakshatraTime = o.getString("nextNakshatraTime"),
        nextNakshatraMillis = o.getLong("nextNakshatraMillis"),
        nextCharan = o.getString("nextCharan"),
        nextCharanTime = o.getString("nextCharanTime"),
        nextCharanMillis = o.getLong("nextCharanMillis")
    )

    private fun writeSun(v: SunState) = JSONObject()
        .put("rashi", v.rashi.name)
        .put("nakshatra", v.nakshatra.name)
        .put("pada", v.pada)
        .put("nextRashi", v.nextRashi)
        .put("nextRashiTime", v.nextRashiTime)
        .put("nextRashiMillis", v.nextRashiMillis)
        .put("nextNakshatra", v.nextNakshatra)
        .put("nextNakshatraTime", v.nextNakshatraTime)
        .put("nextNakshatraMillis", v.nextNakshatraMillis)
        .put("nextCharan", v.nextCharan)
        .put("nextCharanTime", v.nextCharanTime)
        .put("nextCharanMillis", v.nextCharanMillis)

    private fun readSun(o: JSONObject) = SunState(
        rashi = Rashi.valueOf(o.getString("rashi")),
        nakshatra = Nakshatra.valueOf(o.getString("nakshatra")),
        pada = o.getInt("pada"),
        nextRashi = o.getString("nextRashi"),
        nextRashiTime = o.getString("nextRashiTime"),
        nextRashiMillis = o.getLong("nextRashiMillis"),
        nextNakshatra = o.getString("nextNakshatra"),
        nextNakshatraTime = o.getString("nextNakshatraTime"),
        nextNakshatraMillis = o.getLong("nextNakshatraMillis"),
        nextCharan = o.getString("nextCharan"),
        nextCharanTime = o.getString("nextCharanTime"),
        nextCharanMillis = o.getLong("nextCharanMillis")
    )

    private fun writePanchang(v: PanchangState) = JSONObject()
        .put("date", v.date)
        .put("weekday", v.weekday)
        .put("tithi", v.tithi)
        .put("tithiStartTime", v.tithiStartTime)
        .put("nextTithi", v.nextTithi)
        .put("nextTithiTime", v.nextTithiTime)
        .put("nextTithiMillis", v.nextTithiMillis)
        .put("yoga", v.yoga)
        .put("yogaStartTime", v.yogaStartTime)
        .put("nextYoga", v.nextYoga)
        .put("nextYogaTime", v.nextYogaTime)
        .put("nextYogaMillis", v.nextYogaMillis)
        .put("karana", v.karana)
        .put("karanaStartTime", v.karanaStartTime)
        .put("nextKarana", v.nextKarana)
        .put("nextKaranaTime", v.nextKaranaTime)
        .put("nextKaranaMillis", v.nextKaranaMillis)
        .put("paksha", v.paksha)
        .put("pakshaStartTime", v.pakshaStartTime)
        .put("nextPaksha", v.nextPaksha)
        .put("nextPakshaTime", v.nextPakshaTime)
        .put("nextPakshaMillis", v.nextPakshaMillis)
        .put("masa", v.masa)
        .put("masaStartTime", v.masaStartTime)
        .put("nextMasa", v.nextMasa)
        .put("nextMasaTime", v.nextMasaTime)
        .put("nextMasaMillis", v.nextMasaMillis)
        .put("prahar", v.prahar)
        .put("praharStartTime", v.praharStartTime)
        .put("nextPrahar", v.nextPrahar)
        .put("nextPraharTime", v.nextPraharTime)
        .put("nextPraharMillis", v.nextPraharMillis)
        .put("lagna", v.lagna)
        .put("lagnaStartTime", v.lagnaStartTime)
        .put("nextLagna", v.nextLagna)
        .put("nextLagnaTime", v.nextLagnaTime)
        .put("nextLagnaMillis", v.nextLagnaMillis)

    private fun readPanchang(o: JSONObject) = PanchangState(
        date = o.getString("date"),
        weekday = o.getString("weekday"),
        tithi = o.getString("tithi"),
        tithiStartTime = o.getString("tithiStartTime"),
        nextTithi = o.getString("nextTithi"),
        nextTithiTime = o.getString("nextTithiTime"),
        nextTithiMillis = o.getLong("nextTithiMillis"),
        yoga = o.getString("yoga"),
        yogaStartTime = o.getString("yogaStartTime"),
        nextYoga = o.getString("nextYoga"),
        nextYogaTime = o.getString("nextYogaTime"),
        nextYogaMillis = o.getLong("nextYogaMillis"),
        karana = o.getString("karana"),
        karanaStartTime = o.getString("karanaStartTime"),
        nextKarana = o.getString("nextKarana"),
        nextKaranaTime = o.getString("nextKaranaTime"),
        nextKaranaMillis = o.getLong("nextKaranaMillis"),
        paksha = o.getString("paksha"),
        pakshaStartTime = o.getString("pakshaStartTime"),
        nextPaksha = o.getString("nextPaksha"),
        nextPakshaTime = o.getString("nextPakshaTime"),
        nextPakshaMillis = o.getLong("nextPakshaMillis"),
        masa = o.getString("masa"),
        masaStartTime = o.getString("masaStartTime"),
        nextMasa = o.getString("nextMasa"),
        nextMasaTime = o.getString("nextMasaTime"),
        nextMasaMillis = o.getLong("nextMasaMillis"),
        prahar = o.getString("prahar"),
        praharStartTime = o.getString("praharStartTime"),
        nextPrahar = o.getString("nextPrahar"),
        nextPraharTime = o.getString("nextPraharTime"),
        nextPraharMillis = o.getLong("nextPraharMillis"),
        lagna = o.getString("lagna"),
        lagnaStartTime = o.getString("lagnaStartTime"),
        nextLagna = o.getString("nextLagna"),
        nextLagnaTime = o.getString("nextLagnaTime"),
        nextLagnaMillis = o.getLong("nextLagnaMillis")
    )
}
