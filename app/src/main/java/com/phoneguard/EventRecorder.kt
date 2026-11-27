package com.phoneguard

import android.app.KeyguardManager
import android.content.Context

/** Single place where every event is created, saved and handed to the uploader. */
object EventRecorder {

    fun record(context: Context, type: EventType, detail: String = "") {
        val app = context.applicationContext
        val keyguard = app.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val location = LocationHelper.lastKnown(app)
        val event = GuardEvent(
            timeMillis = System.currentTimeMillis(),
            type = type,
            locked = keyguard.isKeyguardLocked,
            detail = detail,
            latitude = location?.first,
            longitude = location?.second,
        )
        EventLog(app).add(event)
        Uploader.current.upload(event)
    }
}
