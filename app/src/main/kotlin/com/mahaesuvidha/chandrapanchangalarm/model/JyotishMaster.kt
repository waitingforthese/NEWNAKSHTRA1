package com.mahaesuvidha.chandrapanchangalarm.model

data class JyotishInfo(
    val rashiLord: String,
    val nakshatraLord: String,
    val navamshaRashi: String,
    val navamshaLord: String,
    val enemies: List<String>
)

object JyotishMaster {

    private val rashiLords = mapOf(
        Rashi.MESHA to "मंगळ",
        Rashi.VRISHABHA to "शुक्र",
        Rashi.MITHUNA to "बुध",
        Rashi.KARKA to "चंद्र",
        Rashi.SIMHA to "सूर्य",
        Rashi.KANYA to "बुध",
        Rashi.TULA to "शुक्र",
        Rashi.VRISHCHIKA to "मंगळ",
        Rashi.DHANU to "गुरु",
        Rashi.MAKARA to "शनि",
        Rashi.KUMBHA to "शनि",
        Rashi.MEENA to "गुरु"
    )

    private val nakshatraLords = mapOf(
        Nakshatra.ASHWINI to "केतू",
        Nakshatra.BHARANI to "शुक्र",
        Nakshatra.KRITTIKA to "सूर्य",
        Nakshatra.ROHINI to "चंद्र",
        Nakshatra.MRIGASHIRSHA to "मंगळ",
        Nakshatra.ARDRA to "राहू",
        Nakshatra.PUNARVASU to "गुरु",
        Nakshatra.PUSHYA to "शनि",
        Nakshatra.ASHLESHA to "बुध",
        Nakshatra.MAGHA to "केतू",
        Nakshatra.PURVA_PHALGUNI to "शुक्र",
        Nakshatra.UTTARA_PHALGUNI to "सूर्य",
        Nakshatra.HASTA to "चंद्र",
        Nakshatra.CHITRA to "मंगळ",
        Nakshatra.SWATI to "राहू",
        Nakshatra.VISHAKHA to "गुरु",
        Nakshatra.ANURADHA to "शनि",
        Nakshatra.JYESHTHA to "बुध",
        Nakshatra.MOOLA to "केतू",
        Nakshatra.PURVA_ASHADHA to "शुक्र",
        Nakshatra.UTTARA_ASHADHA to "सूर्य",
        Nakshatra.SHRAVANA to "चंद्र",
        Nakshatra.DHANISHTHA to "मंगळ",
        Nakshatra.SHATABHISHA to "राहू",
        Nakshatra.PURVA_BHADRAPADA to "गुरु",
        Nakshatra.UTTARA_BHADRAPADA to "शनि",
        Nakshatra.REVATI to "बुध"
    )

    private val rashis = Rashi.entries

    fun getInfo(
        rashi: Rashi,
        nakshatra: Nakshatra,
        pada: Int
    ): JyotishInfo {

        val rashiLord =
            rashiLords[rashi] ?: "—"

        val nakshatraLord =
            nakshatraLords[nakshatra] ?: "—"

        val nakshatraIndex =
            Nakshatra.entries.indexOf(nakshatra)

        val safePada =
            pada.coerceIn(1, 4)

        val navamshaIndex =
            (nakshatraIndex * 4 + safePada - 1) % 12

        val navamshaRashi =
            rashis[navamshaIndex]

        val navamshaLord =
            rashiLords[navamshaRashi] ?: "—"

        val planets = listOf(
            rashiLord,
            nakshatraLord,
            navamshaLord
        ).distinct()

        val enemies = mutableListOf<String>()

        val enemyPairs = mapOf(
            "सूर्य" to setOf("शुक्र", "शनि"),
            "चंद्र" to emptySet(),
            "मंगळ" to setOf("बुध"),
            "बुध" to setOf("चंद्र"),
            "गुरु" to setOf("बुध", "शुक्र"),
            "शुक्र" to setOf("सूर्य", "चंद्र"),
            "शनि" to setOf("सूर्य", "चंद्र"),
            "राहू" to setOf("सूर्य", "चंद्र", "मंगळ"),
            "केतू" to setOf("सूर्य", "चंद्र", "मंगळ")
        )

        planets.forEach { planet ->

            val hasEnemy =
                planets.any { other ->

                    other != planet &&
                        (
                            enemyPairs[planet]
                                ?.contains(other) == true
                            ||
                            enemyPairs[other]
                                ?.contains(planet) == true
                        )
                }

            if (hasEnemy) {
                enemies.add(planet)
            }
        }

        return JyotishInfo(
            rashiLord = rashiLord,
            nakshatraLord = nakshatraLord,
            navamshaRashi = navamshaRashi.marathi,
            navamshaLord = navamshaLord,
            enemies = enemies
        )
    }
}
