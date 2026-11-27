package com.phoneguard

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Small view helpers shared by the screens. */
object Ui {

    /** Events that look like a thief at work. They get a warm red accent in the log. */
    private val SUSPICIOUS = setOf(
        EventType.SHUTDOWN,
        EventType.AIRPLANE_ON,
        EventType.SIM_ABSENT,
        EventType.SIM_OTHER,
        EventType.LOCATION_OFF,
        EventType.WRONG_PIN,
        EventType.ADMIN_DISABLED,
    )

    fun isSuspicious(type: EventType) = type in SUSPICIOUS

    fun eventRow(inflater: LayoutInflater, parent: ViewGroup, event: GuardEvent): View {
        val row = inflater.inflate(R.layout.item_event, parent, false)
        val context = row.context
        row.findViewById<TextView>(R.id.title).text = event.type.label
        row.findViewById<TextView>(R.id.meta).text = meta(event)
        row.findViewById<TextView>(R.id.time).text = shortTime(event.timeMillis)
        val accent = if (isSuspicious(event.type)) R.color.state_off_text else R.color.outline
        row.findViewById<View>(R.id.accent).backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(context, accent))
        return row
    }

    /** A thin line between rows. */
    fun divider(parent: ViewGroup): View = View(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
        setBackgroundColor(ContextCompat.getColor(context, R.color.outline))
    }

    fun pill(view: TextView, text: String, @ColorRes background: Int, @ColorRes foreground: Int) {
        view.text = text
        view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(view.context, background))
        view.setTextColor(ContextCompat.getColor(view.context, foreground))
    }

    fun meta(event: GuardEvent): String {
        val parts = mutableListOf(if (event.locked) "Screen locked" else "Screen unlocked")
        if (event.detail.isNotEmpty()) parts.add(event.detail)
        val text = parts.joinToString("  ·  ")
        if (event.latitude == null || event.longitude == null) return text
        return text + "\n" + String.format(Locale.US, "%.5f, %.5f", event.latitude, event.longitude)
    }

    /** "09:58" for today, "Yesterday" for yesterday, "27 Nov" for older events. */
    fun shortTime(millis: Long): String {
        val event = Calendar.getInstance().apply { timeInMillis = millis }
        val now = Calendar.getInstance()
        val sameYear = event.get(Calendar.YEAR) == now.get(Calendar.YEAR)
        val dayDiff = now.get(Calendar.DAY_OF_YEAR) - event.get(Calendar.DAY_OF_YEAR)
        return when {
            sameYear && dayDiff == 0 -> format("HH:mm", millis)
            sameYear && dayDiff == 1 -> "Yesterday"
            sameYear -> format("d MMM", millis)
            else -> format("d MMM yyyy", millis)
        }
    }

    fun fullTime(millis: Long): String = format("d MMM yyyy, HH:mm:ss", millis)

    private fun format(pattern: String, millis: Long) =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}
