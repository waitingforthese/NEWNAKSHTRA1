package com.mahaesuvidha.chandrapanchangalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (
            intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        Thread {
            try {
                AlarmScheduler(appContext).scheduleAll()
            } catch (t: Throwable) {
                android.util.Log.e(
                    "LifeAlarm",
                    "Failed to restore alarms after boot/package update",
                    t
                )
            } finally {
                pendingResult.finish()
            }
        }.start()
    }
}
