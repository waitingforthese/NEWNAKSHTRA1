package com.mahaesuvidha.chandrapanchangalarm.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val title =
            intent.getStringExtra("title")
                ?: "चंद्र सूर्य अलार्म"

        val message =
            intent.getStringExtra("message")
                ?: "पंचांग बदल झाला आहे."

        val id =
            intent.getIntExtra("id", 1)

        val eventAt =
            intent.getLongExtra("eventAt", 0L)

        val soundResource =
            intent.getStringExtra("soundResource")
                ?: "alarm"

        // Android normally delivers an alarm only once, but OEM battery
        // managers and repeated scheduling can occasionally result in the
        // same broadcast being delivered twice. Ignore an identical event
        // so sound/vibration cannot fire twice for one astronomical change.
        val firedPrefs =
            context.getSharedPreferences(
                "life_alarm_fired_events",
                Context.MODE_PRIVATE
            )

        val firedKey = "fired_$id"
        val lastFiredAt = firedPrefs.getLong(firedKey, -1L)
        if (eventAt > 0L && lastFiredAt == eventAt) {
            return
        }

        if (eventAt > 0L) {
            firedPrefs.edit()
                .putLong(firedKey, eventAt)
                .apply()
        }

        showNotification(
            context,
            title,
            message,
            id,
            soundResource
        )

        // The next astronomical alarm must be calculated again after
        // a real alarm fires, but this calculation is intentionally NOT
        // performed on the BroadcastReceiver thread.
        if (id in 1..3 || id in 11..13 || id in 21..27) {
            val pendingResult = goAsync()
            val appContext = context.applicationContext

            Thread {
                try {
                    AlarmScheduler(appContext).scheduleAll()
                } catch (t: Throwable) {
                    android.util.Log.e(
                        "LifeAlarm",
                        "Failed to schedule next alarm after alarm id=$id",
                        t
                    )
                } finally {
                    pendingResult.finish()
                }
            }.start()
        }
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        id: Int,
        soundResource: String
    ) {

        // Android 8+ locks a notification channel's sound after creation.
        // Therefore each ringtone gets its own stable channel.
        val channelId =
            "life_alarm_${soundResource}_v2"

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val soundUri =
            Uri.parse(
                "android.resource://${context.packageName}/raw/$soundResource"
            )

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val audioAttributes =
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_ALARM
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()

            val channel =
                NotificationChannel(
                    channelId,
                    "चंद्र सूर्य अलार्म",
                    NotificationManager.IMPORTANCE_HIGH
                )

            channel.description =
                "राशी, नक्षत्र, चरण आणि पंचांग बदल अलार्म"

            channel.enableVibration(true)

            channel.setSound(
                soundUri,
                audioAttributes
            )

            notificationManager.createNotificationChannel(
                channel
            )
        }

        val notification =
            NotificationCompat.Builder(
                context,
                channelId
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setCategory(
                    NotificationCompat.CATEGORY_ALARM
                )
                .setAutoCancel(true)
                .setSound(soundUri)
                .build()

        notificationManager.notify(
            id,
            notification
        )
    }
}
