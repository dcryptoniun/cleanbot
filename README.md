# <img src="docs/logo/icon.png" alt="cLeanBot Logo" width="64" height="64" align="middle"> cLeanBot

Debloat, speed up, customize, and wirelessly manage your Android TV, Google TV, and streaming devices directly from your phone. Zero root, no PC required.

<p>
  <a href="https://play.google.com/store/apps/details?id=com.gerstudio.cleanbot">
    <img src="https://img.shields.io/badge/Google_Play-Get_it_on_Play_Store-00696B?logo=googleplay&logoColor=white" alt="Google Play">
  </a>
  <img src="https://img.shields.io/badge/License-GPL--3.0-00696B" alt="License: GPL-3.0">
  <img src="https://img.shields.io/badge/Android-8.0%2B-00696B" alt="Android 8.0+">
  <img src="https://img.shields.io/badge/Root-Not%20Needed-00696B" alt="No Root Needed">
  <img src="https://img.shields.io/badge/Privacy-100%25%20Local-00696B" alt="100% Local / No Tracking">
  <img src="https://img.shields.io/badge/Version-1.2.0-00696B" alt="Version 1.2.0">
  <img src="https://img.shields.io/badge/Status-Active-00696B" alt="Status: Active">
</p>

---

## 📥 Download

Get **cLeanBot** on Google Play:

<a href="https://play.google.com/store/apps/details?id=com.gerstudio.cleanbot">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="60">
</a>

Direct link: [https://play.google.com/store/apps/details?id=com.gerstudio.cleanbot](https://play.google.com/store/apps/details?id=com.gerstudio.cleanbot)

---

## Overview

Modern smart TVs and streaming sticks often come bogged down by pre-installed OEM bloatware, sluggish background telemetry, sponsored recommendation carousels, and constrained storage. 

**cLeanBot** is an open-source, local-first utility built by **Ger Studio** to help you reclaim your television's speed, storage, privacy, and visual aesthetics. Operating entirely over your local Wi-Fi network using Android's native Wireless Debugging (ADB) protocol, cLeanBot allows you to safely freeze or disable system clutter, deploy living room TV themes, sideload apps, push files, and control your device with an intuitive virtual remote—all from your Android phone.

---

## ✨ Key Features

- **⚡ CleanYourTV Curated Debloat Presets & 1-Click Cleaner**  
  One-tap debloat profiles inspired by CleanYourTV (Safe Clean, Ultra Clean Home, Fire TV Unchained, Telemetry & Privacy Shield). Intelligently matches against 330+ curated device lists to safely freeze or disable OEM bloatware with 100% reversible undo.
- **🎨 TV Themes Studio & Wireless Deployer**  
  Browse 9 curated living room TV themes (Aurora Borealis, OLED Pure Black, Cyberpunk Neon, Nordic Frost, Sunset Ember, Tokyo Night, Forest Emerald, Retro Synthwave, Clean Minimalist). Live 16:9 TV mockup preview with customizer for dock styles, corner radii, and ambient clocks, and wirelessly push `config.json` themes directly over ADB.
- **🚀 Launchers Showcase & Hub**  
  Discover, configure, and switch ad-free alternative launchers (CleanYourTV Launcher, Projectivy, FLauncher, Dispatch, Wolf Launcher) with 1-tap default home switching and accessibility button remapping.
- **🛠️ Living Room TV Toolkit**  
  Wireless Sleep Timer with live countdown, Audio-Only screen standby, TV resolution & display density (DPI) tuner, ambient screensaver trigger, cache purging, and ADB port securer.
- **📦 App Manager & Package Inspector**  
  View all installed system and user packages, inspect app status, freeze apps you don't currently need, clear caches to free storage, and uninstall user-installed apps with a tap.
- **📂 Wireless File Transfer & Sideloading**  
  Wirelessly transfer APKs, videos, photos, and files straight from your phone's storage to your TV's filesystem at maximum Wi-Fi speeds. Sideload APKs or `.xapk` / `.apks` / `.apkm` bundles effortlessly.
- **🎮 Virtual Trackpad & Remote**  
  Smooth, low-latency TV control featuring a directional D-Pad, playback controls, volume rockers, keyboard text input, and an interactive virtual mouse trackpad.
- **🔒 100% Private & Local**  
  Zero tracking, zero analytics, zero external cloud dependencies, and zero non-SDK reflection/dadb. 100% Google Play Store compliant.

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

## 💖 Support the Project

cLeanBot is 100% free, open-source, and contains zero advertisements or trackers. If cLeanBot helped declutter or speed up your TV, consider supporting our ongoing development!

- ☕ **Buy Me a Coffee**: [buymeacoffee.com/mayankmeena](https://buymeacoffee.com/mayankmeena)
- 💖 **GitHub Sponsors**: [github.com/sponsors/mayankmeena](https://github.com/sponsors/mayankmeena)
- 📲 **More Apps on Google Play**: [Ger Studio Developer Profile](https://play.google.com/store/apps/dev?id=8895908145914606687)
- 🌐 **Official Website**: [gerstudio.com](https://www.gerstudio.com/)
- ⭐ **Star on GitHub**: [github.com/dcryptoniun/cleanbot](https://github.com/dcryptoniun/cleanbot)

You can also send a tip directly inside the app under the **Support** screen using Google Play Billing!

---

## 📜 License & Acknowledgments

cLeanBot is released under the [GNU General Public License v3.0](LICENSE).

### 🙏 Credits & Inspirations

cLeanBot stands on the shoulders of giants across the Android TV, open-source customization, and debloating communities. We owe immense gratitude to the following projects, creators, and maintainers:

- **[CleanYourTV](https://github.com/Gathin23/cleanyourtv)** (by **[Gathin](https://github.com/Gathin23)** & [cleanyour.tv](https://cleanyour.tv/app))  
  Special heartfelt thanks to Gathin for creating CleanYourTV and the inspirational [SHOWCASE.md](https://github.com/Gathin23/cleanyourtv/blob/main/SHOWCASE.md). CleanYourTV's vision for clean, ad-free living room TV experiences directly inspired cLeanBot's **TV Themes Studio**, live 16:9 TV chassis mockup, dock & clock customizers, wireless `config.json` deployment engine, and curated debloat profiles (Safe Clean, Ultra Clean Home, Fire TV Unchained, and Telemetry Shield).
- **[Tellyscope](https://github.com/man15h/tellyscope)** (by **[Manish](https://github.com/man15h)** and contributors)  
  The foundational phone-to-TV companion app architecture, device discovery, remote control interface, and initial debloat safety framework.
- **[Kadb](https://github.com/flyfishxu/Kadb)** & **[Dadb](https://github.com/mobile-dev-inc/dadb)** (by **[Flyfish233](https://github.com/flyfishxu)** & **Mobile-dev Inc**)  
  Pure-Kotlin Android Debug Bridge (ADB) client protocol implementation enabling zero-root, PC-free wireless pairing and shell execution.
- **[spake2-java](https://github.com/Flyfish233/spake2-java)** (by **[Flyfish233](https://github.com/Flyfish233)**)  
  SPAKE2 password-authenticated key exchange used for Android 11+ Wireless Debugging pairing over TLS.
- **TV Launchers Community**:
  - **[Projectivy Launcher](https://github.com/spocky/projengmenu)** (by **[Spocky](https://github.com/spocky)**) — For pioneering customizable, premium Android TV launcher experiences and accessibility overrides.
  - **[FLauncher](https://gitlab.com/etiennep/flauncher)** (by **[Étienne Fesser](https://gitlab.com/etiennep)**) — An outstanding open-source Flutter TV launcher showcasing clean UI simplicity.
  - **[Dispatch](https://dispatchlauncher.com)** (by **[Spaul Dhaliwal](https://dispatchlauncher.com)**) — For inspiring media-first living room top-shelf presentation.
  - **Wolf Launcher** & **Primal Launcher** — Community favorites for reclaiming Fire TV and Android TV home screens.

Full third-party licenses, notices, and copyright terms are preserved in [NOTICE](NOTICE).

*cLeanBot is maintained by [Ger Studio](https://github.com/dcryptoniun).*
