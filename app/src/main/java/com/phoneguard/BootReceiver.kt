package com.phoneguard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

/** Restarts protection after the phone starts, and records that it started. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val detail = when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> "before first unlock"
            Intent.ACTION_BOOT_COMPLETED -> "after unlock"
            else -> return
        }
        if (!Prefs.isEnabled(context)) return
        EventRecorder.record(context, EventType.BOOT, detail)
        ContextCompat.startForegroundService(context, Intent(context, GuardService::class.java))
    }
}
