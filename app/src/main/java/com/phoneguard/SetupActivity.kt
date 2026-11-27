package com.phoneguard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton

/** The setup checklist. Each step shows whether it is done and opens the right system screen. */
class SetupActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setUpScreen(title = "Setup", showBack = true)
        findViewById<TextView>(R.id.intro).text =
            "Work through these steps once. Phone Guard checks most of them for you; " +
                "the last two you need to check yourself."
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        render()
    }

    private fun render() {
        val list = findViewById<LinearLayout>(R.id.list)
        list.removeAllViews()
        val inflater = LayoutInflater.from(this)
        SetupSteps.all(this).forEachIndexed { index, step ->
            val card = inflater.inflate(R.layout.item_step, list, false)
            card.findViewById<TextView>(R.id.number).text = String.format("Step %02d", index + 1)
            card.findViewById<TextView>(R.id.title).text = step.title
            card.findViewById<TextView>(R.id.description).text = step.description

            val pill = card.findViewById<TextView>(R.id.state)
            when (step.state) {
                StepState.DONE -> Ui.pill(pill, "Done", R.color.state_on_bg, R.color.state_on_text)
                StepState.TODO -> Ui.pill(pill, "To do", R.color.state_off_bg, R.color.state_off_text)
                StepState.MANUAL -> Ui.pill(pill, "Check yourself", R.color.state_manual_bg, R.color.state_manual_text)
            }

            val button = card.findViewById<MaterialButton>(R.id.action)
            val action = step.action
            if (action == null || step.buttonLabel == null || step.state == StepState.DONE) {
                button.visibility = View.GONE
            } else {
                button.text = step.buttonLabel
                button.setOnClickListener { action(this) }
            }
            list.addView(card)
        }
    }
}
