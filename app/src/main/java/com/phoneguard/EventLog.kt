package com.phoneguard

import android.content.Context
import java.io.File

/**
 * Keeps the event log in a plain file on the phone.
 *
 * It uses "device protected" storage so events can be saved even before the owner unlocks
 * the phone for the first time after a restart (a thief restarting the phone is a likely case).
 * Trade-off: that storage is less protected than normal app storage. See README, "Privacy".
 */
class EventLog(context: Context) {

    private val file: File =
        File(context.applicationContext.createDeviceProtectedStorageContext().filesDir, FILE_NAME)

    fun add(event: GuardEvent) {
        synchronized(LOCK) {
            file.appendText(EventCodec.encode(event) + "\n")
            trimIfNeeded()
        }
    }

    fun readAll(): List<GuardEvent> {
        synchronized(LOCK) {
            if (!file.exists()) return emptyList()
            return file.readLines().mapNotNull { EventCodec.decode(it) }
        }
    }

    fun clear() {
        synchronized(LOCK) {
            if (file.exists()) file.delete()
        }
    }

    private fun trimIfNeeded() {
        val lines = file.readLines()
        if (lines.size > MAX_LINES) {
            file.writeText(lines.takeLast(MAX_LINES).joinToString("\n") + "\n")
        }
    }

    private companion object {
        const val FILE_NAME = "events.log"
        const val MAX_LINES = 500
        val LOCK = Any()
    }
}
