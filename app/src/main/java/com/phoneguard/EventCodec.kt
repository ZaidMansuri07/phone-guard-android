package com.phoneguard

/**
 * Turns a [GuardEvent] into one line of text and back.
 * Fields are separated by tabs. Tabs, new lines and backslashes inside the detail text are escaped.
 * This file uses no Android classes, so it can be unit tested on a normal JVM.
 */
object EventCodec {

    fun encode(event: GuardEvent): String = listOf(
        event.timeMillis.toString(),
        event.type.name,
        if (event.locked) "1" else "0",
        escape(event.detail),
        event.latitude?.toString() ?: "",
        event.longitude?.toString() ?: "",
    ).joinToString("\t")

    /** Returns null if the line is damaged, so one bad line never breaks the whole log. */
    fun decode(line: String): GuardEvent? {
        val parts = line.split("\t")
        if (parts.size < 6) return null
        val time = parts[0].toLongOrNull() ?: return null
        val type = runCatching { EventType.valueOf(parts[1]) }.getOrNull() ?: return null
        return GuardEvent(
            timeMillis = time,
            type = type,
            locked = parts[2] == "1",
            detail = unescape(parts[3]),
            latitude = parts[4].toDoubleOrNull(),
            longitude = parts[5].toDoubleOrNull(),
        )
    }

    private fun escape(text: String): String = text
        .replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

    private fun unescape(text: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            if (ch == '\\' && i + 1 < text.length) {
                when (val next = text[i + 1]) {
                    't' -> out.append('\t')
                    'n' -> out.append('\n')
                    'r' -> out.append('\r')
                    '\\' -> out.append('\\')
                    else -> {
                        out.append(ch)
                        out.append(next)
                    }
                }
                i += 2
            } else {
                out.append(ch)
                i++
            }
        }
        return out.toString()
    }
}
