package com.mahaesuvidha.chandrapanchangalarm.model

data class PanchangState(

    // BASIC
    val date: String,
    val weekday: String,

    // TITHI
    val tithi: String,
    val tithiStartTime: String = "—",
    val nextTithi: String,
    val nextTithiTime: String,
    val nextTithiMillis: Long,

    // YOGA
    val yoga: String,
    val yogaStartTime: String = "—",
    val nextYoga: String,
    val nextYogaTime: String,
    val nextYogaMillis: Long,

    // KARANA
    val karana: String,
    val karanaStartTime: String = "—",
    val nextKarana: String,
    val nextKaranaTime: String,
    val nextKaranaMillis: Long,

    // PAKSHA
    val paksha: String,
    val pakshaStartTime: String = "—",
    val nextPaksha: String,
    val nextPakshaTime: String,
    val nextPakshaMillis: Long,

    // MAS
    val masa: String = "—",
    val masaStartTime: String = "—",
    val nextMasa: String = "—",
    val nextMasaTime: String = "—",
    val nextMasaMillis: Long = 0L,

    // PRAHAR
    val prahar: String = "—",
    val praharStartTime: String = "—",
    val nextPrahar: String = "—",
    val nextPraharTime: String = "—",
    val nextPraharMillis: Long = 0L,

    // LAGNA
    val lagna: String = "—",
    val lagnaStartTime: String = "—",
    val nextLagna: String = "—",
    val nextLagnaTime: String = "—",
    val nextLagnaMillis: Long = 0L
)
