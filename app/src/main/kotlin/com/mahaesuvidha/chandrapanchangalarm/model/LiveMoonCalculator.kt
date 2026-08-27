package com.mahaesuvidha.chandrapanchangalarm.model

import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object LiveMoonCalculator {

    // ==========================================
    // RASHI NAMES
    // ==========================================

    private val rashiNames = arrayOf(
        "मेष",
        "वृषभ",
        "मिथुन",
        "कर्क",
        "सिंह",
        "कन्या",
        "तुळ",
        "वृश्चिक",
        "धनु",
        "मकर",
        "कुंभ",
        "मीन"
    )

    // ==========================================
    // NAKSHATRA NAMES
    // ==========================================

    private val nakshatraNames = arrayOf(
        "अश्विनी",
        "भरणी",
        "कृत्तिका",
        "रोहिणी",
        "मृगशीर्ष",
        "आर्द्रा",
        "पुनर्वसू",
        "पुष्य",
        "आश्लेषा",
        "मघा",
        "पूर्वाफाल्गुनी",
        "उत्तराफाल्गुनी",
        "हस्त",
        "चित्रा",
        "स्वाती",
        "विशाखा",
        "अनुराधा",
        "ज्येष्ठा",
        "मूळ",
        "पूर्वाषाढा",
        "उत्तराषाढा",
        "श्रवण",
        "धनिष्ठा",
        "शततारका",
        "पूर्वाभाद्रपदा",
        "उत्तराभाद्रपदा",
        "रेवती"
    )

    // ==========================================
    // DATE → JULIAN DAY
    // ==========================================

    private fun getJulianDay(
        millis: Long = System.currentTimeMillis()
    ): Double {

        val calendar =
            Calendar.getInstance(
                TimeZone.getTimeZone("UTC")
            )

        calendar.timeInMillis =
            millis

        val year =
            calendar.get(Calendar.YEAR)

        val month =
            calendar.get(Calendar.MONTH) + 1

        val day =
            calendar.get(Calendar.DAY_OF_MONTH)

        val hour =
            calendar.get(Calendar.HOUR_OF_DAY) +
                    calendar.get(Calendar.MINUTE) / 60.0 +
                    calendar.get(Calendar.SECOND) / 3600.0 +
                    calendar.get(Calendar.MILLISECOND) / 3600000.0

        return SweDate.getJulDay(
            year,
            month,
            day,
            hour,
            SweDate.SE_GREG_CAL
        )
    }

    // ==========================================
    // MOON LONGITUDE
    // ==========================================

    private fun getMoonLongitude(
        millis: Long = System.currentTimeMillis(),
        swe: SwissEph = createSwissEph()
    ): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        swe.swe_calc_ut(
            getJulianDay(millis),
            SweConst.SE_MOON,
            SweConst.SEFLG_SWIEPH or SweConst.SEFLG_SIDEREAL,
            xx,
            serr
        )
        return xx[0]
    }

    private fun createSwissEph(): SwissEph {
        val swe = SwissEph()
        swe.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0.0, 0.0)
        return swe
    }

    // ==========================================
    // CURRENT RASHI
    // ==========================================

    fun getCurrentRashi(): String {

        val longitude =
            getMoonLongitude()

        val index =
            (longitude / 30.0)
                .toInt()
                .coerceIn(0, 11)

        return rashiNames[index]
    }

    // ==========================================
    // CURRENT NAKSHATRA
    // ==========================================

    fun getCurrentNakshatra(): String {

        val longitude =
            getMoonLongitude()

        val nakshatraSize =
            360.0 / 27.0

        val index =
            (longitude / nakshatraSize)
                .toInt()
                .coerceIn(0, 26)

        return nakshatraNames[index]
    }

    // ==========================================
    // CURRENT CHARAN
    // ==========================================

    fun getCurrentCharan(): Int {

        val longitude =
            getMoonLongitude()

        val nakshatraSize =
            360.0 / 27.0

        val padaSize =
            nakshatraSize / 4.0

        return (
            (longitude % nakshatraSize) /
                    padaSize
        ).toInt()
            .coerceIn(0, 3) + 1
    }

    // ==========================================
    // FORMAT TIME
    // ==========================================

    private fun formatDateTime(
        millis: Long
    ): String {

        val formatter =
            SimpleDateFormat(
                "dd-MM-yyyy HH:mm",
                Locale.getDefault()
            )

        formatter.timeZone =
            TimeZone.getDefault()

        return formatter.format(
            millis
        )
    }

    // ==========================================
    // FIND NEXT RASHI
    // ==========================================

    private fun findNextRashiChange(
        now: Long,
        currentRashi: Int
    ): Pair<String, Long> {
        val swe = createSwissEph()
        val step = 6L * 60 * 60 * 1000
        var previous = now
        var previousIndex = currentRashi

        repeat(16) {
            val current = previous + step
            val index = (getMoonLongitude(current, swe) / 30.0).toInt().coerceIn(0, 11)
            if (index != previousIndex) {
                var low = previous
                var high = current
                repeat(20) {
                    val middle = low + (high - low) / 2
                    val middleIndex = (getMoonLongitude(middle, swe) / 30.0).toInt().coerceIn(0, 11)
                    if (middleIndex == previousIndex) low = middle else high = middle
                }
                return Pair("${rashiNames[currentRashi]} → ${rashiNames[index]}", high)
            }
            previous = current
        }
        return Pair("पुढील राशी बदल शोधत आहे", now + 3L * 24 * 60 * 60 * 1000)
    }

    // ==========================================
    // FIND CURRENT NAKSHATRA START
    // ==========================================

    private fun findCurrentNakshatraStart(
        now: Long,
        currentNakshatra: Int
    ): Long {
        val swe = createSwissEph()
        val step = 2L * 60 * 60 * 1000
        val size = 360.0 / 27.0

        fun indexAt(millis: Long): Int =
            (getMoonLongitude(millis, swe) / size)
                .toInt()
                .coerceIn(0, 26)

        var high = now
        var low = now - step

        repeat(36) {
            if (indexAt(low) != currentNakshatra) {
                repeat(22) {
                    val middle = low + (high - low) / 2
                    if (indexAt(middle) == currentNakshatra) {
                        high = middle
                    } else {
                        low = middle
                    }
                }
                return high
            }
            high = low
            low -= step
        }

        return now
    }

    // ==========================================
    // FIND NEXT NAKSHATRA
    // ==========================================

    private fun findNextNakshatraChange(
        now: Long,
        currentNakshatra: Int
    ): Pair<String, Long> {
        val swe = createSwissEph()
        val step = 2L * 60 * 60 * 1000
        val size = 360.0 / 27.0
        var previous = now
        repeat(36) {
            val current = previous + step
            val index = (getMoonLongitude(current, swe) / size).toInt().coerceIn(0, 26)
            if (index != currentNakshatra) {
                var low = previous
                var high = current
                repeat(20) {
                    val middle = low + (high - low) / 2
                    val middleIndex = (getMoonLongitude(middle, swe) / size).toInt().coerceIn(0, 26)
                    if (middleIndex == currentNakshatra) low = middle else high = middle
                }
                return Pair("${nakshatraNames[currentNakshatra]} → ${nakshatraNames[index]}", high)
            }
            previous = current
        }
        return Pair("पुढील नक्षत्र बदल शोधत आहे", now + 2L * 24 * 60 * 60 * 1000)
    }

    // ==========================================
    // FIND NEXT CHARAN
    // ==========================================

    private fun findNextCharanChange(
        now: Long,
        currentNakshatra: Int,
        currentCharan: Int
    ): Pair<String, Long> {
        val swe = createSwissEph()
        val step = 60L * 60 * 1000
        val nakshatraSize = 360.0 / 27.0
        val padaSize = nakshatraSize / 4.0

        fun charanAt(millis: Long): Pair<Int, Int> {
            val longitude = getMoonLongitude(millis, swe)
            val nak = (longitude / nakshatraSize).toInt().coerceIn(0, 26)
            val pada = ((longitude % nakshatraSize) / padaSize).toInt().coerceIn(0, 3) + 1
            return Pair(nak, pada)
        }

        var previous = now
        repeat(16) {
            val current = previous + step
            val value = charanAt(current)
            if (value.first != currentNakshatra || value.second != currentCharan) {
                var low = previous
                var high = current
                repeat(20) {
                    val middle = low + (high - low) / 2
                    val middleValue = charanAt(middle)
                    if (middleValue.first == currentNakshatra && middleValue.second == currentCharan) {
                        low = middle
                    } else {
                        high = middle
                    }
                }
                val nextCharan = if (value.first != currentNakshatra) 1 else value.second
                return Pair("चरण $currentCharan → चरण $nextCharan", high)
            }
            previous = current
        }
        return Pair("पुढील चरण बदल शोधत आहे", now + 8L * 60 * 60 * 1000)
    }

    // ==========================================
    // MAIN MOON STATE
    // ==========================================

    fun getCurrentMoonState(): MoonState {

        val now =
            System.currentTimeMillis()

        val longitude =
            getMoonLongitude(now)

        val rashiIndex =
            (longitude / 30.0)
                .toInt()
                .coerceIn(0, 11)

        val nakshatraSize =
            360.0 / 27.0

        val nakshatraIndex =
            (longitude / nakshatraSize)
                .toInt()
                .coerceIn(0, 26)

        val padaSize =
            nakshatraSize / 4.0

        val currentPada =
            (
                (longitude % nakshatraSize) /
                        padaSize
            ).toInt()
                .coerceIn(0, 3) + 1

        val nextRashi =
            findNextRashiChange(
                now,
                rashiIndex
            )

        val nakshatraStartMillis =
            findCurrentNakshatraStart(
                now,
                nakshatraIndex
            )

        val nextNakshatra =
            findNextNakshatraChange(
                now,
                nakshatraIndex
            )

        val nextCharan =
            findNextCharanChange(
                now,
                nakshatraIndex,
                currentPada
            )

        return MoonState(

            location = "भारत",

            rashi =
                Rashi.entries[rashiIndex],

            nakshatra =
                Nakshatra.entries[nakshatraIndex],

            pada =
                currentPada,

            nextRashi =
                nextRashi.first,

            nextRashiTime =
                formatDateTime(
                    nextRashi.second
                ),

            nextRashiMillis =
                nextRashi.second,

            nakshatraStartTime =
                formatDateTime(
                    nakshatraStartMillis
                ),

            nextNakshatra =
                nextNakshatra.first,

            nextNakshatraTime =
                formatDateTime(
                    nextNakshatra.second
                ),

            nextNakshatraMillis =
                nextNakshatra.second,

            nextCharan =
                nextCharan.first,

            nextCharanTime =
                formatDateTime(
                    nextCharan.second
                ),

            nextCharanMillis =
                nextCharan.second
        )
    }
}
