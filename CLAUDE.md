# Phone Guard

Android (Kotlin) anti-theft app, IMCA final year project. Read `README.md` for the goal and `docs/ROADMAP.md` for the phases. Phase 1 (event recorder) is in place; it has not yet been run on a real device.

## Commands

- Unit tests (JVM only, no device needed): `./gradlew :app:testDebugUnitTest`
- Debug build (needs the Android SDK): `./gradlew :app:assembleDebug`
- Install on a connected phone or emulator: `./gradlew :app:installDebug`

## Git

Make plain, normal commits. No `Co-Authored-By` line, no session link and no "Generated with" line in commit messages or pull request descriptions.

## Code notes

- `EventCodec.kt` and `GuardEvent.kt` use no Android classes on purpose, so their tests run on a normal JVM. Keep it that way.
- Events and settings use device-protected storage so they work before the first unlock after a restart.
- Treat anything about airplane-mode, shutdown or SIM broadcasts as unverified until tested on a real phone.
- Do not claim the app guarantees recovery. The honest goal is evidence, a shorter window for the thief, and help for the owner to act fast.
