package com.hypertrack.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics


object BatteryStateReceiver : BroadcastReceiver() {

    private var lastReportedDateTime = System.currentTimeMillis()

    override fun onReceive(context: Context, intent: Intent) {
        val time = System.currentTimeMillis()
        if (time - lastReportedDateTime > 1000 * 60 * 10) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPercent = level * 100 / scale.toFloat()
            FirebaseCrashlytics.getInstance().log("$KEY_BATTERY_VALUE $batteryPercent")
            lastReportedDateTime = time
        }
    }

    const val KEY_BATTERY_VALUE = "battery_value"
}
