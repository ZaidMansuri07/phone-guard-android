package com.phoneguard

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

/** About: version, what the app does, privacy and honest limits. */
class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setUpScreen(title = "About", showBack = true)

        val version = packageManager.getPackageInfo(packageName, 0).versionName
        findViewById<TextView>(R.id.version).text = "Version $version"

        val sections = findViewById<LinearLayout>(R.id.sections)
        section(
            sections,
            "What it does",
            "Phone Guard records signs that your phone may have been stolen, such as airplane mode " +
                "being turned on, the phone shutting down, the SIM being removed or location being " +
                "switched off. Each event is saved with the time, whether the screen was locked and the " +
                "last known location.",
        )
        section(
            sections,
            "Privacy",
            "Everything stays on this phone. Nothing is uploaded. The log is kept in storage that works " +
                "before the first unlock after a restart, so it only holds event types, times and coordinates.",
        )
        section(
            sections,
            "Honest limits",
            "No app can stop a real power-off, survive a factory reset or track a phone with no working " +
                "radio. Phone Guard does not promise recovery. It aims to collect evidence, make the phone " +
                "harder to use and help you act fast: report to the police and block the IMEI.",
        )
        section(sections, "Use", "Use Phone Guard only on your own phone.")
    }

    private fun section(parent: LinearLayout, title: String, body: String) {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(12) }
        }
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(TextView(this).apply {
            text = title
            setTextAppearance(R.style.TextAppearance_PhoneGuard_Label)
        })
        column.addView(TextView(this).apply {
            text = body
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            setLineSpacing(0f, 1.2f)
            setPadding(0, dp(8), 0, 0)
        })
        card.addView(column)
        parent.addView(card)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
