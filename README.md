# ROUTINE

[![Android CI](https://github.com/nahidreza45/my_routine/actions/workflows/android.yml/badge.svg)](https://github.com/nahidreza45/my_routine/actions/workflows/android.yml)

An offline-first Android timetable, academic week tracker, and Class Test (CT) marks manager built with **Kotlin** and **Jetpack Compose**, tailored around a Saturday-Wednesday university schedule.

> **Latest Release:** [`v2.5`](https://github.com/nahidreza45/my_routine/releases) (`versionCode = 6`) | **Min SDK:** Android 7.0 (`API 24`) | **Target SDK:** `API 36`

---

## Key Features

- **Day & Week Schedule Views**
  - **Day Mode:** Interactive day pager with elastic rubber-band overscroll physics, live `NOW` aura + remaining time indicator, smart `UP NEXT` detection, and automatic cold-launch focus on the next active class day once today's classes finish.
  - **Week Mode:** Full 5-day (`SAT-WED`) time grid covering 9 periods (`8:00 AM` to `5:00 PM`) with dedicated break bands (`Tea Break` & `Lunch & Prayer Break`) and one-tap empty-slot scheduling.
- **Academic Week & Parity Engine**
  - Automatic `Saturday-Wednesday` academic week numbering with `Thursday-Friday` weekend snapping.
  - **Holiday Pause Engine:** Multi-week holiday intervals merge cleanly so academic week numbers pause without double-counting overlaps.
  - **Odd/Even Lab Shifting:** Shift alternating lab cycles forward by one week while preserving historical weeks cleanly.
- **Course & CT Marks Tracker**
  - Subject color coding, automatic `+1` course code resolution for theory-linked labs (e.g., `ECE 3117` -> `ECE 3118`), and standalone even-coded labs.
  - **Best 3 of 4 CT Calculator:** Highlights the top 3 scores out of 20 and computes the ceiling average automatically.
- **Exact Pre-Class Notifications**
  - Configurable reminder lead time via `AlarmManager` with quick **Cancel Today** notification actions and automatic reschedule across device reboots and timezone changes.
- **Zero-Account Persistence & Export**
  - Stored locally with Jetpack Preferences DataStore + `kotlinx.serialization` JSON (`coerceInputValues` & `ignoreUnknownKeys` for resilient upgrades).
  - Supports both **Schedule-Only** sharing (keeps receivers' CT marks intact) and **Full Personal Backup** with auto-detecting dynamic JSON import, plus high-DPI Day/Week PNG export and share.

---

## Architecture & Tech Stack

| Layer | Technology |
|---|---|
| **UI** | Jetpack Compose (Material 3, Compose BOM `2026.03.01`), Edge-to-Edge |
| **State** | MVVM (`AndroidViewModel` + `StateFlow` / `SharedFlow`) |
| **Scheduling Engine** | Pure Kotlin domain engine (`RoutineScheduleEngine`, `RoutineDate`) |
| **Persistence** | Jetpack DataStore (`Preferences`) + `kotlinx.serialization` JSON |
| **Notifications** | `AlarmManager` exact alarms + `BroadcastReceiver` + Notification Channel |
| **Build & Optimization** | Gradle Kotlin DSL (`AGP 9.0.1`), Kotlin `2.3.20`, R8 minification & resource shrinking |

### Project Structure

```text
app/src/main/java/com/nahidreza/routine/
|-- MainActivity.kt                  # Edge-to-edge host, animated route transitions, notification intent handler
|-- model/
|   |-- RoutineDate.kt               # API 24+ Gregorian/UTC calendar math & SAT-WED week indexing
|   +-- RoutineModels.kt             # Serializable state models, color palette, slots, CT average math
|-- data/
|   |-- RoutineRepository.kt         # DataStore persistence & portable JSON backup serialization
|   +-- RoutineScheduleEngine.kt     # Week parity, holiday merging, UP NEXT lookup, slot conflict checks
|-- notification/
|   |-- AlarmScheduler.kt            # Exact upcoming alarm sync for active working days
|   |-- ClassAlarmReceiver.kt        # Alert firing & one-tap class cancellation handler
|   |-- BootCompletedReceiver.kt     # Reboot & timezone-change alarm recovery
|   +-- NotificationHelper.kt        # Styled notification builder & channel management
|-- ui/
|   |-- RoutineViewModel.kt          # App state orchestration, cold-launch UP NEXT focus, CRUD actions
|   |-- MainScheduleScreen.kt        # Day & Week pagers, live card animations, elastic overscroll, sheets
|   |-- AddEditClassScreen.kt        # Slot picker, parity conflict validation, subject defaults
|   |-- CtTrackerScreen.kt           # Best-3-of-4 CT marks tracker
|   |-- SettingsScreens.kt           # Layout defaults, subjects/teachers, holidays, JSON backup/reset
|   |-- MenuDrawerContent.kt         # Navigation drawer, quick PNG export/share, academic week controls
|   +-- RoutineTheme.kt              # Dark surface palette, typography, spring/elastic modifiers
+-- util/
    +-- RoutineImageExporter.kt      # Canvas-rendered Day/Week timetable PNG generator & MediaStore/FileProvider share
```

---

## Getting Started

### Download APK

Grab the latest prebuilt `ROUTINE.apk` directly from [GitHub Releases](https://github.com/nahidreza45/my_routine/releases).

### Build from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/nahidreza45/my_routine.git
   cd my_routine
   ```
2. Open in **Android Studio** (JDK 17, Android SDK 36) or build via Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```

### Releasing / Upgrading Without Losing Data

- Published APKs preserve `applicationId = "com.nahidreza.routine"` and DataStore key `app_database_json_v2`.
- Keep signing configuration consistent across releases (or move production releases to a dedicated git-ignored `keystore.properties` / `.jks` file before first public Play Store release) so users can install newer APKs directly over older versions without uninstalling.
- Prebuilt release APKs should be attached as **GitHub Release** assets (`v2.2`, etc.) rather than committed directly into git history.

---

## License

Licensed under the [MIT License](LICENSE). Built by **Nahid Reza** (ECE '23, RUET).
