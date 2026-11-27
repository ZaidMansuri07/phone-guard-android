package com.phoneguard

/** Things the app records. Each one is a possible sign that the phone was stolen. */
enum class EventType(val label: String) {
    SERVICE_STARTED("Protection started"),
    BOOT("Phone started"),
    SHUTDOWN("Phone is shutting down"),
    AIRPLANE_ON("Airplane mode turned ON"),
    AIRPLANE_OFF("Airplane mode turned off"),
    SIM_ABSENT("SIM removed"),
    SIM_READY("SIM ready"),
    SIM_OTHER("SIM state changed"),
    LOCATION_OFF("Location turned OFF"),
    LOCATION_ON("Location turned on"),
    WRONG_PIN("Wrong unlock attempt"),
    ADMIN_ENABLED("Device admin turned on"),
    ADMIN_DISABLED("Device admin turned OFF"),
}

/** One recorded event. [locked] is true if the screen was locked when it happened. */
data class GuardEvent(
    val timeMillis: Long,
    val type: EventType,
    val locked: Boolean,
    val detail: String,
    val latitude: Double?,
    val longitude: Double?,
)
