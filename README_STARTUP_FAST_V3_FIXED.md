LifeAlarm STARTUP_FAST_V3_FIXED

Based on LifeAlarm_STARTUP_FAST_V3.

Fixes:
- MainActivity used snapshotFlow through the wrong package.
- Added Compose snapshotFlow import/use and Flow collect/drop imports.
- No calculation/alarm logic was changed by this fix.
- Existing startup optimization, alarm engine, live location, Prahar and Lagna logic are preserved.

Build root:
settings.gradle.kts
build.gradle.kts
app/
.github/
