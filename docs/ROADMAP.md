# Roadmap

Each phase should leave something that works and can be shown at a review.

## Phase 1: event recorder (this commit)

- [x] Foreground service that records airplane mode, shutdown, boot, SIM state, location off
- [x] Device admin receiver for wrong-PIN attempts and admin removal
- [x] Local event log and a simple screen to view it
- [x] Setup checklist (permissions, battery, admin, lock-screen quick settings, strong lock)
- [ ] Test every trigger on a real phone and write down the results (these results go into the report)

## Phase 2: server and heartbeat

- [ ] Backend that receives events and a regular heartbeat from the phone
- [ ] Upload queue on the phone: send events when any connection appears
- [ ] Gap alert: if the heartbeat stops with no "expected off" flag from the owner, alert the owner and trusted contacts with the last known location and trail
- [ ] Owner web dashboard: timeline and map

## Phase 3: fake switch-off

- [ ] Accessibility Service that notices the power menu and the Power off tap (test whether the phone maker's menu can be read)
- [ ] Fake shutdown screen and black screen, with a secret wake sequence
- [ ] Silence ringtone, vibration and notification sounds while in fake-off mode, and quietly reject calls
- [ ] Heartbeat keeps running in fake-off mode

## Phase 4: evidence

- [ ] Intruder selfie and location on repeated wrong unlock attempts
- [ ] Record the new SIM's identifiers when a SIM is inserted, and alert the owner

## Phase 5: commands and recovery help

- [ ] Remote commands (lock, ring, locate) over push, SMS and Wi-Fi, queued until the phone reappears
- [ ] Lost-mode message on the lock screen with a safe contact method
- [ ] Recovery assistant: saved IMEI and purchase proof, guided police report and IMEI block steps

## Things to verify early

- Does the airplane-mode broadcast reach the app while the phone is locked, and how quickly?
- Is the shutdown broadcast delivered on current Android versions?
- What can the app do after a forced restart before the first unlock?
- Can the app read the "quick settings on lock screen" setting on any phone?
- Current Device Admin limits, SMS permission policy and accessibility restrictions for the target Android version.
