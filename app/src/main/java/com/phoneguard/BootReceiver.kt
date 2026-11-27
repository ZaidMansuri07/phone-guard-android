package com.phoneguard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Restarts protection after the phone starts, and records that it started.
 *
 * Android 15 and newer also send these boot broadcasts when the app leaves the "stopped" state
 * (after a force stop or an app update), even though the phone did not restart. So the BOOT event
 * is recorded only once per real restart, using the system boot counter. The service is restarted
 * in every case, which is what we want after a force stop too.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val beforeUnlock = when (intent.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> true
            Intent.ACTION_BOOT_COMPLETED -> false
            else -> return
        }
        if (!Prefs.isEnabled(context)) return

        if (isNewBoot(context, beforeUnlock)) {
            EventRecorder.record(context, EventType.BOOT, if (beforeUnlock) "before first unlock" else "after unlock")
        }
        ContextCompat.startForegroundService(context, Intent(context, GuardService::class.java))
    }

    private fun isNewBoot(context: Context, beforeUnlock: Boolean): Boolean {
        val bootCount = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
        if (bootCount < 0) {
            // No boot counter on this phone. Treat it as a real restart only if the phone has just started.
            return SystemClock.elapsedRealtime() < RECENT_BOOT_MILLIS
        }
        if (Prefs.lastRecordedBoot(context, beforeUnlock) == bootCount) return false
        Prefs.setLastRecordedBoot(context, beforeUnlock, bootCount)
        return true
    }

    private companion object {
        const val RECENT_BOOT_MILLIS = 10 * 60 * 1000L
    }
}
