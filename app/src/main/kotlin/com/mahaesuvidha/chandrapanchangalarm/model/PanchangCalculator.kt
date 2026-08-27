package com.mahaesuvidha.chandrapanchangalarm.model

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.tan


object PanchangCalculator {

    private val INDIA_ZONE =
        ZoneId.of("Asia/Kolkata")

    private const val TITHI_SIZE = 12.0
    private const val YOGA_SIZE = 360.0 / 27.0
    private const val KARANA_SIZE = 6.0

    private val tithiNames = listOf(
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
        "पूर्णिमा"
    )

    private val yogaNames = listOf(
        "विष्कंभ",
        "प्रीती",
        "आयुष्मान",
        "सौभाग्य",
        "शोभन",
        "अतिगंड",
        "सुकर्म",
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

    private val weekdayNames = listOf(
        "रविवार",
        "सोमवार",
        "मंगळवार",
        "बुधवार",
        "गुरुवार",
        "शुक्रवार",
        "शनिवार"
    )

    private val masaNames = listOf(
        "चैत्र",
        "वैशाख",
        "ज्येष्ठ",
        "आषाढ",
        "श्रावण",
        "भाद्रपद",
        "आश्विन",
        "कार्तिक",
        "मार्गशीर्ष",
        "पौष",
        "माघ",
        "फाल्गुन"
    )

    private val lagnaNames = listOf(
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

    fun getCurrentPanchang(): PanchangState {

        val now =
            LocalDateTime.now(INDIA_ZONE)

        val sun =
            getSunLongitude(now)

        val moon =
            getMoonLongitude(now)

        val elongation =
            normalize(moon - sun)


        // =====================================================
        // TITHI
        // =====================================================

        val tithiIndex =
            floor(elongation / TITHI_SIZE)
                .toInt()
                .coerceIn(0, 29)

        val tithiName =
            getTithiName(tithiIndex)

        val tithiStart =
            findPreviousTithiChange(
                now,
                tithiIndex
            )

        val nextTithi =
            findNextTithiChange(
                now,
                tithiIndex
            )


        // =====================================================
        // PAKSHA
        // =====================================================

        val paksha =
            if (tithiIndex < 15) {
                "शुक्ल पक्ष"
            } else {
                "कृष्ण पक्ष"
            }

        val pakshaStart =
            findPreviousPakshaChange(
                now,
                paksha
            )

        val nextPaksha =
            findNextPakshaChange(
                now,
                paksha
            )


        // =====================================================
        // YOGA
        // =====================================================

        val yogaIndex =
            getYogaIndex(now)

        val yogaName =
            yogaNames[yogaIndex]

        val yogaStart =
            findPreviousYogaChange(
                now,
                yogaIndex
            )

        val nextYoga =
            findNextYogaChange(
                now,
                yogaIndex
            )


        // =====================================================
        // KARANA
        // =====================================================

        val karanaIndex =
            floor(elongation / KARANA_SIZE)
                .toInt()
                .coerceIn(0, 59)

        val karanaName =
            getKaranaName(karanaIndex)

        val karanaStart =
            findPreviousKaranaChange(
                now,
                karanaIndex
            )

        val nextKarana =
            findNextKaranaChange(
                now,
                karanaIndex
            )


        // =====================================================
        // MAS
        // =====================================================

        val masaData =
            getMasaData(now)


        // =====================================================
        // PRAHAR
        // =====================================================

        val praharData =
            getPraharData(now)


        // =====================================================
        // LAGNA
        // =====================================================

        val lagnaData =
            getLagnaData(now)


        // =====================================================
        // RETURN
        // =====================================================

        return PanchangState(

            date =
                formatDate(now),

            weekday =
                weekdayNames[
                    now.dayOfWeek.value % 7
                ],


            // TITHI

            tithi =
                tithiName,

            tithiStartTime =
                formatDateTime(tithiStart),

            nextTithi =
                nextTithi.first,

            nextTithiTime =
                formatDateTime(nextTithi.second),

            nextTithiMillis =
                toMillis(nextTithi.second),


            // YOGA

            yoga =
                yogaName,

            yogaStartTime =
                formatDateTime(yogaStart),

            nextYoga =
                nextYoga.first,

            nextYogaTime =
                formatDateTime(nextYoga.second),

            nextYogaMillis =
                toMillis(nextYoga.second),


            // KARANA

            karana =
                karanaName,

            karanaStartTime =
                formatDateTime(karanaStart),

            nextKarana =
                nextKarana.first,

            nextKaranaTime =
                formatDateTime(nextKarana.second),

            nextKaranaMillis =
                toMillis(nextKarana.second),


            // PAKSHA

            paksha =
                paksha,

            pakshaStartTime =
                formatDateTime(pakshaStart),

            nextPaksha =
                nextPaksha.first,

            nextPakshaTime =
                formatDateTime(nextPaksha.second),

            nextPakshaMillis =
                toMillis(nextPaksha.second),


            // MAS

            masa =
                masaData.first,

            nextMasa =
                masaData.second,

            masaStartTime =
                formatDateTime(masaData.third),

            nextMasaTime =
                formatDateTime(masaData.fourth),

            nextMasaMillis =
                toMillis(masaData.fourth),


            // PRAHAR

            prahar =
                praharData.first,

            nextPrahar =
                praharData.second,

            nextPraharTime =
                formatDateTime(praharData.third),

            nextPraharMillis =
                toMillis(praharData.third),


            // LAGNA

            lagna =
                lagnaData.first,

            nextLagna =
                lagnaData.second,

            nextLagnaTime =
                formatDateTime(lagnaData.third),

            nextLagnaMillis =
                toMillis(lagnaData.third)
        )
    }



    // =========================================================
    // FAST TRANSITION SEARCH
    // =========================================================

    private fun findPreviousBoundary(
        now: LocalDateTime,
        stepMinutes: Long,
        maxSteps: Int,
        current: Int,
        classifier: (LocalDateTime) -> Int
    ): LocalDateTime {

        var right = now
        var left = now

        repeat(maxSteps) {
            left = left.minusMinutes(stepMinutes)

            if (classifier(left) != current) {
                var low = left
                var high = right

                repeat(16) {
                    if (java.time.Duration.between(low, high).toMinutes() <= 1) {
                        return high
                    }

                    val middle = low.plusMinutes(
                        java.time.Duration.between(low, high).toMinutes() / 2
                    )

                    if (classifier(middle) == current) {
                        high = middle
                    } else {
                        low = middle
                    }
                }

                return high
            }

            right = left
        }

        return now
    }

    private fun findNextBoundary(
        now: LocalDateTime,
        stepMinutes: Long,
        maxSteps: Int,
        current: Int,
        classifier: (LocalDateTime) -> Int
    ): LocalDateTime {

        var left = now
        var right = now

        repeat(maxSteps) {
            right = right.plusMinutes(stepMinutes)

            if (classifier(right) != current) {
                var low = left
                var high = right

                repeat(16) {
                    if (java.time.Duration.between(low, high).toMinutes() <= 1) {
                        return high
                    }

                    val middle = low.plusMinutes(
                        java.time.Duration.between(low, high).toMinutes() / 2
                    )

                    if (classifier(middle) == current) {
                        low = middle
                    } else {
                        high = middle
                    }
                }

                return high
            }

            left = right
        }

        return now
    }

    private fun findPreviousBoundaryString(
        now: LocalDateTime,
        stepMinutes: Long,
        maxSteps: Int,
        current: String,
        classifier: (LocalDateTime) -> String
    ): LocalDateTime {

        var right = now
        var left = now

        repeat(maxSteps) {
            left = left.minusMinutes(stepMinutes)

            if (classifier(left) != current) {
                var low = left
                var high = right

                repeat(16) {
                    if (java.time.Duration.between(low, high).toMinutes() <= 1) {
                        return high
                    }

                    val middle = low.plusMinutes(
                        java.time.Duration.between(low, high).toMinutes() / 2
                    )

                    if (classifier(middle) == current) {
                        high = middle
                    } else {
                        low = middle
                    }
                }

                return high
            }

            right = left
        }

        return now
    }

    private fun findNextBoundaryString(
        now: LocalDateTime,
        stepMinutes: Long,
        maxSteps: Int,
        current: String,
        classifier: (LocalDateTime) -> String
    ): LocalDateTime {

        var left = now
        var right = now

        repeat(maxSteps) {
            right = right.plusMinutes(stepMinutes)

            if (classifier(right) != current) {
                var low = left
                var high = right

                repeat(16) {
                    if (java.time.Duration.between(low, high).toMinutes() <= 1) {
                        return high
                    }

                    val middle = low.plusMinutes(
                        java.time.Duration.between(low, high).toMinutes() / 2
                    )

                    if (classifier(middle) == current) {
                        low = middle
                    } else {
                        high = middle
                    }
                }

                return high
            }

            left = right
        }

        return now
    }

    // =========================================================
    // TITHI
    // =========================================================

    private fun getTithiIndex(
        time: LocalDateTime
    ): Int {

        val sun =
            getSunLongitude(time)

        val moon =
            getMoonLongitude(time)

        return floor(
            normalize(moon - sun) /
                    TITHI_SIZE
        )
            .toInt()
            .coerceIn(0, 29)
    }

    private fun getTithiName(
        index: Int
    ): String {

        val base =
            tithiNames[index % 15]

        return if (index < 15) {
            base
        } else {
            when (index % 15) {
                14 -> "अमावस्या"
                else -> base
            }
        }
    }

    private fun findPreviousTithiChange(
        now: LocalDateTime,
        current: Int
    ): LocalDateTime =
        findPreviousBoundary(now, 60L, 24 * 4, current, ::getTithiIndex)

    private fun findNextTithiChange(
        now: LocalDateTime,
        current: Int
    ): Pair<String, LocalDateTime> {
        val time = findNextBoundary(now, 60L, 24 * 4, current, ::getTithiIndex)
        val next = getTithiIndex(time)
        return Pair("${getTithiName(current)} → ${getTithiName(next)}", time)
    }


    // =========================================================
    // YOGA
    // =========================================================

    private fun getYogaIndex(
        time: LocalDateTime
    ): Int {

        val sun =
            getSunLongitude(time)

        val moon =
            getMoonLongitude(time)

        return floor(
            normalize(sun + moon) /
                    YOGA_SIZE
        )
            .toInt()
            .coerceIn(0, 26)
    }

    private fun findPreviousYogaChange(
        now: LocalDateTime,
        current: Int
    ): LocalDateTime =
        findPreviousBoundary(now, 60L, 24 * 4, current, ::getYogaIndex)

    private fun findNextYogaChange(
        now: LocalDateTime,
        current: Int
    ): Pair<String, LocalDateTime> {
        val time = findNextBoundary(now, 60L, 24 * 4, current, ::getYogaIndex)
        val next = getYogaIndex(time)
        return Pair("${yogaNames[current]} → ${yogaNames[next]}", time)
    }


    // =========================================================
    // KARANA
    // =========================================================

    private fun getKaranaIndex(
        time: LocalDateTime
    ): Int {

        val sun =
            getSunLongitude(time)

        val moon =
            getMoonLongitude(time)

        return floor(
            normalize(moon - sun) /
                    KARANA_SIZE
        )
            .toInt()
            .coerceIn(0, 59)
    }

    private fun getKaranaName(
        index: Int
    ): String {

        return when (index) {

            0 -> "किंस्तुघ्न"

            1 -> "बव"

            58 -> "चतुष्पाद"

            59 -> "नाग"

            else -> {

                val names =
                    listOf(
                        "बालव",
                        "कौलव",
                        "तैतिल",
                        "गर",
                        "वणिज",
                        "विष्टि",
                        "बव"
                    )

                names[(index - 2) % names.size]
            }
        }
    }

    private fun findPreviousKaranaChange(
        now: LocalDateTime,
        current: Int
    ): LocalDateTime =
        findPreviousBoundary(now, 60L, 24 * 3, current, ::getKaranaIndex)

    private fun findNextKaranaChange(
        now: LocalDateTime,
        current: Int
    ): Pair<String, LocalDateTime> {
        val time = findNextBoundary(now, 60L, 24 * 3, current, ::getKaranaIndex)
        val next = getKaranaIndex(time)
        return Pair("${getKaranaName(current)} → ${getKaranaName(next)}", time)
    }


    // =========================================================
    // PAKSHA
    // =========================================================

    private fun getPaksha(
        time: LocalDateTime
    ): String {

        return if (
            getTithiIndex(time) < 15
        ) {
            "शुक्ल पक्ष"
        } else {
            "कृष्ण पक्ष"
        }
    }

    private fun findPreviousPakshaChange(
        now: LocalDateTime,
        current: String
    ): LocalDateTime =
        findPreviousBoundaryString(now, 360L, 16 * 4, current, ::getPaksha)

    private fun findNextPakshaChange(
        now: LocalDateTime,
        current: String
    ): Pair<String, LocalDateTime> {
        val time = findNextBoundaryString(now, 360L, 16 * 4, current, ::getPaksha)
        val next = getPaksha(time)
        return Pair("$current → $next", time)
    }


    // =========================================================
    // MAS
    // =========================================================

    private fun getMasaData(
        now: LocalDateTime
    ): MasaData {

        /*
         * Keep the project's existing 12-month mapping, but make sure
         * the current and next month fields are always populated.
         *
         * NOTE: This remains the project's simplified month model.
         * Exact Amanta/Purnimanta month calculation requires complete
         * Amavasya/Purnima + Sankranti logic.
         */
        val currentIndex = getMasaIndex(now)
        val currentName = masaNames[currentIndex]

        val start = LocalDateTime.of(
            now.year,
            now.month,
            1,
            0,
            0
        )

        val nextStart = start.plusMonths(1)
        val nextIndex = (currentIndex + 1) % 12

        return MasaData(
            currentName,
            masaNames[nextIndex],
            start,
            nextStart
        )
    }


    private fun getMasaIndex(
        time: LocalDateTime
    ): Int {

        return (time.monthValue + 9) % 12
    }

    // =========================================================
    // PRAHAR
    // =========================================================

   private fun getPraharData(
    now: LocalDateTime
): Triple<String, String, LocalDateTime> {

    // दिवसाचे 4 प्रहर:
    // सूर्योदय → सूर्यास्त
    // रात्रीचे 4 प्रहर:
    // सूर्यास्त → पुढील सूर्योदय

    val sunrise = getSunrise(now)
    val sunset = getSunset(now)

    val nextSunrise =
        getSunrise(now.plusDays(1))

    val isDay =
        !now.isBefore(sunrise) &&
        now.isBefore(sunset)

    val praharNames = listOf(
        "पहिला प्रहर",
        "दुसरा प्रहर",
        "तिसरा प्रहर",
        "चौथा प्रहर",
        "पाचवा प्रहर",
        "सहावा प्रहर",
        "सातवा प्रहर",
        "आठवा प्रहर"
    )

    if (isDay) {

        val totalMinutes =
            java.time.Duration
                .between(
                    sunrise,
                    sunset
                )
                .toMinutes()

        val praharMinutes =
            totalMinutes / 4.0

        val elapsed =
            java.time.Duration
                .between(
                    sunrise,
                    now
                )
                .toMinutes()

        val currentIndex =
            (elapsed / praharMinutes)
                .toInt()
                .coerceIn(0, 3)

        val nextTime =
            sunrise.plusMinutes(
                (
                    (currentIndex + 1) *
                            praharMinutes
                ).toLong()
            )

        return Triple(
            praharNames[currentIndex],
            praharNames[currentIndex + 1],
            nextTime
        )

    } else {

        val nightStart =
            sunset

        val nightEnd =
            nextSunrise

        val totalMinutes =
            java.time.Duration
                .between(
                    nightStart,
                    nightEnd
                )
                .toMinutes()

        val praharMinutes =
            totalMinutes / 4.0

        val elapsed =
            java.time.Duration
                .between(
                    nightStart,
                    now
                )
                .toMinutes()

        val currentIndex =
            (elapsed / praharMinutes)
                .toInt()
                .coerceIn(0, 3)

        val actualIndex =
            currentIndex + 4

        val nextTime =
            nightStart.plusMinutes(
                (
                    (currentIndex + 1) *
                            praharMinutes
                ).toLong()
            )

        return Triple(
            praharNames[actualIndex],
            praharNames[
                (actualIndex + 1)
                    .coerceAtMost(7)
            ],
            nextTime
        )
    }
}
private fun getSunrise(
    date: LocalDateTime
): LocalDateTime {

    // Daund, Maharashtra
    val latitude = 18.46
    val longitude = 74.58

    return calculateSunTime(
        date,
        latitude,
        longitude,
        true
    )
}


private fun getSunset(
    date: LocalDateTime
): LocalDateTime {

    // Daund, Maharashtra
    val latitude = 18.46
    val longitude = 74.58

    return calculateSunTime(
        date,
        latitude,
        longitude,
        false
    )
}
private fun calculateSunTime(
    date: LocalDateTime,
    latitude: Double,
    longitude: Double,
    sunrise: Boolean
): LocalDateTime {

    val dayOfYear =
        date.dayOfYear

    val declination =
        Math.toRadians(
            23.44 *
                    sin(
                        Math.toRadians(
                            (360.0 / 365.0) *
                                    (dayOfYear - 81)
                        )
                    )
        )

    val latitudeRad =
        Math.toRadians(latitude)

    val zenith =
        Math.toRadians(90.833)

    val cosHourAngle =
        (
            cos(zenith) -
                    sin(latitudeRad) *
                    sin(declination)
            ) /
            (
                cos(latitudeRad) *
                        cos(declination)
            )

    val hourAngle =
        Math.toDegrees(
            acos(
                cosHourAngle
                    .coerceIn(-1.0, 1.0)
            )
        )

    val solarNoon =
        12.0 -
                longitude / 15.0

    val solarTime =
        if (sunrise) {
            solarNoon -
                    hourAngle / 15.0
        } else {
            solarNoon +
                    hourAngle / 15.0
        }

    val hour =
        solarTime
            .toInt()
            .coerceIn(0, 23)

    val minute =
        (
            (solarTime - hour) *
                    60.0
        )
            .toInt()
            .coerceIn(0, 59)

    return date
        .withHour(hour)
        .withMinute(minute)
        .withSecond(0)
        .withNano(0)
}
    // =========================================================
    // LAGNA
    // =========================================================

   // =========================================================
// LAGNA — SIDEREAL ASCENDANT
// =========================================================

private fun getLagnaData(
    now: LocalDateTime
): Triple<String, String, LocalDateTime> {

    val latitude = 18.46
    val longitude = 74.58

    val currentLongitude =
        getSiderealAscendant(
            now,
            latitude,
            longitude
        )

    val currentIndex =
        floor(
            currentLongitude / 30.0
        )
            .toInt()
            .coerceIn(0, 11)

    val currentLagna =
        lagnaNames[currentIndex]

    // पुढील लग्न शोधणे
    var check =
        now.plusMinutes(1)

    var nextLongitude =
        getSiderealAscendant(
            check,
            latitude,
            longitude
        )

    var nextIndex =
        floor(
            nextLongitude / 30.0
        )
            .toInt()
            .coerceIn(0, 11)

    var safety = 0

    while (
        nextIndex == currentIndex &&
        safety < 240
    ) {

        check =
            check.plusMinutes(1)

        nextLongitude =
            getSiderealAscendant(
                check,
                latitude,
                longitude
            )

        nextIndex =
            floor(
                nextLongitude / 30.0
            )
                .toInt()
                .coerceIn(0, 11)

        safety++
    }

    return Triple(
        currentLagna,
        lagnaNames[nextIndex],
        check
    )
}
private fun getSiderealAscendant(
    time: LocalDateTime,
    latitude: Double,
    longitude: Double
): Double {

    val jd =
        getJulianDay(time)

    val t =
        (jd - 2451545.0) /
                36525.0

    // Greenwich Mean Sidereal Time
    val gmst =
        normalize(
            280.46061837 +
                    360.98564736629 *
                    (jd - 2451545.0) +
                    0.000387933 *
                    t * t -
                    t * t * t /
                    38710000.0
        )

    // Local Sidereal Time
    val lst =
        normalize(
            gmst + longitude
        )

    val theta =
        Math.toRadians(lst)

    val phi =
        Math.toRadians(latitude)

    // Mean obliquity of the ecliptic
    val epsilon =
        Math.toRadians(
            23.439291
        )

    /*
     * Tropical Ascendant
     */
    val tropicalAscendant =
        Math.toDegrees(
            atan2(
                -cos(theta),
                sin(theta) *
                        cos(epsilon) +
                        tan(phi) *
                        sin(epsilon)
            )
        )

    val tropical =
        normalize(
            tropicalAscendant
        )

    /*
     * Lahiri Ayanamsa
     */
    val ayanamsa =
        getLahiriAyanamsa(
            time
        )

    /*
     * Sidereal Ascendant
     */
    return normalize(
        tropical -
                ayanamsa
    )
}
private fun getLahiriAyanamsa(
    time: LocalDateTime
): Double {

    val years =
        time.year - 2000.0

    // Approximate Lahiri ayanamsa
    // around J2000 epoch

    return 23.85675 +
            (
                50.29 / 3600.0
            ) *
            years
}


    // =========================================================
    // SUN LONGITUDE
    // =========================================================

    private fun getSunLongitude(
        time: LocalDateTime
    ): Double {

        val days =
            getJulianDay(time) -
                    2451545.0

        val meanLongitude =
            normalize(
                280.46646 +
                        0.98564736 * days
            )

        val meanAnomaly =
            Math.toRadians(
                normalize(
                    357.52911 +
                            0.98560028 * days
                )
            )

        return normalize(
            meanLongitude +
                    1.915 * sin(meanAnomaly) +
                    0.020 *
                    sin(2 * meanAnomaly)
        )
    }


    // =========================================================
    // MOON LONGITUDE
    // =========================================================

    private fun getMoonLongitude(
        time: LocalDateTime
    ): Double {

        val days =
            getJulianDay(time) -
                    2451545.0

        val l0 =
            normalize(
                218.316 +
                        13.176396 * days
            )

        val mMoon =
            Math.toRadians(
                normalize(
                    134.963 +
                            13.064993 * days
                )
            )

        val mSun =
            Math.toRadians(
                normalize(
                    357.529 +
                            0.98560028 * days
                )
            )

        val d =
            Math.toRadians(
                normalize(
                    297.850 +
                            12.190749 * days
                )
            )

        return normalize(
            l0 +
                    6.289 *
                    sin(mMoon) +
                    1.274 *
                    sin(2 * d - mMoon) +
                    0.658 *
                    sin(2 * d) +
                    0.214 *
                    sin(2 * mMoon) -
                    0.186 *
                    sin(mSun)
        )
    }


    // =========================================================
    // JULIAN DAY
    // =========================================================

    private fun getJulianDay(
        time: LocalDateTime
    ): Double {

        val instant =
            time
                .atZone(INDIA_ZONE)
                .toInstant()

        return 2440587.5 +
                instant
                    .toEpochMilli()
                    .toDouble() /
                86400000.0
    }


    // =========================================================
    // NORMALIZE
    // =========================================================

    private fun normalize(
        value: Double
    ): Double {

        var result =
            value % 360.0

        if (result < 0) {
            result += 360.0
        }

        return result
    }


    // =========================================================
    // FORMAT
    // =========================================================

    private fun formatDate(
        time: LocalDateTime
    ): String {

        return time.format(
            DateTimeFormatter.ofPattern(
                "dd-MM-yyyy"
            )
        )
    }

    private fun formatDateTime(
        time: LocalDateTime
    ): String {

        return time.format(
            DateTimeFormatter.ofPattern(
                "dd-MM-yyyy HH:mm"
            )
        )
    }


    // =========================================================
    // MILLIS
    // =========================================================

    private fun toMillis(
        time: LocalDateTime
    ): Long {

        return time
            .atZone(INDIA_ZONE)
            .toInstant()
            .toEpochMilli()
    }


    // =========================================================
    // HELPER DATA
    // =========================================================

    private data class MasaData(

        val first: String,

        val second: String,

        val third: LocalDateTime,

        val fourth: LocalDateTime
    )
}
