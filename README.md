# Chandra Panchang Alarm — Prototype V1

Android/Kotlin prototype for a location-aware Chandra Rashi / Nakshatra / Pada alarm.

## Current prototype
- Marathi UI
- Daund validation reference
- Rashi/Nakshatra/Pada data model
- Location permission request
- Notification permission request
- Exact test alarm
- Exact-alarm permission handling
- Alarm BroadcastReceiver

## Important
The astronomical engine is intentionally NOT hard-coded to 17-Aug-2026 4:19 PM.
The next development step is to integrate a properly licensed astronomical ephemeris
(Swiss Ephemeris or an equivalent implementation), apply Lahiri/Chitrapaksha sidereal
calculation, and numerically solve the exact Rashi/Nakshatra/Pada boundary times.

Validation target supplied for this project:
Daund, Maharashtra — 17-Aug-2026 — Cancer -> Leo — 4:19 PM.

## Open in Android Studio
Open this folder as a Gradle project.
