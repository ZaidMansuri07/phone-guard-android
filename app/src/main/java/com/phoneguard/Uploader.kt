package com.phoneguard

/**
 * Where events go once the phone has a connection. Phase 1 only keeps them on the phone.
 * Phase 2 (see docs/ROADMAP.md) replaces [current] with an uploader that sends events to the server
 * and queues them when there is no connection.
 */
interface EventUploader {
    fun upload(event: GuardEvent)
}

object NoopUploader : EventUploader {
    override fun upload(event: GuardEvent) {
        // Intentionally empty in phase 1.
    }
}

object Uploader {
    @Volatile
    var current: EventUploader = NoopUploader
}
