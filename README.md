# SmartMed – Smart Medicine Box

Medication reminder app that works with a connected (Bluetooth) medicine box.
Built with **Kotlin Multiplatform** and **Compose Multiplatform**: the screens, data and
logic are shared between Android and iPhone.

## Features

- **Accounts** – sign up with email, username and a strong password
  (8+ characters, uppercase, lowercase, digit, symbol, with a live checklist); log in with
  email or username; stays logged in. Accounts are stored on the phone; passwords are
  hashed with PBKDF2-HMAC-SHA256 and a random salt, never stored in clear.
- **Medicines** – add / delete medicines with a daily reminder time.
- **Reminders** – notifications at the planned time, re-scheduled after a phone restart,
  an app update or a reinstall.
- **History** – every dose confirmed as taken, newest first (kept even if the medicine is deleted).
- **Smart box** – connect to the medicine box over Bluetooth and send a test reminder.
- **Data kept** – local Room database, backed up and restored by Android Auto Backup
  (e.g. when moving to a new phone).

## Project structure

```
shared/        Shared code (Android + iPhone)
  commonMain/    data (Room entities, DAOs, database), domain (repositories, rules,
                 password hashing), ui (Compose screens, view models, theme)
  iosMain/       iPhone-specific code (database location)
  schemas/       Exported Room database schemas (v1, v2)
androidApp/    Android app: activity, alarms/notifications, Bluetooth, database builder
```

## Build & run (Android)

Requirements: JDK 25 and the Android SDK (platform 37).

```bash
./gradlew :androidApp:assembleDebug          # build the APK
./gradlew :androidApp:testDebugUnitTest      # unit tests (JVM)
./gradlew :androidApp:connectedDebugAndroidTest   # device / emulator tests
```

Or open the project in Android Studio and run the `androidApp` configuration.

## iPhone

The shared module already targets iOS (`iosArm64`, `iosSimulatorArm64`).
The Xcode app (`iosApp/`) and the iOS versions of Bluetooth and notifications are the
next step and need a Mac with Xcode.
