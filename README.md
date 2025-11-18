# Kotlin Compose Multiplatform - Clean Architecture Boilerplate

A production-ready boilerplate for Kotlin Compose Multiplatform applications with Clean Architecture, SQLite as the single source of truth, and dynamic responsive UI.

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  (UI, ViewModels, Compose Components, WindowSizeClass)  │
└───────────────────┬─────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────┐
│                     Domain Layer                         │
│    (Entities, Use Cases, Repository Interfaces)         │
└───────────────────┬─────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────┐
│                      Data Layer                          │
│   (Repository Impl, Data Sources, SQLDelight, Mappers)  │
└─────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

#### 1. Domain Layer (`domain/`)
- **Entities**: Core business models (e.g., `Task.kt`)
- **Use Cases**: Business logic (e.g., `AddTaskUseCase`, `GetAllTasksUseCase`)
- **Repository Interfaces**: Contracts for data operations

**Key Principle**: This layer has NO dependencies on other layers or frameworks.

#### 2. Data Layer (`data/`)
- **Repository Implementations**: Concrete implementations of domain repositories
- **Data Sources**: SQLDelight database queries
- **Mappers**: Convert between database entities and domain models
- **Platform-Specific Drivers**: Database drivers for Android, iOS, and Desktop

**Key Feature**: SQLite is the **single source of truth** using SQLDelight with Flow for reactive updates.

#### 3. Presentation Layer (`presentation/`)
- **ViewModels**: Manage UI state and handle user interactions
- **UI State**: Immutable state classes
- **Composables**: Reusable UI components
- **Screens**: Full screen composables

**Key Feature**: Dynamic responsive UI using `WindowSizeClass` to adapt layout based on screen size.

## 🎯 Key Features

### ✅ Clean Architecture
- Clear separation of concerns
- Independent testable layers
- Business logic isolated from frameworks

### ✅ Single Source of Truth (SQLite)
- All data flows through the local SQLite database
- Reactive updates using Kotlin Flow
- No network layer in this boilerplate (easily extendable)

### ✅ Dynamic Responsive UI
- `WindowSizeClass` determines layout configuration
- **Compact** (< 600dp): Single column, phone portrait
- **Medium** (600-840dp): 2 columns, tablets/phone landscape
- **Expanded** (> 840dp): 3 columns, large tablets/desktop

### ✅ Dependency Injection
- Koin for DI across all platforms
- Platform-specific modules for platform dependencies
- Common module for shared dependencies

### ✅ Multiplatform Support
- **Android**: Minimum SDK 24, Target SDK 34
- **iOS**: iOS 13+ (via native SQLite driver)
- **Desktop**: JVM-based (Windows, macOS, Linux)

## 📁 Project Structure

```
kotlin-compose-multiplatform/
├── shared/                          # Shared multiplatform code
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/com/example/kmpcleanarch/
│       │   │   ├── domain/          # Domain layer
│       │   │   │   ├── model/       # Business entities
│       │   │   │   ├── repository/  # Repository interfaces
│       │   │   │   └── usecase/     # Use cases
│       │   │   ├── data/            # Data layer
│       │   │   │   ├── local/       # Local data sources
│       │   │   │   ├── mapper/      # Entity mappers
│       │   │   │   └── repository/  # Repository implementations
│       │   │   ├── presentation/    # Presentation layer
│       │   │   │   ├── viewmodel/   # ViewModels
│       │   │   │   ├── state/       # UI state classes
│       │   │   │   └── ui/          # Composables
│       │   │   │       ├── screen/  # Screens
│       │   │   │       ├── component/ # Reusable components
│       │   │   │       └── util/    # UI utilities
│       │   │   ├── di/              # Dependency injection
│       │   │   └── App.kt           # Main app composable
│       │   └── sqldelight/          # SQLDelight schemas
│       ├── androidMain/             # Android-specific code
│       ├── iosMain/                 # iOS-specific code
│       └── desktopMain/             # Desktop-specific code
├── androidApp/                      # Android application
├── desktopApp/                      # Desktop application
└── build.gradle.kts                 # Root build configuration
```

## 🚀 Getting Started

### Prerequisites

- **JDK 17** or higher
- **Android Studio** Hedgehog (2023.1.1) or later
- **Xcode 15+** (for iOS development on macOS)
- **Gradle 8.5+**

### Build and Run

#### Android
```bash
./gradlew :androidApp:installDebug
```

#### Desktop
```bash
./gradlew :desktopApp:run
```

#### iOS
Open the project in Xcode and run, or use:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

## 🔧 Technologies & Libraries

### Core
| Technology | Purpose |
|------------|---------|
| **Kotlin 1.9.21** | Programming language |
| **Compose Multiplatform 1.5.11** | UI framework |
| **SQLDelight 2.0.1** | Type-safe SQL database |
| **Koin 3.5.3** | Dependency injection |
| **Coroutines 1.7.3** | Asynchronous programming |
| **Flow** | Reactive streams |
| **Material 3** | UI components and theming |

### Extension Points (Included)
| Technology | Purpose | Status |
|------------|---------|--------|
| **Ktor 2.3.7** | HTTP client for networking | ✅ Configured |
| **Kotlinx Serialization 1.6.2** | JSON parsing | ✅ Configured |
| **Voyager 1.0.0** | Type-safe navigation | ✅ Configured |
| **Kotlinx DateTime 0.5.0** | Date/time utilities | ✅ Configured |

### Testing
| Technology | Purpose |
|------------|---------|
| **Kotlin Test** | Unit testing framework |
| **Coroutines Test** | Async testing |
| **Turbine** | Flow testing |
| **MockK** | Mocking library |
| **JUnit** | Test runner |

## 📝 Example Implementations

### Task Management App (Default)

The boilerplate includes a fully functional **Task Management** app demonstrating:

1. **CRUD Operations**: Create, Read, Update, Delete tasks
2. **Reactive UI**: Automatic updates when database changes
3. **State Management**: Centralized state with ViewModels
4. **Responsive Design**: Adapts to different screen sizes
5. **Error Handling**: User-friendly error messages

### Sample Pages (Login, List, Form) 🆕

Additionally, comprehensive sample pages are included showing:

1. **Login Screen** - User authentication with validation
2. **User List Screen** - Display list data with search/filter
3. **User Form Screen** - Create/edit form with validation

**To enable sample pages:** Replace `App()` with `SampleApp()` in your platform entry points.

**Demo Credentials:** Email: `any@example.com`, Password: `password123`

See **[docs/SAMPLE_PAGES.md](docs/SAMPLE_PAGES.md)** for complete documentation including:
- How to enable sample pages
- Architecture implementation details
- Navigation flow
- Customization guide
- Backend integration examples

## 🧪 Testing

The architecture is designed for easy testing:

- **Domain Layer**: Pure Kotlin, easy to unit test
- **Data Layer**: Test repository implementations with fake drivers
- **Presentation Layer**: Test ViewModels with fake use cases

## 🔄 Extending the Boilerplate

### Quick Start Extensions

All extension points are **already configured** and ready to use:

#### 1. Add Network Layer (Ktor)
```kotlin
// Example API implementation included in:
// shared/src/.../data/remote/api/TaskApi.kt

// Just implement your API:
class MyApiImpl(private val httpClient: HttpClient) : MyApi {
    override suspend fun getData(): Result<Data> {
        return try {
            val response = httpClient.get("/api/data").body<Data>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Register in Koin:
single { HttpClientFactory.create() }
single<MyApi> { MyApiImpl(get()) }
```

#### 2. Add Navigation (Voyager)
```kotlin
// Replace App() with NavigationApp() in platform entry points:
setContent {
    NavigationApp()  // Navigation is ready!
}

// Create screens and navigate:
class DetailScreenVoyager(val id: Long) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DetailScreen(id, onBack = { navigator.pop() })
    }
}

// Navigate:
navigator.push(DetailScreenVoyager(123))
```

#### 3. Add New Features

Follow the established pattern - see `docs/EXTENDING.md` for detailed guide:

1. **Domain**: Define entity, repository interface, and use cases
2. **Data**: Implement repository, create database schema (SQLDelight)
3. **Presentation**: Create ViewModel, UI state, and composables
4. **DI**: Register dependencies in Koin modules

Example structure for a "Notes" feature already provided in documentation!

## 📱 Dynamic UI Examples

### Compact (Phone Portrait)
```kotlin
WindowSizeClass.COMPACT → 1 column, 8dp spacing
```

### Medium (Tablet/Landscape)
```kotlin
WindowSizeClass.MEDIUM → 2 columns, 12dp spacing
```

### Expanded (Desktop/Large Tablet)
```kotlin
WindowSizeClass.EXPANDED → 3 columns, 16dp spacing
```

## 🎓 Best Practices Implemented

1. ✅ **Unidirectional Data Flow**: State flows down, events flow up
2. ✅ **Immutable State**: UI state is read-only
3. ✅ **Single Source of Truth**: Database is the authoritative data source
4. ✅ **Separation of Concerns**: Each layer has a single responsibility
5. ✅ **Dependency Inversion**: High-level modules don't depend on low-level modules
6. ✅ **Platform-Specific Code**: Encapsulated in expect/actual declarations

## 🗄️ Database Schema

The SQLDelight schema (`Task.sq`) defines:

```sql
CREATE TABLE TaskEntity (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    isCompleted INTEGER AS Boolean NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);
```

## 🔐 Optimized for Future Enhancements

This boilerplate is designed to be **easily optimized** later:

- **Modular architecture**: Add features without refactoring core
- **Platform independence**: Shared logic works everywhere
- **Scalable DI**: Easy to add new dependencies
- **Extensible UI**: Component-based design for reusability
- **Database migrations**: SQLDelight supports schema versioning

## 📚 Documentation

Comprehensive guides are available in the `docs/` directory:

### Architecture & Design
- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Complete architecture guide
  - Layer responsibilities and dependencies
  - Data flow patterns
  - Dependency rules
  - Common patterns and best practices

### Sample Pages & Examples
- **[SAMPLE_PAGES.md](docs/SAMPLE_PAGES.md)** - Complete sample pages guide 🆕
  - Login screen with authentication
  - User list with search/filter
  - User form (create/edit)
  - Navigation flow
  - Customization guide
  - Backend integration examples

### Extension Guides
- **[EXTENDING.md](docs/EXTENDING.md)** - How to extend the boilerplate
  - Adding new features step-by-step
  - Adding network layer (Ktor)
  - Adding authentication
  - Adding offline sync
  - Adding analytics
  - Complete example: Notes feature

### Testing
- **[TESTING.md](docs/TESTING.md)** - Complete testing guide
  - Testing strategy for each layer
  - Testing tools and utilities
  - Example tests for domain, data, and presentation layers
  - Best practices and patterns
  - CI/CD integration

### Quick Reference

| Topic | File Location | Description |
|-------|---------------|-------------|
| Network API | `data/remote/api/` | Ktor API interfaces and DTOs |
| Navigation | `presentation/navigation/` | Voyager screens and extensions |
| Testing Examples | `shared/src/commonTest/` | Unit tests for all layers |
| Utilities | `presentation/ui/util/` | DateTime, String, ResourceState utils |
| Domain Extensions | `domain/util/` | Result extensions |

## 📚 External Resources

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [Ktor Client](https://ktor.io/docs/getting-started-ktor-client.html)
- [Voyager Navigation](https://voyager.adriel.cafe/)
- [Koin DI](https://insert-koin.io/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

## 📄 License

This project is a boilerplate template - feel free to use it as a starting point for your own projects.

---

**Built with ❤️ using Kotlin Compose Multiplatform**
