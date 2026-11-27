# Phone Guard

Final year project (IMCA): an Android app that helps the owner get a stolen phone back, even when the thief turns on airplane mode, powers off, removes the SIM or resets the phone.

**Status: phase 1 (event recorder). Early work, not tested on a device yet.**

## The honest goal

A normal Android app cannot stop a real power-off, survive a factory reset, or track a phone with no working radio. So this project does not promise guaranteed recovery. It aims to:

- shrink the thief's window,
- record as much evidence as possible (what happened, when, where),
- make the phone hard to use,
- help the owner act fast (police report and IMEI block).

## What phase 1 does

- Foreground service that records these events with time, whether the screen was locked, and the last known location:
  airplane mode on/off, shutdown, phone start (before and after first unlock), SIM removed or changed, location turned off, wrong unlock attempts, device admin turned on/off.
- Events are stored in a local log on the phone (last 500 kept).
- Setup checklist in the app: notifications, location (always), battery restrictions, device admin, hiding quick settings on the lock screen (to stop airplane mode being toggled), strong screen lock, Google account lock.
- Restarts itself after the phone boots, if protection was switched on.

Nothing is uploaded yet. See [docs/ROADMAP.md](docs/ROADMAP.md) for the next phases (server upload, heartbeat alerts, fake switch-off, intruder selfie, recovery assistant).

## Build and run

1. Install Android Studio (or the Android SDK and JDK 17).
2. Open this folder in Android Studio and let Gradle sync. If the Gradle wrapper is missing, Android Studio creates it.
3. Run the `app` configuration on a phone running Android 8.0 (API 26) or newer.
4. Open the app, work through the setup checklist, then tap **Start protection**.

Unit tests (the event log encoding, runs on a normal JVM):

```
./gradlew :app:testDebugUnitTest
```

## Permissions and why

| Permission | Why |
|---|---|
| Location (fine, coarse, background) | Save where the phone was when something suspicious happens |
| Foreground service (special use) and notifications | Keep the watcher running and visible |
| Receive boot completed | Restart protection after the phone starts |
| Device admin (watch-login) | Harder to uninstall and see wrong-PIN attempts |

## Privacy

- Events stay on the phone in this phase. Location is only the last location the phone already knew.
- The log is kept in device-protected storage so events can be saved before the first unlock after a restart. That storage is less protected than normal app storage, so the log should not hold anything more sensitive than event types, times and coordinates.
- Use this app only on your own phone.

## Known limits of phase 1

- The Stop button has no PIN or biometric check yet.
- SIM monitoring handles the simple case (one SIM state), not dual-SIM details.
- Whether the airplane-mode broadcast arrives while the phone is locked, and whether the shutdown broadcast arrives at all, still need testing on real phones. These are the first things to check.
- The ColorOS setting "Allow Quick Settings on Lock screen" can only be changed by the user; the app guides them to settings.

## Research

The research behind this project (thief behaviour, competing apps, Android limits, pros and cons) is in the Word document "Anti-Theft App - Research Notes" kept with the project files.
