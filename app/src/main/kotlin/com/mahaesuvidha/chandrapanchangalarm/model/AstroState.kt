package com.mahaesuvidha.chandrapanchangalarm.model

data class AstroState(

    val body: String,

    val rashi: Rashi,
    val nakshatra: Nakshatra,
    val pada: Int,

    val nextRashi: String,
    val nextRashiMillis: Long,

    val nextNakshatra: String,
    val nextNakshatraMillis: Long,

    val nextCharan: String,
    val nextCharanMillis: Long
)
