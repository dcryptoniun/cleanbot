# <img src="docs/logo/icon.svg" alt="cLeanBot Logo" width="64" height="64" align="middle"> cLeanBot

Debloat, speed up, and wirelessly manage your Android TV, Google TV, and streaming devices directly from your phone. Zero root, no PC required.

<p>
  <img src="https://img.shields.io/badge/License-GPL--3.0-00696B" alt="License: GPL-3.0">
  <img src="https://img.shields.io/badge/Android-8.0%2B-00696B" alt="Android 8.0+">
  <img src="https://img.shields.io/badge/Root-Not%20Needed-00696B" alt="No Root Needed">
  <img src="https://img.shields.io/badge/Privacy-100%25%20Local-00696B" alt="100% Local / No Tracking">
  <img src="https://img.shields.io/badge/Status-Active-00696B" alt="Status: Active">
</p>

---

## Overview

Modern smart TVs and streaming sticks often come bogged down by pre-installed OEM bloatware, sluggish background telemetry, sponsored recommendation carousels, and constrained storage. 

**cLeanBot** is an open-source, local-first utility built by **Ger Studio** to help you reclaim your television's speed, storage, and privacy. Operating entirely over your local Wi-Fi network using Android's native Wireless Debugging (ADB) protocol, cLeanBot allows you to safely freeze or disable system clutter, sideload apps, push files, and control your device with an intuitive virtual remote—all from your Android phone.

---

## ✨ Key Features

- **⚡ Smart 1-Click Debloat**  
  Intelligently matches your TV against 330+ curated debloat lists. Safely disable sponsored launchers, OEM telemetry, and idle background services. Preview and customize every single package before applying any change.
- **📦 App Manager & Package Inspector**  
  View all installed system and user packages, inspect app status, freeze apps you don't currently need, clear caches to free storage, and uninstall user-installed apps with a tap.
- **📂 Wireless File Transfer & Sideloading**  
  Wirelessly transfer APKs, videos, photos, and files straight from your phone's storage to your TV's filesystem at maximum Wi-Fi speeds. Sideload APKs or `.xapk` / `.apks` / `.apkm` bundles effortlessly.
- **🎮 Virtual Trackpad & Remote**  
  Smooth, low-latency TV control featuring a directional D-Pad, playback controls, volume rockers, keyboard text input, and an interactive virtual mouse trackpad.
- **🚀 Home Screen Freedom**  
  Easily set up clean, ad-free alternative launchers (such as Projectivy or FLauncher) and switch back whenever you wish without breaking your TV's native settings.
- **🔒 100% Private & Local**  
  Zero tracking, zero analytics, and zero cloud logins. All ADB commands and file transfers remain strictly confined to your local home network.

---

## 🛡️ Safety & Reliability Principles

Debloating a television should never risk turning it into a brick. cLeanBot strictly adheres to safety-first engineering:

1. **Reversible Disabling (`disable-user`)**: System apps are never destructively wiped via `pm uninstall --user 0`. Instead, they are turned off with `pm disable-user`, meaning any change can be safely restored at any time.
2. **State Recording & 1-Tap Undo**: cLeanBot records what was originally active before making any modifications. You can undo any batch action or restore your TV with one tap.
3. **Hard Protected List**: Core system packages essential for booting, Wi-Fi connectivity, system updates, display outputs, and setup wizards are strictly locked and can never be disabled.
4. **Full Transparency**: Every action shows you the exact package names and asks for confirmation before anything executes on your TV.

---

## 📺 Supported Devices

Compatible with devices running Android 8.0 (Oreo) or newer:
- **Google TV**: Chromecast with Google TV, Sony Bravia, TCL, Philips, Hisense.
- **Android TV**: Xiaomi Mi Box / TV Stick, Nvidia Shield TV, Mecool, Dynalink.
- **Fire OS**: Amazon Fire TV Cube and Fire TV Stick devices (via Wireless ADB).

---

## 🛠️ Quick Setup (How to Connect)

1. Make sure your phone and your TV are connected to the **same Wi-Fi network**.
2. **Enable Developer Options on your TV**:
   - Go to TV **Settings** → **Device Preferences** (or **System**) → **About**.
   - Scroll down to **Android TV OS Build** (or device name on Fire OS) and press the **OK / Select** button on your remote 7 times until you see the message *"You are now a developer!"*.
3. **Turn on Wireless Debugging**:
   - In TV **Settings** → **Developer Options**, enable **USB Debugging** and **Wireless Debugging** (or **Network Debugging**).
4. **Connect in cLeanBot**:
   - Open cLeanBot on your phone.
   - Tap your detected TV (or tap **Add TV** and enter its local IP address).
   - Enter the pairing code or confirm the authorization prompt shown on your TV screen.

---

## 💻 Building from Source

cLeanBot is built using modern Android architecture, Kotlin, Jetpack Compose, and Material 3 Expressive design.

### Prerequisites
- JDK 17 or higher
- Android SDK with Platform 36/37 installed
- Gradle 9.x (bundled wrapper included)

### Project Modules
- `:app` — Phone application (Jetpack Compose UI, state management, TV sync).
- `:adb` — Pure Kotlin ADB library for Wireless Debugging, shell execution, and file transfer.
- `app/src/helper` — Lightweight on-TV companion helper.

### Build Commands
```sh
# Clone the repository
git clone https://github.com/dcryptoniun/cleanbot.git
cd cleanbot

# Compile and run unit tests
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew :app:assembleDebug

# Assemble Signed Release Bundle (.aab)
./gradlew :app:bundleRelease
```

---

## 🤝 Contributing

Contributions, bug reports, and device compatibility lists are warmly welcomed! Please check out [CONTRIBUTING.md](CONTRIBUTING.md) for architecture guidelines, code style, and PR processes.

---

## 📜 License & Acknowledgments

- **License**: Released under the [GNU General Public License v3.0](LICENSE).
- **Credits & Upstream**: Built upon foundations from the open-source community, including [Tellyscope](https://github.com/man15h/tellyscope), [Kadb](https://github.com/flyfishxu/Kadb), and [cleanyourtv](https://github.com/Gathin23/cleanyourtv). Full third-party notices and licenses are documented in [NOTICE](NOTICE).

*cLeanBot is maintained by [Ger Studio](https://github.com/dcryptoniun).*
