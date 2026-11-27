package com.phoneguard

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var logText: TextView

    private class Step(
        val title: String,
        val description: String,
        val buttonLabel: String,
        val action: (() -> Unit)?,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        logText = findViewById(R.id.logText)
        findViewById<Button>(R.id.startButton).setOnClickListener { setProtection(true) }
        findViewById<Button>(R.id.stopButton).setOnClickListener { setProtection(false) }
        findViewById<Button>(R.id.refreshButton).setOnClickListener { refresh() }
        buildChecklist()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun setProtection(on: Boolean) {
        Prefs.setEnabled(this, on)
        val intent = Intent(this, GuardService::class.java)
        if (on) ContextCompat.startForegroundService(this, intent) else stopService(intent)
        refresh()
    }

    private fun steps(): List<Step> = listOf(
        Step(
            "1. Notifications",
            "Needed on Android 13+ so the protection service can show its status notification.",
            "Allow notifications",
            action = {
                if (Build.VERSION.SDK_INT >= 33) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                }
            },
        ),
        Step(
            "2. Location",
            "Lets the app save where the phone was when something suspicious happens.",
            "Allow location",
            action = {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    2,
                )
            },
        ),
        Step(
            "3. Location: Allow all the time",
            "Open this app's settings and set Location to 'Allow all the time' so it still works in the background.",
            "Open app settings",
            action = {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")),
                )
            },
        ),
        Step(
            "4. Battery",
            "Remove battery restrictions for this app so the phone maker does not stop the service.",
            "Open battery settings",
            action = { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) },
        ),
        Step(
            "5. Device admin",
            "Makes the app harder to uninstall and lets it see wrong-PIN attempts.",
            "Activate device admin",
            action = { requestAdmin() },
        ),
        Step(
            "6. Quick settings on the lock screen",
            "Stops a thief turning on airplane mode from the lock screen. The name differs by brand. " +
                "On ColorOS look for 'Allow Quick Settings on Lock screen' and turn it OFF. " +
                "On other phones search the settings for 'lock screen' or 'quick settings'. " +
                "The app cannot change this for you.",
            "Open settings",
            action = { startActivity(Intent(Settings.ACTION_SETTINGS)) },
        ),
        Step(
            "7. Strong screen lock",
            "Use a long PIN, password or biometrics. A weak or watched PIN lets a thief change settings.",
            "Open security settings",
            action = { startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) },
        ),
        Step(
            "8. Google account and Find Hub",
            "Keep your Google account signed in on the phone and Find Hub switched on. " +
                "This keeps Google's factory reset protection working. The app cannot do this for you.",
            "",
            action = null,
        ),
    )

    private fun buildChecklist() {
        val container = findViewById<LinearLayout>(R.id.checklist)
        for (step in steps()) {
            container.addView(TextView(this).apply {
                text = step.title
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(12), 0, 0)
            })
            container.addView(TextView(this).apply {
                text = step.description
                textSize = 13f
            })
            val action = step.action
            if (action != null) {
                container.addView(Button(this).apply {
                    text = step.buttonLabel
                    setOnClickListener { action() }
                })
            }
        }
    }

    private fun requestAdmin() {
        val component = ComponentName(this, GuardAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component)
            .putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Makes Phone Guard harder to remove and lets it record wrong unlock attempts.",
            )
        startActivity(intent)
    }

    private fun refresh() {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminActive = dpm.isAdminActive(ComponentName(this, GuardAdminReceiver::class.java))
        val locationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        statusText.text = "Protection: ${onOff(Prefs.isEnabled(this))}\n" +
            "Device admin: ${yesNo(adminActive)}\n" +
            "Location permission: ${yesNo(locationGranted)}"

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val events = EventLog(this).readAll().takeLast(50).reversed()
        logText.text = if (events.isEmpty()) {
            "No events yet."
        } else {
            events.joinToString("\n\n") { e ->
                val place = if (e.latitude != null && e.longitude != null) {
                    "\n  at ${e.latitude}, ${e.longitude}"
                } else {
                    ""
                }
                val detail = if (e.detail.isNotEmpty()) "\n  ${e.detail}" else ""
                "${format.format(Date(e.timeMillis))}  ${e.type.label}\n" +
                    "  screen locked: ${yesNo(e.locked)}$detail$place"
            }
        }
    }

    private fun onOff(value: Boolean) = if (value) "ON" else "OFF"
    private fun yesNo(value: Boolean) = if (value) "yes" else "no"
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
