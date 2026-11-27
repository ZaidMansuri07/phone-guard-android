package com.phoneguard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Full event log, newest first, with share and clear. */
class LogActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setUpScreen(title = "Event log", showBack = true)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.log, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_share -> share().let { true }
        R.id.menu_clear -> confirmClear().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun events() = EventLog(this).readAll().reversed()

    private fun render() {
        val events = events()
        findViewById<TextView>(R.id.intro).text = when (events.size) {
            0 -> "Nothing recorded yet. Turn on protection on the home screen and events will appear here."
            1 -> "1 event, newest first. Red marks events that often mean someone is tampering with the phone."
            else -> "${events.size} events, newest first. Red marks events that often mean someone is " +
                "tampering with the phone."
        }
        val list = findViewById<LinearLayout>(R.id.list)
        list.removeAllViews()
        if (events.isEmpty()) return

        val card = MaterialCardView(this).apply {
            setContentPadding(dp(20), dp(4), dp(20), dp(4))
        }
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val inflater = LayoutInflater.from(this)
        events.forEachIndexed { index, event ->
            if (index > 0) column.addView(Ui.divider(column))
            column.addView(Ui.eventRow(inflater, column, event))
        }
        card.addView(column)
        list.addView(card)
    }

    private fun share() {
        val events = events()
        if (events.isEmpty()) return
        val text = buildString {
            appendLine("Phone Guard event log")
            appendLine()
            for (e in events) {
                appendLine("${Ui.fullTime(e.timeMillis)}  ${e.type.label}")
                appendLine("  ${Ui.meta(e)}")
            }
        }
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, "Phone Guard event log")
            .putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, "Share log"))
    }

    private fun confirmClear() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear the event log?")
            .setMessage("All recorded events will be deleted from this phone. This cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Clear") { _, _ ->
                EventLog(this).clear()
                render()
            }
            .show()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
