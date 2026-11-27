package com.phoneguard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EventCodecTest {

    @Test
    fun roundTripKeepsEveryField() {
        val event = GuardEvent(
            timeMillis = 1_700_000_000_000L,
            type = EventType.AIRPLANE_ON,
            locked = true,
            detail = "state=on",
            latitude = 23.0225,
            longitude = 72.5714,
        )
        assertEquals(event, EventCodec.decode(EventCodec.encode(event)))
    }

    @Test
    fun missingLocationStaysNull() {
        val event = GuardEvent(1L, EventType.BOOT, false, "", null, null)
        assertEquals(event, EventCodec.decode(EventCodec.encode(event)))
    }

    @Test
    fun tabsNewLinesAndBackslashesInDetailSurvive() {
        val detail = "a\tb\nc\rd\\e\\t"
        val event = GuardEvent(5L, EventType.WRONG_PIN, true, detail, null, null)
        val line = EventCodec.encode(event)
        assertEquals("encoded event must stay on one line", false, line.contains("\n"))
        assertEquals(detail, EventCodec.decode(line)?.detail)
    }

    @Test
    fun damagedLinesReturnNull() {
        assertNull(EventCodec.decode(""))
        assertNull(EventCodec.decode("not\tenough"))
        assertNull(EventCodec.decode("abc\tBOOT\t1\t\t\t"))
        assertNull(EventCodec.decode("1\tNOT_A_TYPE\t1\t\t\t"))
    }
}
