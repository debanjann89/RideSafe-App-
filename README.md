# 🏍️ RideSafe Auto Reply

> **Smart Auto-Reply & Live GPS HUD Guard for Motorcycle Rider Safety**

[![Android](https://img.shields.io/badge/Platform-Android-00A82D?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-orange)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📖 Project Overview

**RideSafe Auto Reply** is a background-resilient, permission-secure Android application engineered to solve the critical hazard of distracted driving for motorcycle riders. Splitting focus to reject a ringing call on a handlebar phone mount is a primary cause of road accidents. 

This application establishes absolute safety automation: once the protection is activated, the app runs a specialized telemetry-aware foreground service that programmatically rejects incoming calls, processes whitelists under an asynchronous data layer, and dispatches dynamic SMS notifications containing active telemetry data (speed, status, and emergency location links).

Developed and architected by **Debanjan Amin** (Lead Android Architect).

---

## 🛠️ Core Features & Engineering Highlights

### 1. 📞 Programmatic Call Interception & Suppression
* **State Interception**: Registers a dynamic telephony `BroadcastReceiver` that captures incoming states (`ACTION_PHONE_STATE_CHANGED` / `EXTRA_STATE_RINGING`).
* **Telecom Rejection**: Triggers a programmatical end-call sequence using Android's secure `TelecomManager.endCall()` APIs, silencing distraction within a 5-second safety buffer.
* **Spam Prevention**: Utilizes a secure in-memory cache to throttle duplicate automated replies to the same contact, skipping SMS dispatches for 30 minutes after the initial reply.
* **Dynamic Whitelisting**: Resolves whitelisted contact databases asynchronously before suppressing the call, ensuring urgent calls from designated family members bypass rejection.

### 2. 🧠 AI Motion Guard & Telemetry Engine
* **Doppler Coordinates Speed Tracking**: Polls FusedLocationProviderClient at 1-second intervals. If standard GPS Doppler velocity is lost, the service falls back to sequential geometric distance-over-time calculations:
  $$\text{Speed} = \frac{\text{Distance}(\text{Location}_{\text{curr}}, \text{Location}_{\text{prev}})}{\Delta \text{Time}}$$
* **Vibration EMA Filtering**: In AI Smart Mode, 3-axis accelerometer variance is evaluated over a sliding 50-sample queue. Raw variance is filtered using a low-pass Exponential Moving Average (EMA) to isolate motorcycle engine vibration frequencies, confirming riding states even at slow speeds:
  $$\text{Smoothed Variance} = (\text{Variance}_{\text{raw}} \times 0.15) + (\text{Variance}_{\text{prev}} \times 0.85)$$
* **Telemetry Debounce (Hysteresis)**: Enforces a 5-second transition delay state machine when stopping (e.g., at traffic lights), preventing active protection from flickering off.
* **Audio Intercom Detection**: Queries `AudioManager` to identify Bluetooth helmet profiles (SCO/A2DP), automatically scaling the active speed threshold down by 50% for high-traffic environments.

### 3. 🎨 Premium Glassmorphic Theme System
* **Light Theme (Mint Glow)**: Soft mint-green radial gradient backgrounds (center `#E8F5E9` to corners `#F9F9FB`), white cards with sleek light borders, and deep charcoal high-contrast typography (`#1C1C1E`).
* **Dark Theme (Carbon Black)**: Deep graphite radial gradients (`#070708`), carbon grey cards (`#141416`), glowing neon accents (`#39FF14`), and pure white text.
* **Trigonometric Speedometer Dial**: Drawn directly on a Jetpack Compose Canvas, featuring dynamic ticks, alert-red pointers, and vector sweeps spanning a 270° sweep sector (from 135° to 405°).

### 🧪 4. Laboratory Home Test Mode
* Bypasses location telemetry requirements via a manual switch, allowing developers and users to verify whitelist filters, template variables, and programmatic rejection actions directly from their desk.

---

## 🔒 Android Permissions Model

Due to its interface with secure telephony networks and background location updates, the app enforces a highly compliant runtime security model:

| Permission String | Category | Necessity inside RideSafe Engine |
| :--- | :--- | :--- |
| `android.permission.RECEIVE_SMS`<br>`android.permission.SEND_SMS` | Runtime | Listens for remote whitelisting triggers and dispatches automated SMS template replies. |
| `android.permission.READ_PHONE_STATE`<br>`android.permission.ANSWER_PHONE_CALLS` | Runtime | Listens to telephony ring broadcasts and rejects incoming calls programmatically. |
| `android.permission.ACCESS_FINE_LOCATION`<br>`android.permission.ACCESS_COARSE_LOCATION` | Runtime | Calculates real-time GPS telemetry speed and constructs emergency Google Maps tracking links. |
| `android.permission.ACCESS_BACKGROUND_LOCATION` | Secure Runtime | Maintains active speed monitoring in the background when the device screen is off. |
| `android.permission.FOREGROUND_SERVICE`<br>`FOREGROUND_SERVICE_TYPE_LOCATION` | System | Required on Android 14+ (API 34) to run location-aware telemetry tasks with foreground priorities. |

---

## 🏗️ Architecture & Package Layout

The codebase strictly enforces a Clean MVVM (Model-View-ViewModel) architectural pattern to segregate UI rendering, business telemetry, and local persistence layers:

```
Ridesafe/
├── app/
│   ├── build.gradle.kts           # Gradle app dependencies & build tools configuration
│   └── src/main/
│       ├── AndroidManifest.xml     # Registers secure permissions, services, & boot receivers
│       └── java/com/example/ridesafeautoreply/
│           ├── MainActivity.kt     # Binds dynamic theme preferences into Compose Theme
│           ├── Navigation.kt       # Houses Jetpack Navigation routing graphs
│           ├── data/               # Persistent Models & Preferences DataStore
│           │   ├── Contact.kt      # Serializable Whitelisting contact model
│           │   └── SettingsRepository.kt # Preferences DataStore API interfaces
│           ├── receiver/           # Telephony broadcast & auto-reboot listeners
│           ├── service/            # Telemetry foreground engine (RideSafeService.kt)
│           ├── theme/              # Dynamic Material 3 Mint-Glow & Carbon color assets
│           └── ui/                 # Declarative Presentation Layer
│               ├── common/         # Consolidated PermissionHandler dialog modules
│               ├── home/           # Dashboard telemetry summaries & navigation grids
│               ├── riding/         # Analogue Speedometer Dial HUD (RidingStatusScreen.kt)
│               └── settings/       # Threshold configs, AI filters, and theme toggles
```

---

## 🛠️ Build, Installation & Deployment

### Prerequisites
* **Android Studio** (Koala 2024.1.1 or newer)
* **JDK 17** configured in your IDE environment
* **Android SDK Platform 36** (installed via SDK Manager)

### Step-by-Step Build:
1. **Clone the Repo**:
   ```bash
   git clone https://github.com/debanjann89/RideSafe-App-.git
   cd RideSafe-App-
   ```
2. **Open in Android Studio**:
   * Open the project and allow Gradle to sync the version catalog dependencies configured in `libs.versions.toml`.
3. **Assemble Release APK**:
   * Compile and sign a production-ready APK by running:
     ```bash
     ./gradlew assembleRelease
     ```
   * The signed APK will be output to `/app/build/outputs/apk/release/app-release.apk`.

### Cryptographic Keystore Configuration
Production builds are signed using a dedicated release keystore in the project root:
* **Keystore File**: `ridesafe-release.jks`
* **Store Password / Key Password**: `ridesafe123`
* **Alias ID**: `ridesafe-alias`

---

## 🛡️ Bypassing Google Play Protect Warnings

Because the application requests sensitive permissions (`ANSWER_PHONE_CALLS` and `SEND_SMS`), sideloading the app will trigger Google Play Protect warning dialogs. 

To permanently authorize your signature and remove these warnings globally:
1. Upload your signed release APK (`app-release.apk`) to a secure, public sharing link (e.g. Google Drive, OneDrive).
2. Navigate to the official **[Google Play Protect Appeals Form](https://support.google.com/googleplay/android-developer/contact/protect_appeals)**.
3. Fill out the application metadata:
   * **Package Name**: `com.example.ridesafeautoreply`
   * **Download URL**: Paste your public APK download link.
   * **App Description**: *“This is a private utility application designed to automatically reply to incoming phone calls via SMS for motorcycle safety while riding, distributed directly to family and close friends to prevent distracted riding.”*
4. Submit. Google's security systems will index and whitelist your developer signature. Warnings are typically bypassed globally within **1 to 7 business days**.

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author
* **Debanjan Amin** - *Lead Android Architect / Developer* - [GitHub](https://github.com/debanjann89)
