# RideSafe Auto Reply

An automated, background-resilient Android safety app that programmatically intercepts and rejects phone calls while riding a motorcycle, sending custom telemetry-enriched SMS replies. Built under a clean MVVM architecture with Jetpack Compose by Debanjan Amin.

## 🚀 Key Features
* **Programmatic Call suppression**: Rejects distracting incoming calls programmatically using Android's secure `TelecomManager` APIs.
* **AI Motion Guard**: Accurately detects motorcycle riding and engine vibration profiles using high-precision low-pass Exponential Moving Average (EMA) accelerometer filters and GPS Doppler fallback calculations.
* **Smart Hysteresis Handoff**: Keeps protection active during brief stops at red lights using a 5-second stationary delay state machine.
* **Bluetooth Intercom Integration**: Dynamically scales speed thresholds down by 50% when connected to Bluetooth helmet intercoms for safer city riding.
* **Sleek Themes**: Seamlessly toggles between premium Carbon-Black and Mint-Glow Light visual styles, featuring a dynamic retro analog dial Canvas speedometer.
* **Developer Home Test Mode**: Simulates full call interception pipeline from a desk without requiring active riding.
