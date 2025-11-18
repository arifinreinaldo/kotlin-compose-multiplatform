# Quick Start Guide

Get your Kotlin Compose Multiplatform app running in 5 minutes!

## 📋 Prerequisites

### Required for All Platforms
- **JDK 17 or higher** ([Download](https://adoptium.net/))
- **Git** (to clone the repository)

### For Android Development
- **Android Studio** Hedgehog (2023.1.1) or later ([Download](https://developer.android.com/studio))
- **Android SDK** (installed via Android Studio)

### For Desktop Development
- **IntelliJ IDEA** or **Android Studio** with Kotlin plugin

### For iOS Development (macOS only)
- **Xcode 15+** ([Mac App Store](https://apps.apple.com/app/xcode/id497799835))
- **CocoaPods** (install via: `sudo gem install cocoapods`)

## 🚀 5-Minute Setup

### Step 1: Verify Java Installation

```bash
java -version
# Should show: openjdk version "17" or higher
```

If not installed, download from [Adoptium](https://adoptium.net/).

### Step 2: Choose Your Experience

You have two options:

#### Option A: Task Management App (Default)
The boilerplate includes a working task management app. Ready to run immediately!

#### Option B: Sample Pages (Login, List, Form)
See comprehensive examples of authentication, list display, and forms.

**To enable Sample Pages:**

1. Open `androidApp/src/main/kotlin/com/example/kmpcleanarch/android/MainActivity.kt`
2. Change this line:
```kotlin
setContent {
    App()  // Default task app
}
```
To:
```kotlin
setContent {
    SampleApp()  // Sample pages with login
}
```

3. Do the same for Desktop in `desktopApp/src/jvmMain/kotlin/com/example/kmpcleanarch/Main.kt`

**Sample Pages Demo Credentials:**
- Email: `admin@example.com` (or any valid email)
- Password: `password123`

### Step 3: Run on Your Platform

#### 🤖 Android

**Using Android Studio:**
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the project directory
4. Wait for Gradle sync to complete (first time takes 2-5 minutes)
5. Select "androidApp" run configuration
6. Click the green "Run" button ▶️
7. Choose your device/emulator

**Using Command Line:**
```bash
# Build and install on connected device/emulator
./gradlew :androidApp:installDebug

# Or run directly
./gradlew :androidApp:run
```

#### 🖥️ Desktop

**Using IntelliJ IDEA / Android Studio:**
1. Open the project
2. Wait for Gradle sync
3. Select "desktopApp" run configuration
4. Click "Run" ▶️

**Using Command Line:**
```bash
# Run desktop app
./gradlew :desktopApp:run
```

The app will launch in a native window!

#### 🍎 iOS (macOS only)

**Method 1: Xcode (Recommended)**
1. Open Android Studio and sync Gradle
2. Run this command to generate Xcode project:
```bash
./gradlew :shared:podInstall
```
3. Open `iosApp/iosApp.xcworkspace` in Xcode
4. Select a simulator (iPhone 15 Pro recommended)
5. Click "Run" ▶️

**Method 2: Command Line**
```bash
# Build iOS framework
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# This creates the framework, then open in Xcode
open iosApp/iosApp.xcworkspace
```

## ✅ Verification

### You should see:

**Default App (Tasks):**
- An empty task list
- A "+" floating button
- Tap "+" to add a task
- Create some tasks and see them appear
- Tap tasks to toggle completion
- Swipe or tap "×" to delete

**Sample App (Login):**
- A login screen
- Enter email: `admin@example.com`
- Enter password: `password123`
- Click "Login"
- See a list of 5 sample users
- Search, edit, or create new users

### If it doesn't work:

**Common issues:**

1. **Gradle sync fails:**
   - Check internet connection
   - Wait and retry (first sync downloads many dependencies)
   - Clear Gradle cache: `rm -rf ~/.gradle/caches`

2. **Android build fails:**
   - Check Android SDK is installed
   - Update Android SDK in Android Studio
   - Set `ANDROID_HOME` environment variable

3. **Desktop app doesn't start:**
   - Verify Java 17+ is installed
   - Check Gradle daemon: `./gradlew --stop`

4. **iOS build fails:**
   - Update Xcode to latest version
   - Run: `pod repo update`
   - Clean build: Product → Clean Build Folder in Xcode

## 🎯 What to Try Next

### 1. Explore the Code

**Understanding the Architecture:**
- Read [`docs/ARCHITECTURE.md`](ARCHITECTURE.md) - Complete architecture guide
- Explore the layers:
  - `shared/src/commonMain/kotlin/.../domain/` - Business logic
  - `shared/src/commonMain/kotlin/.../data/` - Data & database
  - `shared/src/commonMain/kotlin/.../presentation/` - UI & ViewModels

### 2. Make Your First Change

**Easy Modification: Change App Title**

Edit `shared/src/commonMain/kotlin/.../presentation/ui/screen/TaskListScreen.kt`:

```kotlin
TopAppBar(
    title = { Text("My Awesome App") },  // Change this!
    // ...
)
```

Run the app again and see your change!

### 3. Add Your First Feature

Follow the step-by-step guide in [`docs/EXTENDING.md`](EXTENDING.md) to add a "Notes" feature.

### 4. Enable Network Layer

See [`docs/EXTENDING.md`](EXTENDING.md) section "Adding Network Layer" for:
- Setting up Ktor HTTP client
- Creating API interfaces
- Integrating with backend

### 5. Add Navigation

Already configured! See [`docs/SAMPLE_PAGES.md`](SAMPLE_PAGES.md) for examples.

### 6. Write Tests

Run existing tests:
```bash
# Run all tests
./gradlew :shared:allTests

# Run specific tests
./gradlew :shared:testDebugUnitTest
```

See [`docs/TESTING.md`](TESTING.md) for testing guide.

## 📱 Platform-Specific Features

### Android

**Enable Dark Mode:**
The app automatically follows system dark mode settings.

**Supported Android Versions:**
- Minimum: Android 7.0 (API 24)
- Target: Android 14 (API 34)

**Permissions:**
Currently no special permissions required. Add to `AndroidManifest.xml` as needed.

### Desktop

**Keyboard Shortcuts:**
Standard shortcuts work:
- `Cmd/Ctrl + Q` - Quit
- `Tab` - Navigate fields
- `Enter` - Submit forms

**Window Size:**
Default size is 800x600. Customize in `desktopApp/src/jvmMain/kotlin/.../Main.kt`:

```kotlin
Window(
    state = rememberWindowState(
        width = 1024.dp,
        height = 768.dp
    ),
    // ...
)
```

### iOS

**Supported iOS Versions:**
- Minimum: iOS 14.0
- Recommended: iOS 17.0+

**Build for Physical Device:**
1. Connect your iPhone
2. Select your device in Xcode
3. Update signing & capabilities
4. Click Run

## 🔧 Customization

### Change App Name

**Android:**
Edit `androidApp/src/main/AndroidManifest.xml`:
```xml
<application
    android:label="Your App Name"
```

**Desktop:**
Edit `desktopApp/build.gradle.kts`:
```kotlin
nativeDistributions {
    packageName = "Your App Name"
```

**iOS:**
Edit in Xcode: Select project → General → Display Name

### Change Package Name

1. Rename package in all files
2. Update `build.gradle.kts` files
3. Update `AndroidManifest.xml`
4. Sync Gradle

Better: Start fresh with your own package name from the beginning.

### Change Color Theme

Edit `shared/src/commonMain/kotlin/.../presentation/ui/theme/` (create if needed):

```kotlin
val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    // ... customize colors
)
```

## 📊 Project Structure Summary

```
kotlin-compose-multiplatform/
├── shared/              # Shared Kotlin code (95% of your code)
│   ├── domain/          # Business logic (pure Kotlin)
│   ├── data/            # Data layer (database, network)
│   └── presentation/    # UI code (Compose)
├── androidApp/          # Android-specific (5% of code)
├── desktopApp/          # Desktop-specific (5% of code)
├── iosApp/             # iOS-specific (handled by Xcode)
└── docs/               # Documentation
```

**You'll spend 95% of time in `shared/`!**

## 🆘 Getting Help

### Documentation

- **Architecture:** [`docs/ARCHITECTURE.md`](ARCHITECTURE.md)
- **Extending:** [`docs/EXTENDING.md`](EXTENDING.md)
- **Testing:** [`docs/TESTING.md`](TESTING.md)
- **Sample Pages:** [`docs/SAMPLE_PAGES.md`](SAMPLE_PAGES.md)

### Official Resources

- [Kotlin Multiplatform Docs](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight Docs](https://cashapp.github.io/sqldelight/)
- [Ktor Client Docs](https://ktor.io/docs/getting-started-ktor-client.html)

### Common Questions

**Q: Can I use this for production?**
A: Yes! The architecture is production-ready. Add your features following the established patterns.

**Q: Do I need to know iOS development?**
A: No! Write Kotlin code in `shared/`, and it works on iOS automatically. Only customize iOS-specific UI in Xcode if needed.

**Q: How do I add a database?**
A: Already included! SQLDelight is configured. See task repository examples.

**Q: How do I add network calls?**
A: Ktor is configured. See [`docs/EXTENDING.md`](EXTENDING.md) for examples.

**Q: Can I use this with existing backend?**
A: Yes! Replace the sample repository implementations with real API calls. See network layer guide.

## 🎉 Success Checklist

- [ ] App runs on Android
- [ ] App runs on Desktop
- [ ] App runs on iOS (if you have a Mac)
- [ ] Explored the sample features
- [ ] Read ARCHITECTURE.md
- [ ] Made a small code change
- [ ] Ran the tests
- [ ] Ready to build your app!

## 🚀 Next Steps

1. **Read the architecture guide** - Understand the patterns
2. **Study the sample code** - See how everything connects
3. **Plan your features** - What will you build?
4. **Follow EXTENDING.md** - Add your first feature
5. **Ship your app!** 🎉

Welcome to Kotlin Compose Multiplatform development! You're now ready to build amazing cross-platform apps with a solid foundation.

---

**Need help?** Check the documentation in the `docs/` folder or create an issue on GitHub.

**Happy coding!** 🚀
