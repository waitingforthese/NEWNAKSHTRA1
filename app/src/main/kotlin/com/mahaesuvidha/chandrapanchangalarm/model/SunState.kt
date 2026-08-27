package com.mahaesuvidha.chandrapanchangalarm.model

data class SunState(

    val rashi: Rashi,
    val nakshatra: Nakshatra,
    val pada: Int,

    val nextRashi: String,
    val nextRashiTime: String,
    val nextRashiMillis: Long,

    val nextNakshatra: String,
    val nextNakshatraTime: String,
    val nextNakshatraMillis: Long,

    val nextCharan: String,
    val nextCharanTime: String,
    val nextCharanMillis: Long
)
