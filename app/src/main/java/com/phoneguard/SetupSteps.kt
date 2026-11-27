package com.phoneguard

import android.Manifest
import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/** DONE and TODO are checked by the app. MANUAL means the app cannot check it; the owner has to. */
enum class StepState { DONE, TODO, MANUAL }

class SetupStep(
    val title: String,
    val description: String,
    val state: StepState,
    val buttonLabel: String? = null,
    val action: ((Activity) -> Unit)? = null,
)

/** The setup checklist, with the live state of each step. */
object SetupSteps {

    const val REQUEST_NOTIFICATIONS = 1
    const val REQUEST_LOCATION = 2

    fun all(context: Context): List<SetupStep> = listOf(
        SetupStep(
            "Notifications",
            "Lets the protection service show its status notification. Needed on Android 13 and newer.",
            check(Build.VERSION.SDK_INT < 33 || granted(context, Manifest.permission.POST_NOTIFICATIONS)),
            "Allow notifications",
        ) { activity ->
            if (Build.VERSION.SDK_INT >= 33) {
                ActivityCompat.requestPermissions(
                    activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS,
                )
            }
        },
        SetupStep(
            "Location",
            "Saves where the phone was when something suspicious happens.",
            check(granted(context, Manifest.permission.ACCESS_FINE_LOCATION)),
            "Allow location",
        ) { activity ->
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION,
            )
        },
        SetupStep(
            "Location all the time",
            "In this app's settings, open Permissions, then Location, and choose 'Allow all the time' " +
                "so location still works in the background.",
            check(granted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)),
            "Open app settings",
        ) { activity ->
            activity.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${activity.packageName}")),
            )
        },
        SetupStep(
            "No battery limits",
            "Remove battery restrictions for Phone Guard so the phone maker does not stop the service.",
            check(ignoringBatteryOptimizations(context)),
            "Open battery settings",
        ) { activity ->
            activity.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        },
        SetupStep(
            "Device admin",
            "Makes the app harder to uninstall and lets it see wrong unlock attempts.",
            check(adminActive(context)),
            "Activate device admin",
        ) { activity -> requestAdmin(activity) },
        SetupStep(
            "Strong screen lock",
            "Use a long PIN, a password or biometrics. A weak or watched PIN lets a thief change settings.",
            check(deviceSecure(context)),
            "Open security settings",
        ) { activity -> activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) },
        SetupStep(
            "Quick settings on the lock screen",
            "Stops a thief turning on airplane mode from the lock screen. On ColorOS turn OFF " +
                "'Allow Quick Settings on Lock screen'. On other phones search settings for 'lock screen'.",
            StepState.MANUAL,
            "Open settings",
        ) { activity -> activity.startActivity(Intent(Settings.ACTION_SETTINGS)) },
        SetupStep(
            "Google account and Find Hub",
            "Keep your Google account signed in and Find Hub switched on. " +
                "This keeps Google's factory reset protection working.",
            StepState.MANUAL,
        ),
    )

    /** Pair of (steps done, steps the app can check). Manual steps are not counted. */
    fun progress(context: Context): Pair<Int, Int> {
        val checkable = all(context).filter { it.state != StepState.MANUAL }
        return checkable.count { it.state == StepState.DONE } to checkable.size
    }

    fun adminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isAdminActive(ComponentName(context, GuardAdminReceiver::class.java))
    }

    private fun requestAdmin(activity: Activity) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(activity, GuardAdminReceiver::class.java))
            .putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Makes Phone Guard harder to remove and lets it record wrong unlock attempts.",
            )
        activity.startActivity(intent)
    }

    private fun check(done: Boolean) = if (done) StepState.DONE else StepState.TODO

    private fun granted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun ignoringBatteryOptimizations(context: Context): Boolean {
        val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return power.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun deviceSecure(context: Context): Boolean {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguard.isDeviceSecure
    }
}
