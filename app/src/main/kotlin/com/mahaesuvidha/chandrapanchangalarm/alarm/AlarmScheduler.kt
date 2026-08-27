package com.mahaesuvidha.chandrapanchangalarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

import com.mahaesuvidha.chandrapanchangalarm.model.LiveMoonCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.LiveSunCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.LivePanchangCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.BirthProfileStore
import com.mahaesuvidha.chandrapanchangalarm.model.NakshatraGuidanceCalculator
import com.mahaesuvidha.chandrapanchangalarm.settings.AlarmPrefs
import com.mahaesuvidha.chandrapanchangalarm.settings.LocationPrefs

class AlarmScheduler(
    private val context: Context
) {

    private val alarmManager =
        context.getSystemService(
            AlarmManager::class.java
        )

    /**
     * Stores the exact event currently represented by each alarm ID.
     * This makes scheduleAll() idempotent: repeated location/UI/boot
     * refreshes do not recreate the same alarm or produce duplicates.
     */
    private val scheduledPrefs =
        context.getSharedPreferences(
            "life_alarm_scheduled_events",
            Context.MODE_PRIVATE
        )

    // ==========================================
    // TEST ALARMS
    // ==========================================

    fun scheduleRashiAlarm(
        delayMillis: Long
    ) {
        schedule(
            id = 101,
            at = System.currentTimeMillis() + delayMillis,
            title = "🌙 चंद्र राशी बदल",
            message = "ही राशी बदलाची टेस्ट सूचना आहे.",
            soundResource = "rashi"
        )
    }

    fun scheduleNakshatraAlarm(
        delayMillis: Long
    ) {
        schedule(
            id = 102,
            at = System.currentTimeMillis() + delayMillis,
            title = "🌙 चंद्र नक्षत्र बदल",
            message = "ही नक्षत्र बदलाची टेस्ट सूचना आहे.",
            soundResource = "nakshatra"
        )
    }

    fun scheduleCharanAlarm(
        delayMillis: Long
    ) {
        schedule(
            id = 103,
            at = System.currentTimeMillis() + delayMillis,
            title = "🌙 चंद्र चरण बदल",
            message = "ही चरण बदलाची टेस्ट सूचना आहे.",
            soundResource = "charan"
        )
    }

    fun schedulePraharAlarm(
        delayMillis: Long
    ) {
        schedule(
            id = 104,
            at = System.currentTimeMillis() + delayMillis,
            title = "⌛ प्रहर बदल",
            message = "ही प्रहर बदलाची टेस्ट सूचना आहे."
        )
    }

    fun scheduleLagnaAlarm(
        delayMillis: Long
    ) {
        schedule(
            id = 105,
            at = System.currentTimeMillis() + delayMillis,
            title = "⭐ लग्न बदल",
            message = "ही लग्न बदलाची टेस्ट सूचना आहे."
        )
    }

    // ==========================================
    // SCHEDULE ALL ALARMS
    // ==========================================

    fun scheduleAll() {

        val prefs = AlarmPrefs(context)
        val location = LocationPrefs(context)

        // Personalized Nakshatra Guidance: the change notification and the
        // 3-hour reminder use the same live Nakshatra/Tara-Bala calculation
        // and the same Do/Avoid text shown on the Guidance card.
        scheduleNakshatraGuidanceAlarms()

        // IMPORTANT: do not call cancelAll() here. scheduleAll() can be
        // triggered by GPS updates, boot and an alarm firing. Cancelling
        // everything first creates unnecessary churn and can momentarily
        // remove a valid alarm. Each stable ID is instead reconciled below.

        // ==========================================
        // MOON ALARMS — IDs 1..3
        // ==========================================
        if (prefs.moonRashi || prefs.moonNakshatra || prefs.moonCharan) {
            val moon = LiveMoonCalculator.getCurrentMoonState()

            reconcile(
                id = 1,
                enabled = prefs.moonRashi,
                at = moon.nextRashiMillis,
                title = "🌙 चंद्र राशी बदल",
                message = moon.nextRashi,
                soundResource = "rashi"
            )
            reconcile(
                id = 2,
                enabled = prefs.moonNakshatra,
                at = moon.nextNakshatraMillis,
                title = "🌙 चंद्र नक्षत्र बदल",
                message = moon.nextNakshatra,
                soundResource = "nakshatra"
            )
            reconcile(
                id = 3,
                enabled = prefs.moonCharan,
                at = moon.nextCharanMillis,
                title = "🌙 चंद्र चरण बदल",
                message = moon.nextCharan,
                soundResource = "charan"
            )
        } else {
            cancel(1); cancel(2); cancel(3)
        }

        // ==========================================
        // SUN ALARMS — IDs 11..13
        // ==========================================
        if (prefs.sunRashi || prefs.sunNakshatra || prefs.sunCharan) {
            val sun = LiveSunCalculator.getCurrentSunState()

            reconcile(
                id = 11,
                enabled = prefs.sunRashi,
                at = sun.nextRashiMillis,
                title = "☀️ सूर्य राशी बदल",
                message = sun.nextRashi,
                soundResource = "rashi"
            )
            reconcile(
                id = 12,
                enabled = prefs.sunNakshatra,
                at = sun.nextNakshatraMillis,
                title = "☀️ सूर्य नक्षत्र बदल",
                message = sun.nextNakshatra,
                soundResource = "nakshatra"
            )
            reconcile(
                id = 13,
                enabled = prefs.sunCharan,
                at = sun.nextCharanMillis,
                title = "☀️ सूर्य चरण बदल",
                message = sun.nextCharan,
                soundResource = "charan"
            )
        } else {
            cancel(11); cancel(12); cancel(13)
        }

        // ==========================================
        // PANCHANG ALARMS — IDs 21..24, 26, 27
        // Prahar and Lagna are calculated with the saved live location.
        // ==========================================
        val needsPanchang =
            prefs.tithiAlarm ||
            prefs.yogaAlarm ||
            prefs.karanaAlarm ||
            prefs.pakshaAlarm ||
            prefs.praharAlarm ||
            prefs.lagnaAlarm

        if (needsPanchang) {
            val panchang = LivePanchangCalculator.getCurrentPanchangState(
                latitude = location.latitude,
                longitude = location.longitude
            )

            reconcile(
                id = 21,
                enabled = prefs.tithiAlarm,
                at = panchang.nextTithiMillis,
                title = "🔔 तिथी बदल",
                message = "${panchang.tithi} → ${panchang.nextTithi}",
                soundResource = "tithi_badal"
            )
            reconcile(
                id = 22,
                enabled = prefs.yogaAlarm,
                at = panchang.nextYogaMillis,
                title = "🔔 योग बदल",
                message = "${panchang.yoga} → ${panchang.nextYoga}",
                soundResource = "yog_badal"
            )
            reconcile(
                id = 23,
                enabled = prefs.karanaAlarm,
                at = panchang.nextKaranaMillis,
                title = "🔔 करण बदल",
                message = "${panchang.karana} → ${panchang.nextKarana}",
                soundResource = "karan_badal"
            )
            reconcile(
                id = 24,
                enabled = prefs.pakshaAlarm,
                at = panchang.nextPakshaMillis,
                title = "🔔 पक्ष बदल",
                message = "${panchang.paksha} → ${panchang.nextPaksha}",
                soundResource = "paksh_badal"
            )
            reconcile(
                id = 26,
                enabled = prefs.praharAlarm,
                at = panchang.nextPraharMillis,
                title = "⌛ प्रहर बदल",
                message = "${panchang.prahar} → ${panchang.nextPrahar}",
                soundResource = "prahar_badal"
            )
            reconcile(
                id = 27,
                enabled = prefs.lagnaAlarm,
                at = panchang.nextLagnaMillis,
                title = "⭐ लग्न बदल",
                message = "${panchang.lagna} → ${panchang.nextLagna}",
                soundResource = "lagn_badal"
            )
        } else {
            cancel(21); cancel(22); cancel(23); cancel(24); cancel(26); cancel(27)
        }
    }

    private fun scheduleNakshatraGuidanceAlarms() {
        val profile = BirthProfileStore.load(context.applicationContext)
        if (profile == null || profile.birthNakshatra.isBlank()) {
            cancel(121)
            cancel(122)
            return
        }

        val moon = LiveMoonCalculator.getCurrentMoonState()

        // If the existing Moon-Nakshatra alarm is enabled, turn that same
        // event into the personalized guidance notification in AlarmReceiver.
        // Otherwise use the dedicated guidance-change alarm. This prevents
        // duplicate notifications for the same Nakshatra transition.
        if (AlarmPrefs(context).moonNakshatra) {
            cancel(121)
        } else {
            reconcile(
                id = 121,
                enabled = true,
                at = moon.nextNakshatraMillis,
                title = "🌙 नक्षत्र मार्गदर्शन",
                message = "सध्याचे नक्षत्र व तारा मार्गदर्शन पाहा.",
                soundResource = "nakshatra"
            )
        }

        // Personalized guidance reminder every 3 hours. Keep an existing
        // future reminder untouched so unrelated refreshes/location updates
        // do not reset the 3-hour interval. After delivery, AlarmReceiver
        // calls scheduleAll() and a fresh 3-hour reminder is created.
        val reminderAlreadyScheduled =
            scheduledPrefs.getString("event_122", null) != null && isAlarmScheduled(122)
        if (!reminderAlreadyScheduled) {
            val nextReminder = System.currentTimeMillis() + 3L * 60L * 60L * 1000L
            reconcile(
                id = 122,
                enabled = true,
                at = nextReminder,
                title = "🌙 चालू नक्षत्र मार्गदर्शन",
                message = "सध्याचे नक्षत्र, तारा, काय करावे आणि काय टाळावे पाहा.",
                soundResource = "nakshatra"
            )
        }
    }

    /**
     * Reconcile one real alarm. If the exact same event is already scheduled,
     * leave it alone. If the event time/message changed, replace it.
     */
    private fun reconcile(
        id: Int,
        enabled: Boolean,
        at: Long,
        title: String,
        message: String,
        soundResource: String? = null
    ) {
        if (!enabled || at <= 0L) {
            cancel(id)
            return
        }

        val now = System.currentTimeMillis()
        if (at <= now) {
            // A calculator must never create a past real alarm. The next
            // event will be calculated again after the current cycle.
            cancel(id)
            return
        }

        val signature = "$at|$title|$message"
        val oldSignature = scheduledPrefs.getString("event_$id", null)
        if (oldSignature == signature && isAlarmScheduled(id)) {
            return
        }

        schedule(
            id = id,
            at = at,
            title = title,
            message = message,
            soundResource = soundResource
        )
    }

    // ==========================================
    // GENERAL TEST ALARM
    // ==========================================

    fun scheduleTest(
        type: String
    ) {

        val soundResource =
            when {
                type.contains("राशी") -> "rashi"
                type.contains("नक्षत्र") -> "nakshatra"
                type.contains("चरण") -> "charan"
                else -> "alarm"
            }

        schedule(
            id = 99,
            at = System.currentTimeMillis() + 10_000L,
            title = "🔔 $type बदल Test",
            message = "$type Test Alarm आहे.",
            soundResource = soundResource
        )
    }

    // ==========================================
    // PANCHANG RINGTONE TEST
    // ==========================================

    fun schedulePanchangTestSequence() {

        val tests =
            listOf(
                Triple(201, "📅 तिथी बदल Test", "tithi_badal"),
                Triple(202, "✨ योग बदल Test", "yog_badal"),
                Triple(203, "🔔 करण बदल Test", "karan_badal"),
                Triple(204, "🌗 पक्ष बदल Test", "paksh_badal"),
                Triple(205, "⏳ प्रहर बदल Test", "prahar_badal"),
                Triple(206, "⭐ लग्न बदल Test", "lagn_badal")
            )

        val start =
            System.currentTimeMillis() + 2_000L

        tests.forEachIndexed { index, item ->

            schedule(
                id = item.first,
                at = start + (index * 5_000L),
                title = item.second,
                message = "Panchang ringtone test",
                soundResource = item.third
            )
        }
    }

    // ==========================================
    // MAIN SCHEDULE FUNCTION
    // ==========================================

    private fun schedule(
        id: Int,
        at: Long,
        title: String,
        message: String,
        soundResource: String? = null
    ) {

        val safeAt = maxOf(
            at,
            System.currentTimeMillis() + 2_000L
        )

        val exactAllowed =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()

        if (!exactAllowed && id != 99 && id !in 101..105) {
            // Do not launch Settings from a BroadcastReceiver/background
            // thread. The user can grant exact-alarm access from the app.
            android.util.Log.w(
                "LifeAlarm",
                "Exact alarm permission unavailable for id=$id"
            )
            return
        }

        cancel(id)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("id", id)
            putExtra("eventAt", safeAt)
            if (soundResource != null) {
                putExtra("soundResource", soundResource)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (exactAllowed) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    safeAt,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    safeAt,
                    pendingIntent
                )
            }

            scheduledPrefs.edit()
                .putString("event_$id", "$at|$title|$message")
                .apply()
        } catch (t: Throwable) {
            android.util.Log.e(
                "LifeAlarm",
                "Unable to schedule alarm id=$id",
                t
            )
        }
    }

    private fun isAlarmScheduled(id: Int): Boolean {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    // ==========================================
    // CANCEL ALL ALARMS
    // ==========================================

    fun cancelAll() {

        // All persistent alarms:
        // 1..3   = Moon: Rashi / Nakshatra / Charan
        // 11..13 = Sun:  Rashi / Nakshatra / Charan
        // 21..24 = Panchang: Tithi / Yoga / Karana / Paksha
        // 26     = Prahar
        // 27     = Lagna
        for (id in 1..3) {
            cancel(id)
        }

        for (id in 11..13) {
            cancel(id)
        }

        for (id in 21..27) {
            cancel(id)
        }

        // Test alarms
        cancel(99)
        cancel(101)
        cancel(102)
        cancel(103)
        cancel(104)
        cancel(105)
        cancel(121)
        cancel(122)

        for (id in 201..206) {
            cancel(id)
        }
    }

    // ==========================================
    // CANCEL SINGLE ALARM
    // ==========================================

    private fun cancel(
        id: Int
    ) {

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                id,
                Intent(
                    context,
                    AlarmReceiver::class.java
                ),
                PendingIntent.FLAG_NO_CREATE or
                        PendingIntent.FLAG_IMMUTABLE
            )

        if (pendingIntent != null) {
            try {
                alarmManager.cancel(pendingIntent)
            } catch (t: Throwable) {
                android.util.Log.e(
                    "LifeAlarm",
                    "Unable to cancel alarm id=$id",
                    t
                )
            }

            try {
                pendingIntent.cancel()
            } catch (_: Throwable) {
            }
        }

        scheduledPrefs.edit()
            .remove("event_$id")
            .apply()
    }
}
