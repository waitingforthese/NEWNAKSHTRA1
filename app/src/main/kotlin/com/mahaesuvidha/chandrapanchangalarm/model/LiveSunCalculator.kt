package com.mahaesuvidha.chandrapanchangalarm.model

import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object LiveSunCalculator {

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
    // CURRENT JULIAN DAY
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
    // SUN LONGITUDE
    // ==========================================

    private fun getSunLongitude(
        millis: Long = System.currentTimeMillis(),
        swe: SwissEph = createSwissEph()
    ): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        swe.swe_calc_ut(
            getJulianDay(millis),
            SweConst.SE_SUN,
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
    // CURRENT RASHI INDEX
    // ==========================================

    private fun getRashiIndex(
        longitude: Double
    ): Int {

        return (longitude / 30.0)
            .toInt()
            .coerceIn(0, 11)
    }


    // ==========================================
    // CURRENT NAKSHATRA INDEX
    // ==========================================

    private fun getNakshatraIndex(
        longitude: Double
    ): Int {

        val size =
            360.0 / 27.0

        return (longitude / size)
            .toInt()
            .coerceIn(0, 26)
    }


    // ==========================================
    // CURRENT PADA
    // ==========================================

    private fun getPada(
        longitude: Double
    ): Int {

        val nakshatraSize =
            360.0 / 27.0

        val padaSize =
            nakshatraSize / 4.0

        val position =
            longitude % nakshatraSize

        return (
            position / padaSize
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
    // FIND NEXT RASHI CHANGE
    // ==========================================

    private fun findNextRashiChange(
        now: Long,
        currentRashi: Int
    ): Pair<String, Long> {
        val swe = createSwissEph()
        val step = 12L * 60 * 60 * 1000
        var previous = now
        repeat(70) {
            val current = previous + step
            val index = getRashiIndex(getSunLongitude(current, swe))
            if (index != currentRashi) {
                var low = previous
                var high = current
                repeat(22) {
                    val middle = low + (high - low) / 2
                    if (getRashiIndex(getSunLongitude(middle, swe)) == currentRashi) low = middle else high = middle
                }
                return Pair("${rashiNames[currentRashi]} → ${rashiNames[index]}", high)
            }
            previous = current
        }
        return Pair("पुढील राशी बदल शोधत आहे", now + 35L * 24 * 60 * 60 * 1000)
    }


    // ==========================================
    // FIND NEXT NAKSHATRA CHANGE
    // ==========================================

    private fun findNextNakshatraChange(
        now: Long,
        currentNakshatra: Int
    ): Pair<String, Long> {
        val swe = createSwissEph()
        val step = 6L * 60 * 60 * 1000
        var previous = now
        repeat(100) {
            val current = previous + step
            val index = getNakshatraIndex(getSunLongitude(current, swe))
            if (index != currentNakshatra) {
                var low = previous
                var high = current
                repeat(22) {
                    val middle = low + (high - low) / 2
                    if (getNakshatraIndex(getSunLongitude(middle, swe)) == currentNakshatra) low = middle else high = middle
                }
                return Pair("${nakshatraNames[currentNakshatra]} → ${nakshatraNames[index]}", high)
            }
            previous = current
        }
        return Pair("पुढील नक्षत्र बदल शोधत आहे", now + 20L * 24 * 60 * 60 * 1000)
    }


    // ==========================================
    // FIND NEXT PADA CHANGE
    // ==========================================

    private fun findNextPadaChange(
        now: Long,
        currentNakshatra: Int,
        currentPada: Int
    ): Pair<String, Long> {
        val swe = createSwissEph()
        val step = 3L * 60 * 60 * 1000
        var previous = now

        fun padaAt(millis: Long): Pair<Int, Int> {
            val longitude = getSunLongitude(millis, swe)
            return Pair(getNakshatraIndex(longitude), getPada(longitude))
        }

        repeat(50) {
            val current = previous + step
            val value = padaAt(current)
            if (value.first != currentNakshatra || value.second != currentPada) {
                var low = previous
                var high = current
                repeat(22) {
                    val middle = low + (high - low) / 2
                    val valueMid = padaAt(middle)
                    if (valueMid.first == currentNakshatra && valueMid.second == currentPada) low = middle else high = middle
                }
                val nextPada = if (value.first != currentNakshatra) 1 else value.second
                return Pair("चरण $currentPada → चरण $nextPada", high)
            }
            previous = current
        }
        return Pair("पुढील चरण बदल शोधत आहे", now + 5L * 24 * 60 * 60 * 1000)
    }


    // ==========================================
    // MAIN FUNCTION
    // ==========================================

    fun getCurrentSunState(): SunState {

        val now =
            System.currentTimeMillis()

        val longitude =
            getSunLongitude(now)

        val currentRashiIndex =
            getRashiIndex(longitude)

        val currentNakshatraIndex =
            getNakshatraIndex(longitude)

        val currentPada =
            getPada(longitude)


        // NEXT RASHI

        val nextRashi =
            findNextRashiChange(
                now,
                currentRashiIndex
            )


        // NEXT NAKSHATRA

        val nextNakshatra =
            findNextNakshatraChange(
                now,
                currentNakshatraIndex
            )


        // NEXT PADA

        val nextCharan =
            findNextPadaChange(
                now,
                currentNakshatraIndex,
                currentPada
            )


        return SunState(

            rashi =
                Rashi.entries[
                    currentRashiIndex
                ],

            nakshatra =
                Nakshatra.entries[
                    currentNakshatraIndex
                ],

            pada =
                currentPada,


            // RASHI

            nextRashi =
                nextRashi.first,

            nextRashiTime =
                formatDateTime(
                    nextRashi.second
                ),

            nextRashiMillis =
                nextRashi.second,


            // NAKSHATRA

            nextNakshatra =
                nextNakshatra.first,

            nextNakshatraTime =
                formatDateTime(
                    nextNakshatra.second
                ),

            nextNakshatraMillis =
                nextNakshatra.second,


            // CHARAN

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
