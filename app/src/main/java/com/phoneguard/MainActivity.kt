package com.phoneguard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator

/** Home: protection status, setup progress and the latest events. */
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpScreen(title = null, showBack = false)

        findViewById<View>(R.id.turnOnButton).setOnClickListener { setProtection(true) }
        findViewById<View>(R.id.turnOffButton).setOnClickListener { confirmTurnOff() }
        findViewById<View>(R.id.setupButton).setOnClickListener { open(SetupActivity::class.java) }
        findViewById<View>(R.id.setupCard).setOnClickListener { open(SetupActivity::class.java) }
        findViewById<View>(R.id.viewLogButton).setOnClickListener { open(LogActivity::class.java) }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_setup -> open(SetupActivity::class.java).let { true }
        R.id.menu_log -> open(LogActivity::class.java).let { true }
        R.id.menu_about -> open(AboutActivity::class.java).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun open(screen: Class<*>) = startActivity(Intent(this, screen))

    private fun confirmTurnOff() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Turn off protection?")
            .setMessage("Phone Guard will stop recording events until you turn it on again.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Turn off") { _, _ -> setProtection(false) }
            .show()
    }

    private fun setProtection(on: Boolean) {
        Prefs.setEnabled(this, on)
        val intent = Intent(this, GuardService::class.java)
        if (on) ContextCompat.startForegroundService(this, intent) else stopService(intent)
        refresh()
    }

    private fun refresh() {
        showStatus(Prefs.isEnabled(this))
        showSetup()
        showRecent()
    }

    private fun showStatus(on: Boolean) {
        val card = findViewById<MaterialCardView>(R.id.statusCard)
        val pill = findViewById<TextView>(R.id.statusPill)
        if (on) {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.state_on_bg))
            card.strokeColor = ContextCompat.getColor(this, R.color.state_on_bg)
            Ui.pill(pill, "On", R.color.brand, R.color.on_brand)
        } else {
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card))
            card.strokeColor = ContextCompat.getColor(this, R.color.outline)
            Ui.pill(pill, "Off", R.color.state_off_bg, R.color.state_off_text)
        }
        findViewById<TextView>(R.id.statusTitle).text = if (on) "Protected" else "Not protected"
        findViewById<TextView>(R.id.statusBody).text = if (on) {
            "Watching for airplane mode, shutdown, SIM changes and location being switched off."
        } else {
            "Turn on protection so suspicious events are recorded with the time and place."
        }
        findViewById<View>(R.id.turnOnButton).visibility = if (on) View.GONE else View.VISIBLE
        findViewById<View>(R.id.turnOffButton).visibility = if (on) View.VISIBLE else View.GONE
    }

    private fun showSetup() {
        val (done, total) = SetupSteps.progress(this)
        findViewById<TextView>(R.id.setupTitle).text = "$done of $total steps done"
        findViewById<LinearProgressIndicator>(R.id.setupProgress).apply {
            max = total
            setProgressCompat(done, false)
        }
        findViewById<TextView>(R.id.setupBody).text = if (done == total) {
            "All checks pass. Two steps can only be checked by you; see Setup."
        } else {
            "Finish setup so protection keeps working when it matters."
        }
        findViewById<TextView>(R.id.setupButton).text = if (done == total) "Review setup" else "Continue setup"
    }

    private fun showRecent() {
        val list = findViewById<LinearLayout>(R.id.recentList)
        list.removeAllViews()
        val events = EventLog(this).readAll().takeLast(3).reversed()
        val inflater = LayoutInflater.from(this)
        events.forEachIndexed { index, event ->
            if (index > 0) list.addView(Ui.divider(list))
            list.addView(Ui.eventRow(inflater, list, event))
        }
        findViewById<View>(R.id.recentEmpty).visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
    }
}
