# VIREX Wallpapers – Pure Black AMOLED

<p align="center">
  <img src="assets/logo.png" width="120" alt="VIREX Logo">
</p>

<p align="center">
  <strong>Premium Android wallpaper app with TRUE BLACK AMOLED wallpapers</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-26%2B-green.svg" alt="Min SDK 26">
  <img src="https://img.shields.io/badge/Kotlin-2.1.0-blueviolet.svg" alt="Kotlin 2.1.0">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/License-Proprietary-red.svg" alt="License">
</p>

---

## 📱 Features

- 🖤 **Pure AMOLED Black** – True #000000 black for maximum battery savings
- 🎨 **Premium Design** – Minimalist, futuristic UI with neon blue accents
- 📂 **Categories** – Organized wallpaper collections
- ❤️ **Favorites** – Save your favorite wallpapers
- 🔍 **Fullscreen Preview** – Zoom and pan support
- 📲 **Easy Apply** – Set as home screen, lock screen, or both
- 💾 **Download** – Save wallpapers to your device
- 🔒 **PRO Version** – One-time purchase for premium features
- 📴 **Offline Cache** – PRO users can access wallpapers offline
- ✨ **AI Generator (PRO)** – Create custom AMOLED wallpapers offline

---

## 🤖 AI Wallpaper Generator

**PRO Feature** – Generate unlimited custom wallpapers completely offline!

### Features
- 🔌 **100% Offline** – No internet, APIs, or servers required
- ⚡ **Fast Generation** – Procedural algorithms for instant results
- 🎨 **10 Unique Styles** – Geometric, Neon, Particles, Waves, and more
- 🎛️ **Full Control** – Adjust colors, intensity, and randomness
- 💾 **Save & Apply** – Save to gallery or set as wallpaper directly

### Available Styles
| Style | Description |
|-------|-------------|
| Geometric Lines | Sharp angular lines with glowing accents |
| Neon Glow | Soft glowing orbs on pure black |
| Particle Field | Scattered particles with connections |
| Abstract Waves | Flowing wave patterns with gradients |
| Dark Gradient | Smooth corner gradients |
| Constellation | Star field with connections |
| Minimal Shapes | Clean geometric shapes |
| Circuit Board | Tech-inspired circuit patterns |
| Aurora | Northern lights inspired bands |
| Fractal Noise | Organic noise with glow spots |

---

## 🏗️ Architecture

```
com.virex.wallpapers/
├── VirexApp.kt              # Application class (Hilt + Coil)
├── MainActivity.kt          # Single Activity entry point
├── generator/               # AI Wallpaper Generator (Procedural)
│   ├── WallpaperStyle.kt    # Style definitions
│   └── ProceduralGenerator.kt # Generation algorithms
├── di/                      # Dependency Injection (Hilt)
│   ├── AppModule.kt
│   ├── FirebaseModule.kt
│   └── BillingModule.kt
├── data/
│   ├── model/               # Data classes
│   ├── local/               # Room Database + DataStore
│   ├── remote/              # Firebase Data Source
│   └── repository/          # Repositories
├── domain/
│   └── usecase/             # Use Cases (optional layer)
└── ui/
    ├── theme/               # Material 3 Theme
    ├── navigation/          # Navigation Graph
    ├── screens/             # Screen Composables + ViewModels
    └── components/          # Reusable UI Components
```

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Async | Coroutines + Flow |
| Database | Room |
| Preferences | DataStore |
| Network | Firebase Firestore + Storage |
| Image Loading | Coil |
| Billing | Google Play Billing v7+ |

---

## 🚀 VS Code Setup

### Prerequisites

1. **JDK 17+**
   ```bash
   # Windows (Chocolatey)
   choco install openjdk17
   
   # macOS (Homebrew)
   brew install openjdk@17
   
   # Verify
   java -version
   ```

2. **Android SDK**
   - Download [Command-line tools](https://developer.android.com/studio#command-tools)
   - Extract to `C:\Android\Sdk` (Windows) or `~/Android/Sdk` (macOS/Linux)
   - Add to PATH:
     ```bash
     # Windows (PowerShell)
     $env:ANDROID_HOME = "C:\Android\Sdk"
     $env:PATH += ";$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools"
     
     # macOS/Linux
     export ANDROID_HOME=~/Android/Sdk
     export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
     ```

3. **Accept Licenses & Install SDK**
   ```bash
   sdkmanager --licenses
   sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"
   ```

### VS Code Extensions

Install these extensions:
- **Kotlin** by fwcd
- **Gradle for Java** by Microsoft
- **Gradle Language Support** by naco-siren
- **XML** by Red Hat

### Configure SDK Path

1. Create `local.properties` in project root:
   ```properties
   # Windows
   sdk.dir=C:\\Android\\Sdk
   
   # macOS/Linux
   sdk.dir=/Users/YOUR_USERNAME/Android/Sdk
   ```

2. Update `.vscode/settings.json` with your SDK path

---

## 📦 Build Commands

### Debug Build
```bash
# Windows
.\gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK
```bash
.\gradlew.bat assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Release AAB (for Google Play)
```bash
.\gradlew.bat bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### Install on Device
```bash
# Make sure device is connected via USB and USB debugging is enabled
adb devices

# Install debug APK
.\gradlew.bat installDebug
```

### Clean Build
```bash
.\gradlew.bat clean
```

---

## 🔐 Signing for Release

1. Generate keystore:
   ```bash
   keytool -genkey -v -keystore virex-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias virex
   ```

2. Update `app/build.gradle.kts`:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file("../virex-release.jks")
           storePassword = "YOUR_STORE_PASSWORD"
           keyAlias = "virex"
           keyPassword = "YOUR_KEY_PASSWORD"
       }
   }
   
   buildTypes {
       release {
           signingConfig = signingConfigs.getByName("release")
           // ... rest of config
       }
   }
   ```

3. Build signed AAB:
   ```bash
   .\gradlew.bat bundleRelease
   ```

---

## 🔥 Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project "VIREX Wallpapers"
3. Add Android app with package name `com.virex.wallpapers`
4. Download `google-services.json` and place in `app/` directory
5. Enable Firestore Database and Storage
6. Deploy security rules from `firebase/` folder

### Firestore Structure

```
/wallpapers/{wallpaperId}
  - id: string
  - title: string
  - thumbnail_url: string
  - full_url: string
  - category_id: string
  - is_premium: boolean
  - is_featured: boolean
  - is_trending: boolean
  - downloads: number
  - created_at: timestamp

/categories/{categoryId}
  - id: string
  - name: string
  - cover_url: string
  - wallpaper_count: number
  - sort_order: number
```

---

## 💰 Google Play Billing Setup

1. Create app in [Google Play Console](https://play.google.com/console)
2. Go to **Monetize** → **In-app products**
3. Create product:
   - **Product ID**: `virex_pro_unlock`
   - **Type**: One-time product
   - **Price**: $2.99
4. Activate the product
5. For testing, add tester emails in **License testing**

---

## 📤 Upload to Google Play

1. Build release AAB:
   ```bash
   .\gradlew.bat bundleRelease
   ```

2. Go to Google Play Console → **Production** → **Create new release**

3. Upload `app/build/outputs/bundle/release/app-release.aab`

4. Fill in:
   - Release name
   - Release notes
   - Content rating
   - Privacy policy URL

5. Review and submit

---

## 📁 Project Structure

```
virex-wall/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/virex/wallpapers/
│       │   ├── VirexApp.kt
│       │   ├── MainActivity.kt
│       │   ├── di/
│       │   ├── data/
│       │   └── ui/
│       └── res/
│           ├── values/
│           ├── xml/
│           └── mipmap-*/
├── assets/
│   └── logo.png
├── firebase/
│   ├── firestore.rules
│   └── storage.rules
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── .vscode/
│   ├── settings.json
│   ├── tasks.json
│   ├── launch.json
│   └── extensions.json
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

---

## 🎨 Design System

| Element | Value |
|---------|-------|
| Primary Background | `#000000` (True Black) |
| Accent Color | `#00D4FF` (Neon Blue) |
| Surface Card | `#1A1A1A` |
| PRO Badge | `#FFD700` (Gold) |
| Text Primary | `#FFFFFF` |
| Text Secondary | `#B3FFFFFF` (70% white) |

---

## 📝 License

Proprietary - All rights reserved.

---

## 🤝 Support

For issues and feature requests, please open an issue on GitHub.

---

<p align="center">
  Made with ❤️ for AMOLED displays
</p>
