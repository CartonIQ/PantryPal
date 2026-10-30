# PantryPal 1.1

Package `com.cartoniq.pantrypal`. Compose + Room (KSP) + WorkManager + AdMob (test IDs).

## Build (Termux ARM)
1. `pkg install openjdk-17 gradle aapt2`
2. `bash setup-wrapper.sh` (creates gradlew + wrapper jar; run once. Do NOT run `gradle wrapper` inside the project: your global Gradle 9.x cannot load AGP 8.7.)
3. `gradle.properties` already points `android.aapt2FromMavenOverride` at Termux's aapt2 (remove that line on a desktop).
4. Set `sdk.dir=/path/to/android-sdk` in `local.properties` (platform 34 + build-tools).
5. `bash build.sh` (runs unit tests, then assembleDebug).

See `CHANGES.md` for what changed in 1.1. Unit tests: `bash build.sh` runs them before assembling.
