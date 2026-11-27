package com.phoneguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat

/**
 * Foreground service that listens for theft-related system events and records them.
 * Receivers are registered at runtime because most of these broadcasts cannot be declared in the manifest.
 */
class GuardService : Service() {

    private var lastSimState = TelephonyManager.SIM_STATE_UNKNOWN
    private var lastLocationOn = true

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                    val on = intent.getBooleanExtra("state", false)
                    EventRecorder.record(
                        context,
                        if (on) EventType.AIRPLANE_ON else EventType.AIRPLANE_OFF,
                    )
                }
                Intent.ACTION_SHUTDOWN -> EventRecorder.record(context, EventType.SHUTDOWN)
                SIM_STATE_ACTION -> handleSimChange()
                LocationManager.PROVIDERS_CHANGED_ACTION -> handleLocationChange()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startInForeground()
        lastSimState = currentSimState()
        lastLocationOn = LocationHelper.isLocationEnabled(this)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(Intent.ACTION_SHUTDOWN)
            addAction(SIM_STATE_ACTION)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        EventRecorder.record(this, EventType.SERVICE_STARTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        try {
            unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered. Nothing to do.
        }
        super.onDestroy()
    }

    private fun currentSimState(): Int =
        (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simState

    private fun handleSimChange() {
        val state = currentSimState()
        if (state == lastSimState) return
        lastSimState = state
        val type = when (state) {
            TelephonyManager.SIM_STATE_ABSENT -> EventType.SIM_ABSENT
            TelephonyManager.SIM_STATE_READY -> EventType.SIM_READY
            else -> EventType.SIM_OTHER
        }
        EventRecorder.record(this, type, "simState=$state")
    }

    private fun handleLocationChange() {
        val on = LocationHelper.isLocationEnabled(this)
        if (on == lastLocationOn) return
        lastLocationOn = on
        EventRecorder.record(this, if (on) EventType.LOCATION_ON else EventType.LOCATION_OFF)
    }

    private fun startInForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Protection status", NotificationManager.IMPORTANCE_LOW),
        )
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("Phone Guard is on")
            .setContentText("Watching for theft-related events")
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "guard_status"
        const val NOTIFICATION_ID = 1
        const val SIM_STATE_ACTION = "android.intent.action.SIM_STATE_CHANGED"
    }
}
