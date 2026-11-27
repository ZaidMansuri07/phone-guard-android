package com.phoneguard

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Device admin makes the app harder to uninstall and lets it see wrong-PIN attempts.
 * Only the "watch-login" policy is requested (see res/xml/device_admin.xml).
 */
class GuardAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        EventRecorder.record(context, EventType.ADMIN_ENABLED)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        EventRecorder.record(context, EventType.ADMIN_DISABLED)
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        EventRecorder.record(context, EventType.WRONG_PIN)
    }
}
