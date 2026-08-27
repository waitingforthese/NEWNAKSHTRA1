package com.mahaesuvidha.chandrapanchangalarm.model

import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

object LivePanchangCalculator {

    private const val TITHI_SIZE = 12.0
    private const val KARANA_SIZE = 6.0
    private const val YOGA_SIZE = 360.0 / 27.0

    private val indiaTimeZone =
        TimeZone.getTimeZone("Asia/Kolkata")


    // ==========================================
    // CURRENT JULIAN DAY
    // ==========================================

    private fun getJulianDay(): Double {

        val calendar =
            Calendar.getInstance(
                TimeZone.getTimeZone("UTC")
            )

        val year =
            calendar.get(Calendar.YEAR)

        val month =
            calendar.get(Calendar.MONTH) + 1

        val day =
            calendar.get(Calendar.DAY_OF_MONTH)

        val hour =
            calendar.get(Calendar.HOUR_OF_DAY) +
                    calendar.get(Calendar.MINUTE) / 60.0 +
                    calendar.get(Calendar.SECOND) / 3600.0

        return SweDate.getJulDay(
            year,
            month,
            day,
            hour,
            SweDate.SE_GREG_CAL
        )
    }


    // ==========================================
    // PLANET LONGITUDE
    // ==========================================

    private fun createSwissEph(): SwissEph {
        val swe = SwissEph()
        swe.swe_set_sid_mode(
            SweConst.SE_SIDM_LAHIRI,
            0.0,
            0.0
        )
        return swe
    }

    private fun getLongitude(
        planet: Int,
        jd: Double,
        swe: SwissEph
    ): Double {

        val xx = DoubleArray(6)
        val serr = StringBuffer()

        swe.swe_calc_ut(
            jd,
            planet,
            SweConst.SEFLG_SWIEPH or
                    SweConst.SEFLG_SIDEREAL,
            xx,
            serr
        )

        return normalize(xx[0])
    }

    private fun getSunMoon(
        millis: Long,
        swe: SwissEph
    ): Pair<Double, Double> {

        val jd =
            2440587.5 +
                    millis / 86400000.0

        return Pair(
            getLongitude(SweConst.SE_SUN, jd, swe),
            getLongitude(SweConst.SE_MOON, jd, swe)
        )
    }

    // ==========================================
    // NORMALIZE
    // ==========================================

    private fun normalize(value: Double): Double {

        var result = value % 360.0

        if (result < 0) {
            result += 360.0
        }

        return result
    }


    // ==========================================
    // INDEX CALCULATIONS
    // ==========================================

    private fun getTithiIndex(
        sun: Double,
        moon: Double
    ): Int {

        val difference =
            normalize(moon - sun)

        return floor(
            difference / TITHI_SIZE
        ).toInt() + 1
    }


    private fun getPakshaIndex(
        tithiIndex: Int
    ): Int =
        if (tithiIndex <= 15) 0 else 1


    private fun getPaksha(
        tithiIndex: Int
    ): String =
        if (tithiIndex <= 15) {
            "शुक्ल पक्ष"
        } else {
            "कृष्ण पक्ष"
        }


    private fun getKaranaIndex(
        sun: Double,
        moon: Double
    ): Int {

        val difference =
            normalize(moon - sun)

        return floor(
            difference / KARANA_SIZE
        ).toInt()
    }


    /**
     * Surya Siddhanta Panchanga Yoga:
     * Yoga = normalized(Sun sidereal longitude + Moon sidereal longitude)
     * divided into 27 equal sectors of 13°20′ each.
     *
     * Ghat-Chakra comparison MUST use this same Panchang Yoga value;
     * no separate/alternate Yoga calculation is used for Ghat-Chakra.
     */
    private fun getYogaIndex(
        sun: Double,
        moon: Double
    ): Int {

        val total = normalize(sun + moon)

        return floor(total / YOGA_SIZE).toInt().coerceIn(0, 26)
    }


    // ==========================================
    // NAMES
    // ==========================================

    private fun getTithiName(index: Int): String {

        val names = arrayOf(
            "प्रतिपदा",
            "द्वितीया",
            "तृतीया",
            "चतुर्थी",
            "पंचमी",
            "षष्ठी",
            "सप्तमी",
            "अष्टमी",
            "नवमी",
            "दशमी",
            "एकादशी",
            "द्वादशी",
            "त्रयोदशी",
            "चतुर्दशी",
            "पौर्णिमा"
        )

        return if (index == 30) {
            "अमावस्या"
        } else {
            names[(index - 1).coerceIn(0, 14)]
        }
    }


    private fun getKaranaName(index: Int): String {

        val repeating = arrayOf(
            "बव",
            "बालव",
            "कौलव",
            "तैतिल",
            "गर",
            "वणिज",
            "विष्टि"
        )

        return when (index) {
            0 -> "किंस्तुघ्न"
            57 -> "शकुनि"
            58 -> "चतुष्पाद"
            59 -> "नाग"
            else -> repeating[(index - 1) % 7]
        }
    }


    private fun getYogaName(index: Int): String {

        val names = arrayOf(
            "विष्कंभ",
            "प्रीति",
            "आयुष्मान",
            "सौभाग्य",
            "शोभन",
            "अतिगंड",
            "सुकर्मा",
            "धृति",
            "शूल",
            "गंड",
            "वृद्धि",
            "ध्रुव",
            "व्याघात",
            "हर्षण",
            "वज्र",
            "सिद्धि",
            "व्यतीपात",
            "वरीयान",
            "परिघ",
            "शिव",
            "सिद्ध",
            "साध्य",
            "शुभ",
            "शुक्ल",
            "ब्रह्म",
            "इंद्र",
            "वैधृति"
        )

        return names[index.coerceIn(0, 26)]
    }


    private enum class BoundaryType {
        TITHI,
        YOGA,
        KARANA,
        PAKSHA
    }

    private fun indexAt(
        millis: Long,
        type: BoundaryType,
        swe: SwissEph
    ): Int {

        val (sun, moon) =
            getSunMoon(millis, swe)

        return when (type) {
            BoundaryType.TITHI ->
                getTithiIndex(sun, moon)

            BoundaryType.YOGA ->
                getYogaIndex(sun, moon)

            BoundaryType.KARANA ->
                getKaranaIndex(sun, moon)

            BoundaryType.PAKSHA ->
                getPakshaIndex(
                    getTithiIndex(sun, moon)
                )
        }
    }

    // ==========================================
    // PREVIOUS / NEXT BOUNDARY
    //
    // First find the boundary in 30-minute steps,
    // then refine it to approximately 1 second.
    // ==========================================

    private fun findBoundary(
        now: Long,
        currentIndex: Int,
        forward: Boolean,
        maxMinutes: Int,
        type: BoundaryType
    ): Long {

        /*
         * Performance optimization:
         * - One SwissEph instance is reused for the whole search.
         * - Sun and Moon are calculated together.
         * - Coarse search uses 60 minutes instead of 30.
         * - Final binary search keeps approximately 1-second accuracy.
         *
         * Panchang boundaries are much slower than the 60-minute
         * coarse step, so this does not skip a normal Tithi/Yoga/Karana/
         * Paksha transition.
         */
        val swe = createSwissEph()

        val step = 6L * 60 * 60_000L

        var previous = now
        var current = now

        repeat(maxMinutes / 60 + 2) {

            current =
                if (forward) {
                    current + step
                } else {
                    current - step
                }

            val index =
                indexAt(
                    current,
                    type,
                    swe
                )

            if (index != currentIndex) {

                var low =
                    if (forward) previous else current

                var high =
                    if (forward) current else previous

                repeat(18) {

                    val middle =
                        low + (high - low) / 2

                    val middleIndex =
                        indexAt(
                            middle,
                            type,
                            swe
                        )

                    if (forward) {

                        if (middleIndex == currentIndex) {
                            low = middle
                        } else {
                            high = middle
                        }

                    } else {

                        if (middleIndex == currentIndex) {
                            high = middle
                        } else {
                            low = middle
                        }
                    }
                }

                return if (forward) high else low
            }

            previous = current
        }

        return if (forward) {
            now + 24 * 60 * 60 * 1000L
        } else {
            now - 24 * 60 * 60 * 1000L
        }
    }

    // ==========================================
    // FORMAT TIME
    // ==========================================

    private fun formatTime(
        millis: Long
    ): String {

        val formatter =
            SimpleDateFormat(
                "dd-MM-yyyy HH:mm",
                Locale.getDefault()
            )

        formatter.timeZone = indiaTimeZone

        return formatter.format(millis)
    }


    // ==========================================
    // MASA
    // ==========================================

    private data class MasaInfo(
        val masa: String,
        val startMillis: Long,
        val nextMasa: String,
        val nextStartMillis: Long
    )

    private data class PraharInfo(
        val currentName: String,
        val startMillis: Long,
        val nextName: String,
        val nextMillis: Long
    )

    private data class LagnaInfo(
        val currentName: String,
        val startMillis: Long,
        val nextName: String,
        val nextMillis: Long
    )

private val masaNamesBySunSign = arrayOf(
    "वैशाख",      // मेष
    "ज्येष्ठ",    // वृषभ
    "आषाढ",       // मिथुन
    "श्रावण",     // कर्क
    "भाद्रपद",    // सिंह
    "आश्विन",     // कन्या
    "कार्तिक",    // तुला
    "मार्गशीर्ष", // वृश्चिक
    "पौष",        // धनु
    "माघ",        // मकर
    "फाल्गुन",    // कुंभ
    "चैत्र"       // मीन
)

    private val rashiNames = arrayOf(
        "मेष", "वृषभ", "मिथुन", "कर्क", "सिंह", "कन्या",
        "तुला", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन"
    )

    private fun findTithiStartOf(
        now: Long,
        currentIndex: Int,
        forward: Boolean,
        maxMinutes: Int,
        targetIndex: Int,
        swe: SwissEph
    ): Long {
        val step = 6L * 60 * 60_000L
        var previous = now
        var current = now

        repeat(maxMinutes / 60 + 2) {
            current = if (forward) current + step else current - step
            val index = indexAt(current, BoundaryType.TITHI, swe)

            if (index == targetIndex) {
                var low = if (forward) previous else current
                var high = if (forward) current else previous

                repeat(20) {
                    val middle = low + (high - low) / 2
                    val middleIndex = indexAt(middle, BoundaryType.TITHI, swe)

                    if (forward) {
                        if (middleIndex == targetIndex) high = middle else low = middle
                    } else {
                        if (middleIndex == targetIndex) low = middle else high = middle
                    }
                }

                return if (forward) high else low
            }
            previous = current
        }

        return if (forward) now + 24 * 60 * 60 * 1000L else now - 24 * 60 * 60 * 1000L
    }

    private fun getSunSignAt(
        millis: Long,
        swe: SwissEph
    ): Int {
        val (sun, _) = getSunMoon(millis, swe)
        return floor(sun / 30.0).toInt().coerceIn(0, 11)
    }

    private fun findSunSignBoundary(
        startMillis: Long,
        endMillis: Long,
        swe: SwissEph
    ): Pair<Int, Long>? {

        // A lunar (Amanta) month is named from the solar Sankranti
        // occurring inside that Amavasya -> Amavasya interval.
        // We use the sidereal Lahiri Sun longitude already used by
        // this calculator and locate the exact sign ingress.
        val step = 60L * 60_000L

        var previous = startMillis
        var previousSign = getSunSignAt(previous, swe)

        while (previous < endMillis) {
            val current = minOf(previous + step, endMillis)
            val currentSign = getSunSignAt(current, swe)

            if (currentSign != previousSign) {
                var low = previous
                var high = current

                repeat(25) {
                    val middle = low + (high - low) / 2
                    val middleSign = getSunSignAt(middle, swe)

                    if (middleSign == previousSign) {
                        low = middle
                    } else {
                        high = middle
                    }
                }

                return Pair(currentSign, high)
            }

            previous = current
            previousSign = currentSign
        }

        return null
    }

private fun getMasaNameForInterval(
    startMillis: Long,
    endMillis: Long,
    nextIntervalStart: Long?,
    nextIntervalEnd: Long?,
    swe: SwissEph
): String {

    val sankranti =
        findSunSignBoundary(
            startMillis = startMillis,
            endMillis = endMillis,
            swe = swe
        )

    if (sankranti != null) {

        return masaNamesBySunSign[
            sankranti.first.coerceIn(0, 11)
        ]
    }

    // ==========================================
    // ADHIK MAS
    // ==========================================

    if (
        nextIntervalStart != null &&
        nextIntervalEnd != null
    ) {

        val nextSankranti =
            findSunSignBoundary(
                startMillis = nextIntervalStart,
                endMillis = nextIntervalEnd,
                swe = swe
            )

        if (nextSankranti != null) {

            return masaNamesBySunSign[
                nextSankranti.first.coerceIn(0, 11)
            ]
        }
    }

    // ==========================================
    // SAFE FALLBACK
    // ==========================================

    return masaNamesBySunSign[
        getSunSignAt(
            startMillis,
            swe
        ).coerceIn(0, 11)
    ]
}
 private fun getMasaInfo(
    now: Long,
    currentTithiIndex: Int,
    swe: SwissEph
): MasaInfo {

    // ==========================================
    // AMAVASYA / MASA START
    // ==========================================
    //
    // Amanta month starts exactly when TITHI changes
    // from Amavasya (30) to Pratipada (1).
    //
    // The previous implementation searched for the
    // phase 300° boundary, which is NOT Amavasya.
    // That caused wrong masaStartTime values such as
    // 01-08-2026 00:00.
    //
    // We now locate the actual Tithi-1 boundary.
    // This gives the real Amavasya / Masa start time.

    fun findMasaStart(
        fromMillis: Long,
        forward: Boolean,
        includeCurrent: Boolean
    ): Long {

        // If we are currently inside Pratipada, the
        // current Tithi boundary itself is the masa start.
        if (currentTithiIndex == 1 && includeCurrent) {
            return findBoundary(
                now = fromMillis,
                currentIndex = 1,
                forward = false,
                maxMinutes = 2880,
                type = BoundaryType.TITHI
            )
        }

        return findTithiStartOf(
            now = fromMillis,
            currentIndex = currentTithiIndex,
            forward = forward,
            maxMinutes = 60 * 24 * 40,
            targetIndex = 1,
            swe = swe
        )
    }

    // Current Amavasya -> current Masa start.
    val currentMasaStart =
        if (currentTithiIndex == 1) {
            findBoundary(
                now = now,
                currentIndex = 1,
                forward = false,
                maxMinutes = 2880,
                type = BoundaryType.TITHI
            )
        } else {
            findTithiStartOf(
                now = now,
                currentIndex = currentTithiIndex,
                forward = false,
                maxMinutes = 60 * 24 * 40,
                targetIndex = 1,
                swe = swe
            )
        }

    // Next Amavasya -> next Masa start.
    val nextMasaStart =
        if (currentTithiIndex == 1) {
            findBoundary(
                now = now,
                currentIndex = 1,
                forward = true,
                maxMinutes = 60 * 24 * 40,
                type = BoundaryType.TITHI
            )
        } else {
            findTithiStartOf(
                now = now,
                currentIndex = currentTithiIndex,
                forward = true,
                maxMinutes = 60 * 24 * 40,
                targetIndex = 1,
                swe = swe
            )
        }

    // Following Amavasya -> following Masa start.
    val nextNextMasaStart =
        findTithiStartOf(
            now = nextMasaStart + 60_000L,
            currentIndex = 1,
            forward = true,
            maxMinutes = 60 * 24 * 40,
            targetIndex = 1,
            swe = swe
        )

    // ==========================================
    // MASA NAME
    // ==========================================
    //
    // In the Amanta system the lunar month is named
    // according to the sidereal solar Sankranti that
    // occurs between two consecutive Amavasyas.
    //
    // If a Sankranti occurs, use the corresponding
    // Marathi masa name.
    // If no Sankranti occurs, the interval is Adhik.
    //
    // The existing Sankranti calculation is retained.

    val masa =
        getMasaNameForInterval(
            startMillis = currentMasaStart,
            endMillis = nextMasaStart,
            nextIntervalStart = nextMasaStart,
            nextIntervalEnd = nextNextMasaStart,
            swe = swe
        )

    val nextMasa =
        getMasaNameForInterval(
            startMillis = nextMasaStart,
            endMillis = nextNextMasaStart,
            nextIntervalStart = null,
            nextIntervalEnd = null,
            swe = swe
        )

    return MasaInfo(
        masa = masa,
        startMillis = currentMasaStart,
        nextMasa = nextMasa,
        nextStartMillis = nextMasaStart
    )
}


// ==========================================
    // PRAHAR
    // ==========================================

    private fun solarRiseSet(
        dateMillis: Long,
        latitude: Double,
        longitude: Double
    ): Pair<Long, Long> {
        // NOAA-style solar calculation. Accuracy is sufficient for prahar
        // boundaries and avoids expensive ephemeris searches.
        val cal = Calendar.getInstance(indiaTimeZone)
        cal.timeInMillis = dateMillis
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        val daysInYear = if (Calendar.getInstance().apply {
                set(year, Calendar.DECEMBER, 31)
            }.get(Calendar.DAY_OF_YEAR) == 366) 366 else 365

        fun solarEvent(isSunrise: Boolean): Long {
            val zenith = Math.toRadians(90.833)
            val gamma = 2.0 * Math.PI / daysInYear * (dayOfYear - 1 + 0.5)
            val eqTime = 229.18 * (
                0.000075 +
                    0.001868 * kotlin.math.cos(gamma) -
                    0.032077 * kotlin.math.sin(gamma) -
                    0.014615 * kotlin.math.cos(2 * gamma) -
                    0.040849 * kotlin.math.sin(2 * gamma)
                )
            val decl =
                0.006918 -
                    0.399912 * kotlin.math.cos(gamma) +
                    0.070257 * kotlin.math.sin(gamma) -
                    0.006758 * kotlin.math.cos(2 * gamma) +
                    0.000907 * kotlin.math.sin(2 * gamma) -
                    0.002697 * kotlin.math.cos(3 * gamma) +
                    0.00148 * kotlin.math.sin(3 * gamma)

            val cosH = (
                kotlin.math.cos(zenith) -
                    kotlin.math.sin(Math.toRadians(latitude)) * kotlin.math.sin(decl)
                ) / (
                kotlin.math.cos(Math.toRadians(latitude)) * kotlin.math.cos(decl)
                )

            if (cosH !in -1.0..1.0) return cal.timeInMillis

            val hourAngle = kotlin.math.acos(cosH) * 180.0 / Math.PI
            val solarMinutes = if (isSunrise) {
                720.0 - 4.0 * (longitude + hourAngle) - eqTime
            } else {
                720.0 - 4.0 * (longitude - hourAngle) - eqTime
            }

            val result = Calendar.getInstance(indiaTimeZone)
            result.timeInMillis = cal.timeInMillis
            result.set(Calendar.HOUR_OF_DAY, 0)
            result.set(Calendar.MINUTE, 0)
            result.set(Calendar.SECOND, 0)
            result.set(Calendar.MILLISECOND, 0)
            result.add(Calendar.MINUTE, solarMinutes.toInt())
            result.add(Calendar.MILLISECOND, ((solarMinutes - solarMinutes.toInt()) * 60_000.0).toInt())
            return result.timeInMillis
        }

        return Pair(solarEvent(true), solarEvent(false))
    }

    private fun getPraharInfo(
        now: Long,
        latitude: Double,
        longitude: Double
    ): PraharInfo {

        val day = Calendar.getInstance(indiaTimeZone).apply { timeInMillis = now }
        val startOfDay = Calendar.getInstance(indiaTimeZone).apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val (sunrise, sunset) = solarRiseSet(startOfDay, latitude, longitude)
        val nextDay = Calendar.getInstance(indiaTimeZone).apply {
            timeInMillis = startOfDay
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
        val (nextSunrise, _) = solarRiseSet(nextDay, latitude, longitude)

        val boundaries = ArrayList<Long>(9)
        boundaries.add(sunrise)
        val dayPart = (sunset - sunrise) / 4L
        for (i in 1..3) boundaries.add(sunrise + dayPart * i)
        boundaries.add(sunset)
        val nightPart = (nextSunrise - sunset) / 4L
        for (i in 1..3) boundaries.add(sunset + nightPart * i)
        boundaries.add(nextSunrise)

        val names = arrayOf(
            "दिवसाचा पहिला प्रहर", "दिवसाचा दुसरा प्रहर", "दिवसाचा तिसरा प्रहर", "दिवसाचा चौथा प्रहर",
            "रात्रीचा पहिला प्रहर", "रात्रीचा दुसरा प्रहर", "रात्रीचा तिसरा प्रहर", "रात्रीचा चौथा प्रहर"
        )

        var index = 0
        for (i in 0 until 8) {
            if (now >= boundaries[i] && now < boundaries[i + 1]) {
                index = i
                break
            }
        }

        return PraharInfo(
            currentName = names[index],
            startMillis = boundaries[index],
            nextName = names[(index + 1) % 8],
            nextMillis = boundaries[index + 1]
        )
    }

    // ==========================================
    // LAGNA
    // ==========================================

    private fun getLagnaAt(
        millis: Long,
        latitude: Double,
        longitude: Double,
        swe: SwissEph
    ): Int {
        val jd = 2440587.5 + millis / 86400000.0
        val cusp = DoubleArray(14)
        val ascmc = DoubleArray(10)

        swe.swe_set_sid_mode(
            SweConst.SE_SIDM_LAHIRI,
            0.0,
            0.0
        )

        swe.swe_houses(
            jd,
            SweConst.SEFLG_SIDEREAL,
            latitude,
            longitude,
            'W'.code,
            cusp,
            ascmc
        )

        return floor(normalize(ascmc[0]) / 30.0).toInt().coerceIn(0, 11)
    }

    private fun findLagnaBoundary(
        now: Long,
        currentIndex: Int,
        forward: Boolean,
        latitude: Double,
        longitude: Double,
        swe: SwissEph
    ): Long {
        val step = 15L * 60_000L
        var previous = now
        var current = now

        repeat(24 * 12 + 12) {
            current = if (forward) current + step else current - step
            val index = getLagnaAt(
                current,
                latitude,
                longitude,
                swe
            )
            if (index != currentIndex) {
                var low = if (forward) previous else current
                var high = if (forward) current else previous

                repeat(18) {
                    val middle = low + (high - low) / 2
                    val middleIndex = getLagnaAt(
                        middle,
                        latitude,
                        longitude,
                        swe
                    )
                    if (forward) {
                        if (middleIndex == currentIndex) low = middle else high = middle
                    } else {
                        if (middleIndex == currentIndex) high = middle else low = middle
                    }
                }
                return if (forward) high else low
            }
            previous = current
        }

        return if (forward) now + 2L * 60 * 60 * 1000L else now - 2L * 60 * 60 * 1000L
    }

    private fun getLagnaInfo(
        now: Long,
        latitude: Double,
        longitude: Double,
        swe: SwissEph
    ): LagnaInfo {
        val currentIndex =
            getLagnaAt(
                now,
                latitude,
                longitude,
                swe
            )

        val previous =
            findLagnaBoundary(
                now,
                currentIndex,
                false,
                latitude,
                longitude,
                swe
            )

        val next =
            findLagnaBoundary(
                now,
                currentIndex,
                true,
                latitude,
                longitude,
                swe
            )

        val nextIndex =
            getLagnaAt(
                next + 60_000L,
                latitude,
                longitude,
                swe
            )

        return LagnaInfo(
            currentName = "${rashiNames[currentIndex]} लग्न",
            startMillis = previous,
            nextName = "${rashiNames[nextIndex]} लग्न",
            nextMillis = next
        )
    }

    // ==========================================
    // MAIN PANCHANG STATE
    // ==========================================

    fun getCurrentPanchangState(
        latitude: Double = 18.5204,
        longitude: Double = 73.8567
    ): PanchangState {

        val safeLatitude =
            latitude.coerceIn(-89.0, 89.0)

        val safeLongitude =
            longitude.coerceIn(-180.0, 180.0)

        val now =
            System.currentTimeMillis()

        val swe = createSwissEph()

        val (sun, moon) =
            getSunMoon(
                now,
                swe
            )


        // ==========================================
        // TITHI
        // ==========================================

        val tithiIndex =
            getTithiIndex(sun, moon)

        val previousTithiMillis =
            findBoundary(
                now = now,
                currentIndex = tithiIndex,
                forward = false,
                maxMinutes = 2880,
                type = BoundaryType.TITHI
            )

        val nextTithiMillis =
            findBoundary(
                now = now,
                currentIndex = tithiIndex,
                forward = true,
                maxMinutes = 2880,
                type = BoundaryType.TITHI
            )

        val nextTithiIndex =
            indexAt(
                nextTithiMillis,
                BoundaryType.TITHI,
                swe
            )


        // ==========================================
        // YOGA
        // ==========================================

        val yogaIndex =
            getYogaIndex(sun, moon)

        val previousYogaMillis =
            findBoundary(
                now = now,
                currentIndex = yogaIndex,
                forward = false,
                maxMinutes = 2880,
                type = BoundaryType.YOGA
            )

        val nextYogaMillis =
            findBoundary(
                now = now,
                currentIndex = yogaIndex,
                forward = true,
                maxMinutes = 2880,
                type = BoundaryType.YOGA
            )

        val nextYogaIndex =
            indexAt(
                nextYogaMillis,
                BoundaryType.YOGA,
                swe
            )


        // ==========================================
        // KARANA
        // ==========================================

        val karanaIndex =
            getKaranaIndex(sun, moon)

        val previousKaranaMillis =
            findBoundary(
                now = now,
                currentIndex = karanaIndex,
                forward = false,
                maxMinutes = 1440,
                type = BoundaryType.KARANA
            )

        val nextKaranaMillis =
            findBoundary(
                now = now,
                currentIndex = karanaIndex,
                forward = true,
                maxMinutes = 1440,
                type = BoundaryType.KARANA
            )

        val nextKaranaIndex =
            indexAt(
                nextKaranaMillis,
                BoundaryType.KARANA,
                swe
            )


        // ==========================================
        // PAKSHA
        // ==========================================

        val pakshaIndex =
            getPakshaIndex(tithiIndex)

        val previousPakshaMillis =
            findBoundary(
                now = now,
                currentIndex = pakshaIndex,
                forward = false,
                maxMinutes = 21600,
                type = BoundaryType.PAKSHA
            )

        val nextPakshaMillis =
            findBoundary(
                now = now,
                currentIndex = pakshaIndex,
                forward = true,
                maxMinutes = 21600,
                type = BoundaryType.PAKSHA
            )

        val nextPakshaTithiIndex =
            indexAt(
                nextPakshaMillis,
                BoundaryType.TITHI,
                swe
            )


        // ==========================================
        // DATE / WEEKDAY
        // ==========================================

        val calendar =
            Calendar.getInstance(
                indiaTimeZone
            )

        val dateFormatter =
            SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            )

        dateFormatter.timeZone =
            indiaTimeZone

        val weekdayFormatter =
            SimpleDateFormat(
                "EEEE",
                Locale("mr", "IN")
            )

        weekdayFormatter.timeZone =
            indiaTimeZone

        // ==========================================
        // PRAHAR / LAGNA CALCULATIONS
        // MASA calculation intentionally disabled.
        // The Masa card has been removed from the UI.
        // ==========================================

        val praharInfo =
            getPraharInfo(
                now,
                safeLatitude,
                safeLongitude
            )

        val lagnaInfo =
            getLagnaInfo(
                now,
                safeLatitude,
                safeLongitude,
                swe
            )


        // ==========================================
        // RETURN
        // ==========================================

        return PanchangState(

            // BASIC

            date =
                dateFormatter.format(
                    calendar.time
                ),

            weekday =
                weekdayFormatter.format(
                    calendar.time
                ),


            // TITHI

            tithi =
                getTithiName(
                    tithiIndex
                ),

            tithiStartTime =
                formatTime(
                    previousTithiMillis
                ),

            nextTithi =
                getTithiName(
                    nextTithiIndex
                ),

            nextTithiTime =
                formatTime(
                    nextTithiMillis
                ),

            nextTithiMillis =
                nextTithiMillis,


            // YOGA

            yoga =
                getYogaName(
                    yogaIndex
                ),

            yogaStartTime =
                formatTime(
                    previousYogaMillis
                ),

            nextYoga =
                getYogaName(
                    nextYogaIndex
                ),

            nextYogaTime =
                formatTime(
                    nextYogaMillis
                ),

            nextYogaMillis =
                nextYogaMillis,


            // KARANA

            karana =
                getKaranaName(
                    karanaIndex
                ),

            karanaStartTime =
                formatTime(
                    previousKaranaMillis
                ),

            nextKarana =
                getKaranaName(
                    nextKaranaIndex
                ),

            nextKaranaTime =
                formatTime(
                    nextKaranaMillis
                ),

            nextKaranaMillis =
                nextKaranaMillis,


            // PAKSHA

            paksha =
                getPaksha(
                    tithiIndex
                ),

            pakshaStartTime =
                formatTime(
                    previousPakshaMillis
                ),

            nextPaksha =
                getPaksha(
                    nextPakshaTithiIndex
                ),

            nextPakshaTime =
                formatTime(
                    nextPakshaMillis
                ),

            nextPakshaMillis =
                nextPakshaMillis,


            // MASA intentionally left blank.
            // Calculation is disabled because the Masa card is removed.
            masa = "",
            masaStartTime = "",
            nextMasa = "",
            nextMasaTime = "",
            nextMasaMillis = 0L,


            // PRAHAR

            prahar =
                praharInfo.currentName,

            praharStartTime =
                formatTime(praharInfo.startMillis),

            nextPrahar =
                praharInfo.nextName,

            nextPraharTime =
                formatTime(praharInfo.nextMillis),

            nextPraharMillis =
                praharInfo.nextMillis,


            // LAGNA

            lagna =
                lagnaInfo.currentName,

            lagnaStartTime =
                formatTime(lagnaInfo.startMillis),

            nextLagna =
                lagnaInfo.nextName,

            nextLagnaTime =
                formatTime(lagnaInfo.nextMillis),

            nextLagnaMillis =
                lagnaInfo.nextMillis
        )
    }
}
