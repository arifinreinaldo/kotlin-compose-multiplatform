# CI/CD and Testing Guide

Complete guide for Continuous Integration, Continuous Deployment, and automated testing in this Kotlin Compose Multiplatform project.

## Table of Contents

- [Overview](#overview)
- [GitHub Actions Workflows](#github-actions-workflows)
- [Testing Suite](#testing-suite)
- [Running Tests Locally](#running-tests-locally)
- [CI/CD Best Practices](#cicd-best-practices)
- [Troubleshooting](#troubleshooting)

## Overview

This project uses **GitHub Actions** for CI/CD with comprehensive workflows for:

- ✅ **Automated Testing** - Unit tests, integration tests, UI tests
- ✅ **Multi-Platform Builds** - Android, iOS, Desktop (Windows, macOS, Linux)
- ✅ **Code Quality** - Linting, static analysis with Detekt
- ✅ **Automated Releases** - Tag-based releases with artifacts
- ✅ **Artifact Management** - APKs, installers, and build outputs

## GitHub Actions Workflows

### 1. Main CI Workflow

**File**: `.github/workflows/ci.yml`

**Triggers**:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`
- Manual workflow dispatch

**Jobs**:
1. **validate** - Validates Gradle wrapper
2. **test** - Runs all unit tests
3. **lint** - Runs Detekt static analysis
4. **build-shared** - Builds shared KMP module
5. **build-android** - Builds Android APK
6. **build-desktop** - Builds Desktop app

**What it does**:
```yaml
on: [push, pull_request]
jobs:
  test:
    - Validates Gradle wrapper
    - Runs all tests with ./gradlew test
    - Uploads test results as artifacts
  
  build:
    - Builds for all platforms
    - Uploads build artifacts
```

**Artifacts**:
- Test results (HTML reports)
- Lint results
- Build outputs (APK, JARs)

### 2. Android Build Workflow

**File**: `.github/workflows/android-build.yml`

**Triggers**:
- Push/PR with changes to `shared/**` or `androidApp/**`
- Manual dispatch

**Jobs**:
1. **build** - Builds debug and release APKs
2. **instrumented-tests** - Runs UI tests on emulator (PR only)

**Features**:
- Matrix build (debug + release)
- Android emulator tests
- APK artifact upload

**Example**:
```bash
# Locally replicate this workflow
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:assembleRelease
./gradlew :androidApp:connectedDebugAndroidTest
```

### 3. iOS Build Workflow

**File**: `.github/workflows/ios-build.yml`

**Triggers**:
- Push/PR with changes to `shared/**` or `iosApp/**`
- Manual dispatch

**Jobs**:
1. **build-ios** - Builds iOS framework and app

**Features**:
- Runs on macOS runner
- Builds for iOS Simulator
- CocoaPods integration
- Xcode build

**Requirements**:
- macOS runner (macos-14)
- Xcode 15.2+
- CocoaPods

### 4. Desktop Build Workflow

**File**: `.github/workflows/desktop-build.yml`

**Triggers**:
- Push/PR with changes to `shared/**` or `desktopApp/**`
- Manual dispatch

**Jobs**:
1. **build-linux** - Creates .deb package
2. **build-windows** - Creates .msi installer
3. **build-macos** - Creates .dmg package

**Artifacts**:
- Linux: `.deb` package
- Windows: `.msi` installer
- macOS: `.dmg` disk image

### 5. Release Workflow

**File**: `.github/workflows/release.yml`

**Triggers**:
- Push to tag matching `v*.*.*` (e.g., `v1.0.0`)
- Manual dispatch

**Jobs**:
1. **create-release** - Creates GitHub release
2. **build-and-release-android** - Builds and uploads APK
3. **build-desktop-***  - Builds platform-specific installers

**How to create a release**:
```bash
# Tag your release
git tag v1.0.0
git push origin v1.0.0

# GitHub Actions will automatically:
# 1. Create a GitHub release
# 2. Build all platform artifacts
# 3. Upload to the release
```

**Release artifacts**:
- Android APK
- Linux .deb
- Windows .msi
- macOS .dmg

## Testing Suite

### Test Structure

```
shared/src/commonTest/kotlin/
├── domain/
│   └── usecase/
│       ├── SendAiMessageUseCaseTest.kt
│       ├── LoginUseCaseTest.kt
│       └── AddTaskUseCaseTest.kt
└── presentation/
    └── viewmodel/
        └── AiChatViewModelTest.kt
```

### 1. Domain Layer Tests

#### Use Case Tests

Tests business logic in isolation using fake repositories.

**Example: SendAiMessageUseCaseTest.kt**

```kotlin
class SendAiMessageUseCaseTest {
    @Test
    fun `should reject empty message`() = runTest {
        val fakeRepository = FakeAiRepository()
        val useCase = SendAiMessageUseCase(fakeRepository)

        val result = useCase("", emptyList(), testModel)

        assertTrue(result.isFailure)
        assertEquals("Message cannot be empty", 
            result.exceptionOrNull()?.message)
    }
}
```

**What we test**:
- ✅ Input validation
- ✅ Business logic
- ✅ Error handling
- ✅ Repository interaction
- ✅ Data transformations

### 2. Data Layer Tests

#### Repository Tests

Uses fake implementations to test repository logic.

**Fake Pattern**:
```kotlin
class FakeAiRepository(
    private val shouldFail: Boolean = false
) : AiRepository {
    var lastMessage: String? = null
    
    override suspend fun sendMessage(...): Result<AiMessage> {
        lastMessage = message
        return if (shouldFail) {
            Result.failure(Exception("Error"))
        } else {
            Result.success(testResponse)
        }
    }
}
```

### 3. Presentation Layer Tests

#### ViewModel Tests

Tests UI state management using Turbine for Flow testing.

**Example: AiChatViewModelTest.kt**

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AiChatViewModelTest {
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }
    
    @Test
    fun `sendMessage should add user message and AI response`() = runTest {
        viewModel.setApiKey("test-key")
        viewModel.selectModel(testModel)
        viewModel.onInputChanged("Hello")
        
        viewModel.sendMessage()
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.messages.size)
        }
    }
}
```

**What we test**:
- ✅ State management
- ✅ User interactions
- ✅ Flow emissions
- ✅ Loading states
- ✅ Error handling

### Test Dependencies

All testing dependencies are already configured in `shared/build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            implementation("app.cash.turbine:turbine:1.0.0")
        }
    }
}
```

## Running Tests Locally

### Run All Tests

```bash
./gradlew test
```

### Run Tests for Specific Module

```bash
# Shared module tests
./gradlew :shared:test

# Android tests
./gradlew :androidApp:testDebugUnitTest

# Desktop tests
./gradlew :desktopApp:test
```

### Run Tests with Coverage

```bash
./gradlew test jacocoTestReport
```

Coverage reports will be generated at:
- `shared/build/reports/jacoco/test/html/index.html`

### Run Specific Test Class

```bash
./gradlew :shared:testDebug \
  --tests "SendAiMessageUseCaseTest"
```

### Run Tests with Detailed Output

```bash
./gradlew test --info --stacktrace
```

### Android Instrumented Tests

```bash
# Start emulator first, then:
./gradlew :androidApp:connectedDebugAndroidTest
```

### Watch Mode (Continuous Testing)

```bash
./gradlew test --continuous
```

## CI/CD Best Practices

### 1. Branch Protection

Configure branch protection rules on GitHub:

```yaml
# Settings → Branches → Add rule
Branch name pattern: main
☑ Require status checks to pass before merging
  ☑ test
  ☑ build-android
  ☑ build-desktop
☑ Require branches to be up to date
```

### 2. Secrets Management

Add secrets for releases:

```bash
# GitHub → Settings → Secrets → Actions
ANDROID_KEYSTORE_BASE64  # Base64 encoded keystore
KEYSTORE_PASSWORD        # Keystore password
KEY_ALIAS               # Key alias
KEY_PASSWORD            # Key password
```

### 3. Caching Strategy

All workflows use Gradle caching:

```yaml
- uses: actions/setup-java@v4
  with:
    cache: 'gradle'  # Auto-caches ~/.gradle
```

### 4. Artifact Retention

Configure retention periods based on importance:

```yaml
# Test results: 7 days
retention-days: 7

# Release artifacts: 14 days
retention-days: 14
```

### 5. Concurrency Control

Prevents duplicate runs:

```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```

## Continuous Integration Checklist

Before merging a PR, ensure:

- [ ] All tests pass
- [ ] Lint checks pass (Detekt)
- [ ] Android APK builds successfully
- [ ] Desktop app builds successfully
- [ ] iOS framework builds (if iOS app configured)
- [ ] No new warnings in build logs
- [ ] Test coverage hasn't decreased significantly

## Troubleshooting

### Tests Failing Locally But Pass in CI

**Cause**: Different environments

**Solution**:
```bash
# Clean and rebuild
./gradlew clean test

# Use same JDK version as CI (17)
java -version

# Check for cached test results
rm -rf .gradle/
```

### iOS Build Fails

**Common issues**:

1. **CocoaPods not installed**
   ```bash
   sudo gem install cocoapods
   cd iosApp && pod install
   ```

2. **Wrong Xcode version**
   ```bash
   xcode-select --install
   sudo xcode-select --switch /Applications/Xcode.app
   ```

### Android Instrumented Tests Timeout

**Solution**:
```bash
# Increase timeout in build.gradle.kts
android {
    testOptions {
        unitTests.all {
            timeout = 10 * 60 * 1000  // 10 minutes
        }
    }
}
```

### Gradle Build Fails in CI

**Check**:
1. Gradle wrapper is committed (`gradle/wrapper/`)
2. Executable permissions: `chmod +x gradlew`
3. Dependencies are available on Maven Central

### Test Flakiness

**Solutions**:

1. **Use deterministic test data**
   ```kotlin
   val testTimestamp = 1234567890L  // Fixed, not System.currentTimeMillis()
   ```

2. **Proper coroutine testing**
   ```kotlin
   @Test
   fun test() = runTest {
       // Use runTest from kotlinx-coroutines-test
       viewModel.doSomething()
       advanceUntilIdle()  // Wait for all coroutines
   }
   ```

3. **Avoid Thread.sleep()**
   ```kotlin
   // ❌ Bad
   Thread.sleep(1000)
   
   // ✅ Good
   advanceTimeBy(1000)
   ```

## Adding New Tests

### 1. Create Test File

```bash
# Domain layer
shared/src/commonTest/kotlin/domain/usecase/MyUseCaseTest.kt

# Presentation layer
shared/src/commonTest/kotlin/presentation/viewmodel/MyViewModelTest.kt
```

### 2. Write Test

```kotlin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class MyUseCaseTest {
    @Test
    fun `should do something`() = runTest {
        // Arrange
        val useCase = MyUseCase(fakeRepository)
        
        // Act
        val result = useCase("input")
        
        // Assert
        assertTrue(result.isSuccess)
    }
}
```

### 3. Run Test

```bash
./gradlew :shared:test --tests "MyUseCaseTest"
```

### 4. Verify in CI

Push to branch and check GitHub Actions run.

## Code Coverage

### Generate Coverage Report

```bash
./gradlew test jacocoTestReport
```

### View Coverage

Open `shared/build/reports/jacoco/test/html/index.html`

### Coverage Goals

- **Domain layer**: 80%+ coverage
- **Data layer**: 70%+ coverage
- **Presentation layer**: 60%+ coverage

### Add Coverage to CI

```yaml
# Add to .github/workflows/ci.yml
- name: Generate coverage report
  run: ./gradlew jacocoTestReport

- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
  with:
    files: ./shared/build/reports/jacoco/test/jacocoTestReport.xml
```

## Resources

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Kotlin Testing](https://kotlinlang.org/docs/testing.html)
- [Turbine (Flow Testing)](https://github.com/cashapp/turbine)
- [kotlinx-coroutines-test](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Android Testing](https://developer.android.com/training/testing)
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)

---

**Your CI/CD pipeline is ready!** 🎉

Push your code and watch the automated workflows build, test, and deploy your app across all platforms.
