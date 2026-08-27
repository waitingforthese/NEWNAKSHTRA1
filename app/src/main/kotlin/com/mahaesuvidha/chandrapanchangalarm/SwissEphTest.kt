package com.mahaesuvidha.chandrapanchangalarm.model

import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph

object SwissEphTest {

    fun getMoonLongitude(): Double {

        val swe = SwissEph()

        val now =
            java.util.Calendar.getInstance()

        val year =
            now.get(java.util.Calendar.YEAR)

        val month =
            now.get(java.util.Calendar.MONTH) + 1

        val day =
            now.get(java.util.Calendar.DAY_OF_MONTH)

        val hour =
            now.get(java.util.Calendar.HOUR_OF_DAY)

        val minute =
            now.get(java.util.Calendar.MINUTE)

        val decimalHour =
            hour + minute / 60.0

        val jd =
            SweDate.getJulDay(
                year,
                month,
                day,
                decimalHour,
                SweDate.SE_GREG_CAL
            )

        val xx =
            DoubleArray(6)

        val serr =
            StringBuffer()

        swe.swe_calc_ut(
            jd,
            SweConst.SE_MOON,
            SweConst.SEFLG_SWIEPH,
            xx,
            serr
        )

        return xx[0]
    }
}
