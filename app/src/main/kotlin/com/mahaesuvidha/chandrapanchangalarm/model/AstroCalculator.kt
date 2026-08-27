package com.mahaesuvidha.chandrapanchangalarm.model

object AstroCalculator {

    fun moon(): AstroState {

        val state =
            LiveMoonCalculator.getCurrentMoonState()

        return AstroState(

            body = "चंद्र",

            rashi = state.rashi,
            nakshatra = state.nakshatra,
            pada = state.pada,

            nextRashi = state.nextRashi,
            nextRashiMillis = state.nextRashiMillis,

            nextNakshatra = state.nextNakshatra,
            nextNakshatraMillis = state.nextNakshatraMillis,

            nextCharan = state.nextCharan,
            nextCharanMillis = state.nextCharanMillis
        )
    }

    fun sun(): AstroState {

        val state =
            LiveSunCalculator.getCurrentSunState()

        return AstroState(

            body = "सूर्य",

            rashi = state.rashi,
            nakshatra = state.nakshatra,
            pada = state.pada,

            nextRashi = state.nextRashi,
            nextRashiMillis = state.nextRashiMillis,

            nextNakshatra = state.nextNakshatra,
            nextNakshatraMillis = state.nextNakshatraMillis,

            nextCharan = state.nextCharan,
            nextCharanMillis = state.nextCharanMillis
        )
    }
}
