package com.mahaesuvidha.chandrapanchangalarm.model

import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Nakshatra/Tara Bala calculation using the same Lahiri sidereal Swiss Ephemeris
 * convention already used by LiveMoonCalculator.
 */
object NakshatraGuidanceCalculator {
    private const val NAKSHATRA_SIZE = 360.0 / 27.0
    private val indiaTz = TimeZone.getTimeZone("Asia/Kolkata")

    private val names = arrayOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशीर्ष", "आर्द्रा", "पुनर्वसू", "पुष्य", "आश्लेषा",
        "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी", "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा",
        "मूळ", "पूर्वाषाढा", "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शततारका", "पूर्वाभाद्रपदा", "उत्तराभाद्रपदा", "रेवती"
    )

    enum class Tara(val marathi: String, val isWarning: Boolean) {
        JANMA("जन्म", false), SAMPAT("संपत", false), VIPAT("विपत", true), KSHEMA("क्षेम", false),
        PRATYARI("प्रत्यारी", true), SADHAKA("साधक", false), VADHA("वध", true), MITRA("मित्र", false),
        PARAM_MITRA("परम मित्र", false)
    }

    data class Guidance(
        val nakshatra: String,
        val tara: Tara,
        val startMillis: Long,
        val endMillis: Long,
        val doText: String,
        val avoidText: String
    )

    private fun swe(): SwissEph = SwissEph().also {
        it.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0.0, 0.0)
    }

    private fun longitudeAt(millis: Long, eph: SwissEph): Double {
        val jd = 2440587.5 + millis / 86400000.0
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        eph.swe_calc_ut(jd, SweConst.SE_MOON,
            SweConst.SEFLG_SWIEPH or SweConst.SEFLG_SIDEREAL, xx, serr)
        var value = xx[0] % 360.0
        if (value < 0) value += 360.0
        return value
    }

    fun nakshatraIndexAt(millis: Long): Int =
        (longitudeAt(millis, swe()) / NAKSHATRA_SIZE).toInt().coerceIn(0, 26)

    fun nakshatraNameAt(millis: Long): String = names[nakshatraIndexAt(millis)]

    fun calculateBirthNakshatra(birthDate: String, birthTime: String): String {
        val d = birthDate.trim().split("/")
        require(d.size == 3) { "जन्मतारीख DD/MM/YYYY मध्ये भरा" }
        val t = birthTime.trim().split(":")
        require(t.size == 2) { "जन्मवेळ 00:00 या पद्धतीने भरा" }
        val cal = java.util.Calendar.getInstance(indiaTz)
        cal.isLenient = false
        cal.set(d[2].toInt(), d[1].toInt() - 1, d[0].toInt(), t[0].toInt(), t[1].toInt(), 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis // force validation
        return nakshatraNameAt(cal.timeInMillis)
    }

    fun taraFor(birthNakshatra: String, currentNakshatra: String): Tara {
        val birth = names.indexOf(birthNakshatra)
        val current = names.indexOf(currentNakshatra)
        if (birth < 0 || current < 0) return Tara.JANMA
        val distance = (current - birth + 27) % 27 + 1 // birth itself = 1
        return Tara.entries[(distance - 1) % 9]
    }

    private fun advice(tara: Tara): Pair<String, String> = when (tara) {
        Tara.JANMA -> "सामान्य व आवश्यक कामे, स्वतःशी संबंधित कामे." to "अनावश्यक ताण व घाई टाळावी."
        Tara.SAMPAT -> "आर्थिक, व्यवहारिक व प्रगतीची कामे." to "अनावश्यक खर्च टाळावा."
        Tara.VIPAT -> "आवश्यक व नियमित कामे शांतपणे करावीत." to "मोठे आर्थिक निर्णय, जोखमीची कामे व महत्त्वाचे नवीन उपक्रम टाळावेत."
        Tara.KSHEMA -> "आरोग्य, घरगुती व सुरक्षिततेशी संबंधित कामे." to "अति जोखीम व घाई टाळावी."
        Tara.PRATYARI -> "फक्त आवश्यक कामे; कागदपत्रे पुन्हा तपासून काम करावे." to "वाद, मोठे करार, महत्त्वाचे निर्णय व नवीन उपक्रम टाळावेत."
        Tara.SADHAKA -> "महत्त्वाची कामे, अभ्यास, नियोजन व प्रयत्नांची कामे." to "आळस व काम पुढे ढकलणे टाळावे."
        Tara.VADHA -> "अत्यावश्यक कामेच प्राधान्याने करावीत." to "मोठे निर्णय, नवीन उपक्रम, जोखमीची कामे व अनावश्यक प्रवास टाळावा."
        Tara.MITRA -> "सामाजिक, सहकार्याची व नियमित कामे." to "अनावश्यक मतभेद टाळावेत."
        Tara.PARAM_MITRA -> "महत्त्वाची व शुभ कामे पुढे नेता येतात." to "अति आत्मविश्वास व घाई टाळावी."
    }

    private fun boundary(now: Long, targetIndex: Int, forward: Boolean): Long {
        val eph = swe()
        val step = 2L * 60 * 60 * 1000
        if (forward) {
            var low = now
            var high = now + step
            while (nakshatraIndexAtWith(eph, high) == targetIndex) high += step
            repeat(24) {
                val mid = low + (high - low) / 2
                if (nakshatraIndexAtWith(eph, mid) == targetIndex) low = mid else high = mid
            }
            return high
        } else {
            var high = now
            var low = now - step
            while (nakshatraIndexAtWith(eph, low) == targetIndex) low -= step
            repeat(24) {
                val mid = low + (high - low) / 2
                if (nakshatraIndexAtWith(eph, mid) == targetIndex) high = mid else low = mid
            }
            return high
        }
    }

    private fun nakshatraIndexAtWith(eph: SwissEph, millis: Long): Int =
        (longitudeAt(millis, eph) / NAKSHATRA_SIZE).toInt().coerceIn(0, 26)

    fun currentGuidance(birthNakshatra: String, now: Long = System.currentTimeMillis()): Guidance {
        val index = nakshatraIndexAt(now)
        val name = names[index]
        val tara = taraFor(birthNakshatra, name)
        val start = boundary(now, index, false)
        val end = boundary(now, index, true)
        val a = advice(tara)
        return Guidance(name, tara, start, end, a.first, a.second)
    }

    fun upcomingGuidance(birthNakshatra: String, days: Int = 60, now: Long = System.currentTimeMillis()): List<Guidance> {
        val eph = swe()
        val result = mutableListOf<Guidance>()
        val end = now + days.toLong() * 86400000L
        val step = 2L * 60 * 60 * 1000
        var cursor = now
        var currentIndex = nakshatraIndexAtWith(eph, cursor)
        while (cursor < end && result.size < 80) {
            var probe = cursor + step
            while (probe < end && nakshatraIndexAtWith(eph, probe) == currentIndex) probe += step
            if (probe >= end) break
            var low = cursor
            var high = probe
            repeat(24) {
                val mid = low + (high - low) / 2
                if (nakshatraIndexAtWith(eph, mid) == currentIndex) low = mid else high = mid
            }
            val nextIndex = nakshatraIndexAtWith(eph, high)
            val tara = taraFor(birthNakshatra, names[nextIndex])
            val a = advice(tara)
            result += Guidance(names[nextIndex], tara, high, runEnd(eph, high, nextIndex, end), a.first, a.second)
            cursor = high + 1000L
            currentIndex = nextIndex
        }
        return result
    }

    private fun runEnd(eph: SwissEph, start: Long, index: Int, limit: Long): Long {
        val step = 2L * 60 * 60 * 1000
        var low = start
        var high = (start + step).coerceAtMost(limit)
        while (high < limit && nakshatraIndexAtWith(eph, high) == index) high += step
        if (high > limit) high = limit
        repeat(24) {
            val mid = low + (high - low) / 2
            if (nakshatraIndexAtWith(eph, mid) == index) low = mid else high = mid
        }
        return high
    }

    fun format(millis: Long): String = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).apply {
        timeZone = indiaTz
    }.format(Date(millis))
}
