package com.mahaesuvidha.chandrapanchangalarm.model

data class MoonState(
    val location: String,

    val rashi: Rashi,
    val nakshatra: Nakshatra,
    val pada: Int,

    val nextRashi: String,
    val nextRashiTime: String,
    val nextRashiMillis: Long,

    val nakshatraStartTime: String,

    val nextNakshatra: String,
    val nextNakshatraTime: String,
    val nextNakshatraMillis: Long,

    val nextCharan: String,
    val nextCharanTime: String,
    val nextCharanMillis: Long
)

enum class Graha(
    val marathi: String
) {
    SURYA("सूर्य"),
    CHANDRA("चंद्र"),
    MANGAL("मंगळ"),
    BUDH("बुध"),
    GURU("गुरु"),
    SHUKRA("शुक्र"),
    SHANI("शनि"),
    RAHU("राहू"),
    KETU("केतू")
}

enum class Rashi(
    val marathi: String,
    val swami: Graha
) {
    MESHA("मेष", Graha.MANGAL),
    VRISHABHA("वृषभ", Graha.SHUKRA),
    MITHUNA("मिथुन", Graha.BUDH),
    KARKA("कर्क", Graha.CHANDRA),
    SIMHA("सिंह", Graha.SURYA),
    KANYA("कन्या", Graha.BUDH),
    TULA("तुळ", Graha.SHUKRA),
    VRISHCHIKA("वृश्चिक", Graha.MANGAL),
    DHANU("धनु", Graha.GURU),
    MAKARA("मकर", Graha.SHANI),
    KUMBHA("कुंभ", Graha.SHANI),
    MEENA("मीन", Graha.GURU)
}

enum class Nakshatra(
    val marathi: String,
    val swami: Graha
) {
    ASHWINI("अश्विनी", Graha.KETU),
    BHARANI("भरणी", Graha.SHUKRA),
    KRITTIKA("कृत्तिका", Graha.SURYA),
    ROHINI("रोहिणी", Graha.CHANDRA),
    MRIGASHIRSHA("मृगशीर्ष", Graha.MANGAL),
    ARDRA("आर्द्रा", Graha.RAHU),
    PUNARVASU("पुनर्वसू", Graha.GURU),
    PUSHYA("पुष्य", Graha.SHANI),
    ASHLESHA("आश्लेषा", Graha.BUDH),
    MAGHA("मघा", Graha.KETU),
    PURVA_PHALGUNI("पूर्वाफाल्गुनी", Graha.SHUKRA),
    UTTARA_PHALGUNI("उत्तराफाल्गुनी", Graha.SURYA),
    HASTA("हस्त", Graha.CHANDRA),
    CHITRA("चित्रा", Graha.MANGAL),
    SWATI("स्वाती", Graha.RAHU),
    VISHAKHA("विशाखा", Graha.GURU),
    ANURADHA("अनुराधा", Graha.SHANI),
    JYESHTHA("ज्येष्ठा", Graha.BUDH),
    MOOLA("मूळ", Graha.KETU),
    PURVA_ASHADHA("पूर्वाषाढा", Graha.SHUKRA),
    UTTARA_ASHADHA("उत्तराषाढा", Graha.SURYA),
    SHRAVANA("श्रवण", Graha.CHANDRA),
    DHANISHTHA("धनिष्ठा", Graha.MANGAL),
    SHATABHISHA("शततारका", Graha.RAHU),
    PURVA_BHADRAPADA("पूर्वाभाद्रपदा", Graha.GURU),
    UTTARA_BHADRAPADA("उत्तराभाद्रपदा", Graha.SHANI),
    REVATI("रेवती", Graha.BUDH)
}
