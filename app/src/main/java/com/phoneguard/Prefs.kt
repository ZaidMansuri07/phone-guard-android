package com.phoneguard

import android.content.Context

/** Small settings store. Uses device protected storage so the boot receiver can read it before unlock. */
object Prefs {
    private const val FILE = "guard_prefs"
    private const val KEY_ENABLED = "protection_enabled"

    private fun prefs(context: Context) = context.applicationContext
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }
}
