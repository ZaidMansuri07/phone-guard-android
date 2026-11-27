package com.phoneguard

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.MaterialToolbar

/**
 * Shared screen setup: draws behind the system bars (edge to edge), then pads the app bar and the
 * scrolling content so nothing ends up hidden under the status bar or the navigation bar.
 * Every screen layout has views with the ids appBar, toolbar and content.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    protected fun setUpScreen(title: String?, showBack: Boolean) {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(showBack)
            if (title == null) setDisplayShowTitleEnabled(false) else this.title = title
        }
        applyInsets(findViewById(R.id.appBar), findViewById(R.id.content))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun applyInsets(appBar: View, content: View) {
        val baseBottom = content.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { view, insets ->
            val bars = insets.getInsets(BARS)
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val bars = insets.getInsets(BARS)
            view.updatePadding(left = bars.left, right = bars.right, bottom = baseBottom + bars.bottom)
            insets
        }
    }

    private companion object {
        val BARS = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
    }
}
