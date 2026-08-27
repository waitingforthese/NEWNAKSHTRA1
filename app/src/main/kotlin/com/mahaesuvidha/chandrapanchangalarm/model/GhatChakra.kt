package com.mahaesuvidha.chandrapanchangalarm.model

/** Personal Ghat Chakra selected from the native's birth Moon sign. */
data class GhatChakra(
    val birthRashi: String,
    val masa: String,
    val tithiGroup: String,
    val weekday: String,
    val nakshatra: String,
    val yoga: String,
    val karana: String,
    val prahar: Int,
    val moonRashi: String,
    val maleLagna: String,
    val femaleLagna: String,
    /** Ghat tithi-group applies in both Shukla and Krishna paksha. */
    val paksha: String = "शुक्ल + कृष्ण"
) {
    fun lagnaFor(gender: String): String =
        if (gender.equals("Female", ignoreCase = true) || gender == "स्त्री") femaleLagna else maleLagna

    /**
     * The current Panchang Yoga is already calculated by the Live Panchang
     * engine using the Surya Siddhanta Sun+Moon Yoga rule. Ghat-Chakra uses
     * that exact value for comparison; it does not calculate a second Yoga.
     */
    fun matchesSuryaSiddhantaYoga(currentPanchangYoga: String): Boolean =
        currentPanchangYoga.trim() == yoga.trim()

    fun matchesTithi(tithiNumber: Int): Boolean {
        val group = when ((tithiNumber - 1) % 5) {
            0 -> "नंदा"
            1 -> "भद्रा"
            2 -> "जया"
            3 -> "रिक्ता"
            else -> "पूर्णा"
        }
        return group == tithiGroup
    }
}

object GhatChakraCalculator {
    private data class Row(
        val rashi: String, val masa: String, val tithiGroup: String, val weekday: String,
        val nakshatra: String, val yoga: String, val karana: String, val prahar: Int,
        val moonRashi: String, val maleLagna: String, val femaleLagna: String
    )

    private val rows = listOf(
        Row("मेष", "कार्तिक", "नंदा", "रविवार", "मघा", "विष्कंभ", "बव", 1, "मेष", "मेष", "तुळ"),
        Row("वृषभ", "मार्गशीर्ष", "पूर्णा", "शनिवार", "हस्त", "शुक्ल", "शकुनि", 4, "कन्या", "वृषभ", "वृश्चिक"),
        Row("मिथुन", "आषाढ", "भद्रा", "सोमवार", "स्वाती", "परिघ", "चतुष्पद", 3, "कुंभ", "कर्क", "मकर"),
        Row("कर्क", "पौष", "भद्रा", "बुधवार", "अनुराधा", "व्याघात", "नाग", 1, "सिंह", "तुळ", "मेष"),
        Row("सिंह", "ज्येष्ठ", "जया", "शनिवार", "मूळ", "धृति", "बव", 1, "मकर", "मकर", "कर्क"),
        Row("कन्या", "भाद्रपद", "पूर्णा", "शनिवार", "श्रवण", "शुक्ल", "कौलव", 1, "मिथुन", "मीन", "कन्या"),
        Row("तुळ", "माघ", "रिक्ता", "गुरुवार", "शतभिषा", "शुक्ल", "तैतिल", 4, "धनु", "कन्या", "मीन"),
        Row("वृश्चिक", "आश्विन", "नंदा", "शुक्रवार", "रेवती", "व्यतीपात", "गर", 1, "वृषभ", "वृश्चिक", "वृषभ"),
        Row("धनु", "श्रावण", "जया", "शुक्रवार", "भरणी", "वज्र", "तैतिल", 1, "मीन", "धनु", "मिथुन"),
        Row("मकर", "वैशाख", "रिक्ता", "मंगळवार", "रोहिणी", "वैधृति", "शकुनि", 4, "सिंह", "कुंभ", "सिंह"),
        Row("कुंभ", "चैत्र", "जया", "गुरुवार", "आर्द्रा", "गंड", "वणिज", 3, "धनु", "मिथुन", "धनु"),
        Row("मीन", "फाल्गुन", "पूर्णा", "शुक्रवार", "आश्लेषा", "वज्र", "विष्टि", 6, "कुंभ", "सिंह", "कुंभ")
    )

    fun fromBirthMoonRashi(rashi: String): GhatChakra {
        val row = rows.firstOrNull { it.rashi == rashi } ?: rows.first()
        return GhatChakra(row.rashi, row.masa, row.tithiGroup, row.weekday, row.nakshatra,
            row.yoga, row.karana, row.prahar, row.moonRashi, row.maleLagna, row.femaleLagna)
    }

    fun calculateBirthMoonRashi(birthDate: String, birthTime: String): String {
        val parts = birthDate.trim().split("/")
        require(parts.size == 3) { "जन्मतारीख DD/MM/YYYY मध्ये भरा" }
        val day = parts[0].toInt()
        val month = parts[1].toInt()
        val year = parts[2].toInt()

        val timeParts = birthTime.trim().uppercase().split(" ")
        val hm = timeParts[0].split(":")
        require(hm.size == 2) { "जन्मवेळ 00:00 या पद्धतीने भरा" }
        var hour = hm[0].toInt()
        val minute = hm[1].toInt()
        if (timeParts.size > 1) {
            // Backward compatibility for previously saved/manual AM/PM values.
            require(hour in 1..12 && minute in 0..59) { "जन्मवेळ चुकीची आहे" }
            val ampm = timeParts[1]
            if (ampm == "PM" && hour != 12) hour += 12
            if (ampm == "AM" && hour == 12) hour = 0
        } else {
            require(hour in 0..23 && minute in 0..59) { "जन्मवेळ 00:00 ते 23:59 मध्ये भरा" }
        }

        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
        cal.isLenient = false
        cal.set(year, month - 1, day, hour, minute, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val millis = cal.timeInMillis
        val jd = 2440587.5 + millis / 86400000.0
        val swe = swisseph.SwissEph()
        swe.swe_set_sid_mode(swisseph.SweConst.SE_SIDM_LAHIRI, 0.0, 0.0)
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        swe.swe_calc_ut(jd, swisseph.SweConst.SE_MOON,
            swisseph.SweConst.SEFLG_SWIEPH or swisseph.SweConst.SEFLG_SIDEREAL, xx, serr)
        val index = (xx[0].mod(360.0) / 30.0).toInt().coerceIn(0, 11)
        return arrayOf("मेष", "वृषभ", "मिथुन", "कर्क", "सिंह", "कन्या", "तुळ", "वृश्चिक", "धनु", "मकर", "कुंभ", "मीन")[index]
    }
}
