# Sample Pages Guide

This guide explains the three sample pages included in the boilerplate:
1. **Login Screen** - User authentication
2. **User List Screen** - Display list data
3. **User Form Screen** - Create/edit forms

## 🚀 Quick Start

### Enable Sample Pages

To see the sample pages in action, replace `App()` with `SampleApp()` in your platform entry points:

**Android** (`androidApp/src/main/kotlin/.../MainActivity.kt`):
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleApp()  // Instead of App()
        }
    }
}
```

**Desktop** (`desktopApp/src/jvmMain/kotlin/.../Main.kt`):
```kotlin
fun main() {
    startKoin {
        modules(platformModule, commonModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMP Clean Architecture"
        ) {
            SampleApp()  // Instead of App()
        }
    }
}
```

## 📱 Sample Pages Overview

### 1. Login Screen

**File:** `presentation/ui/screen/LoginScreen.kt`

**Features:**
- Email and password input fields
- Password visibility toggle
- Form validation
- Loading state during authentication
- Error handling with user-friendly messages
- Responsive layout

**Demo Credentials:**
- **Email:** Any valid email (e.g., `admin@example.com`)
- **Password:** `password123`

**Screenshot Flow:**
```
[Login Screen]
     ↓ (Enter credentials and tap Login)
[User List Screen]
```

**Key Components:**
- Email field with validation
- Password field with show/hide toggle
- Loading indicator during login
- Error card for failed login
- Responsive to keyboard

**ViewModel:** `LoginViewModel`
- Email/password state management
- Input validation
- Authentication logic
- Error handling

**Use Cases:**
- `LoginUseCase` - Handles authentication logic with validation

### 2. User List Screen

**File:** `presentation/ui/screen/UserListScreen.kt`

**Features:**
- Display list of users from repository
- Search/filter functionality
- Pull to refresh (reactive updates)
- User role badges (Admin, User, Moderator)
- Delete with confirmation dialog
- Navigate to edit screen
- Logout button

**Screenshot Flow:**
```
[User List Screen]
     ↓ (Tap + button)
[User Form Screen - Create Mode]

[User List Screen]
     ↓ (Tap user card)
[User Form Screen - Edit Mode]

[User List Screen]
     ↓ (Tap delete icon)
[Confirmation Dialog]
     ↓ (Confirm)
[User deleted, list updates]
```

**Key Components:**
- Search bar with real-time filtering
- User cards with avatar (initials)
- Role color coding:
  - 🔴 Admin (Red)
  - 🟣 Moderator (Purple)
  - 🔵 User (Blue)
- Delete confirmation dialog
- FAB for adding new users
- Empty state when no users

**ViewModel:** `UserListViewModel`
- User list state management
- Search/filter logic
- Delete operations
- Logout functionality

**Use Cases:**
- `GetAllUsersUseCase` - Observe users from repository
- `DeleteUserUseCase` - Delete user with validation
- `LogoutUseCase` - Handle user logout

### 3. User Form Screen

**File:** `presentation/ui/screen/UserFormScreen.kt`

**Features:**
- Create new users
- Edit existing users
- Form validation
- Role selection (chips)
- Optional fields
- Save/Cancel actions
- Loading state while saving
- Validation rules display

**Screenshot Flow:**
```
[User Form Screen]
     ↓ (Fill form fields)
     ↓ (Tap Create/Update)
[Saving... (Loading)]
     ↓ (Success)
[Navigate back to User List]
```

**Form Fields:**
1. **Full Name*** (Required)
   - Validation: Min 2 characters
   - Example: "John Doe"

2. **Email*** (Required)
   - Validation: Valid email format
   - Example: "john.doe@example.com"

3. **Role*** (Required)
   - Options: Admin, User, Moderator
   - UI: Filter chips for selection

4. **Avatar URL** (Optional)
   - URL to profile picture
   - Example: "https://example.com/avatar.jpg"

**Validation Rules:**
- ✓ Name must be at least 2 characters
- ✓ Email must be valid format
- ✓ Role must be one of: admin, user, moderator
- ✓ All required fields must be filled

**ViewModel:** `UserFormViewModel`
- Form state management
- Load user for editing
- Input validation
- Save operations

**Use Cases:**
- `SaveUserUseCase` - Create or update user with validation

## 🏗️ Architecture Implementation

All sample pages follow Clean Architecture:

### Domain Layer

**Entities:**
```kotlin
// domain/model/User.kt
data class User(
    val id: Long = 0,
    val email: String,
    val name: String,
    val role: String,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// domain/model/User.kt
data class AuthState(
    val user: User? = null,
    val token: String? = null,
    val isAuthenticated: Boolean = false
)
```

**Repositories (Interfaces):**
```kotlin
// domain/repository/AuthRepository.kt
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthState>
    suspend fun logout(): Result<Unit>
    fun observeAuthState(): Flow<AuthState>
}

// domain/repository/UserRepository.kt
interface UserRepository {
    fun observeAllUsers(): Flow<List<User>>
    suspend fun saveUser(user: User): Long
    suspend fun deleteUser(id: Long)
}
```

**Use Cases:**
- `LoginUseCase` - Email/password validation + authentication
- `LogoutUseCase` - Clear authentication state
- `GetAllUsersUseCase` - Observe user list
- `SaveUserUseCase` - Create/update with validation
- `DeleteUserUseCase` - Delete user

### Data Layer

**Repository Implementations:**
```kotlin
// data/repository/AuthRepositoryImpl.kt
class AuthRepositoryImpl : AuthRepository {
    // In-memory implementation for demo
    // In production: Replace with API calls + token storage
}

// data/repository/UserRepositoryImpl.kt
class UserRepositoryImpl : UserRepository {
    // In-memory implementation with sample data
    // In production: Replace with SQLDelight or API + Database
}
```

**Sample Data:**
The boilerplate includes 5 sample users:
1. John Doe (Admin)
2. Jane Smith (User)
3. Bob Wilson (Moderator)
4. Alice Brown (User)
5. Charlie Davis (User)

### Presentation Layer

**State Management:**
```kotlin
// presentation/state/LoginUiState.kt
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**ViewModels:**
- Handle UI state
- Call use cases
- Manage loading/error states
- Navigate on success

**Screens:**
- Pure Composable functions
- Observe ViewModel state
- Handle user interactions
- Display loading/error states

## 🔄 Navigation Flow

```
Application Start
       ↓
[LoginScreenVoyager]
       ↓ (Login success)
[UserListScreenVoyager]
       ↓ (Tap +)
[UserFormScreenVoyager(userId = 0)] ← Create Mode
       ↓ (Save)
[Back to UserListScreenVoyager]

[UserListScreenVoyager]
       ↓ (Tap user card)
[UserFormScreenVoyager(userId = X)] ← Edit Mode
       ↓ (Save)
[Back to UserListScreenVoyager]

[UserListScreenVoyager]
       ↓ (Tap Logout)
[LoginScreenVoyager] ← Back to login
```

**Navigation Implementation:**
- Uses Voyager for type-safe navigation
- `Navigator.push()` - Navigate to new screen
- `Navigator.pop()` - Go back
- `Navigator.replace()` - Replace current screen
- `Navigator.replaceAll()` - Clear stack and navigate

## 🎨 UI/UX Features

### Material 3 Design
- ✅ Material You color system
- ✅ Dynamic theming
- ✅ Typography scale
- ✅ Shape system

### Responsive Design
- ✅ Adapts to different screen sizes
- ✅ Proper padding and spacing
- ✅ Keyboard-aware layouts
- ✅ Touch target sizes

### Loading States
- ✅ Circular progress indicators
- ✅ Disabled inputs during loading
- ✅ Loading text feedback

### Error Handling
- ✅ Validation errors
- ✅ Network errors
- ✅ User-friendly messages
- ✅ Dismissible error cards

### Empty States
- ✅ "No users" empty state
- ✅ "No search results" state
- ✅ Helpful action hints

## 📝 Customization Guide

### Adding More Fields to User Form

1. **Add to Domain Entity:**
```kotlin
data class User(
    // ... existing fields
    val phoneNumber: String? = null,
    val address: String? = null
)
```

2. **Update UI State:**
```kotlin
data class UserFormUiState(
    // ... existing fields
    val phoneNumber: String = "",
    val address: String = ""
)
```

3. **Add to Form Screen:**
```kotlin
OutlinedTextField(
    value = uiState.phoneNumber,
    onValueChange = { viewModel.onPhoneNumberChanged(it) },
    label = { Text("Phone Number") }
)
```

4. **Update ViewModel:**
```kotlin
fun onPhoneNumberChanged(phoneNumber: String) {
    _uiState.update { it.copy(phoneNumber = phoneNumber) }
}
```

### Changing Validation Rules

Edit `SaveUserUseCase.kt`:
```kotlin
if (name.length < 3) {  // Changed from 2 to 3
    return Result.failure(IllegalArgumentException("Name must be at least 3 characters"))
}
```

### Adding More Roles

1. Update `UserFormUiState`:
```kotlin
val availableRoles: List<String> = listOf("admin", "user", "moderator", "guest")
```

2. Update `SaveUserUseCase` validation:
```kotlin
val validRoles = listOf("admin", "user", "moderator", "guest")
```

## 🔌 Integration with Backend

### Replace In-Memory Repository with Real API

1. **Implement AuthApi:**
```kotlin
class AuthApiImpl(private val httpClient: HttpClient) : AuthApi {
    override suspend fun login(email: String, password: String): Result<AuthState> {
        return try {
            val response = httpClient.post("/api/auth/login") {
                setBody(LoginRequest(email, password))
            }.body<AuthResponse>()

            Result.success(response.toAuthState())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

2. **Update Repository:**
```kotlin
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<AuthState> {
        return authApi.login(email, password)
            .onSuccess { authState ->
                tokenStorage.saveToken(authState.token!!)
                _authState.value = authState
            }
    }
}
```

3. **Use SQLDelight for Users:**
```kotlin
class UserRepositoryImpl(
    private val database: AppDatabase,
    private val userApi: UserApi
) : UserRepository {
    override fun observeAllUsers(): Flow<List<User>> {
        // Sync from API
        viewModelScope.launch {
            userApi.getUsers().onSuccess { users ->
                users.forEach { database.userQueries.insert(it.toEntity()) }
            }
        }

        // Return from database (single source of truth)
        return database.userQueries.selectAll()
            .asFlow()
            .mapToList()
            .map { it.map { entity -> entity.toDomainModel() } }
    }
}
```

## 📊 Testing Sample Pages

### Unit Tests

**Test ViewModel:**
```kotlin
class LoginViewModelTest {
    @Test
    fun `should show error when email is invalid`() = runTest {
        val viewModel = LoginViewModel(loginUseCase)

        viewModel.onEmailChanged("invalid-email")
        viewModel.login()

        assertNotNull(viewModel.uiState.value.error)
    }
}
```

**Test Use Case:**
```kotlin
class LoginUseCaseTest {
    @Test
    fun `should reject empty email`() = runTest {
        val result = loginUseCase("", "password123")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Email") == true)
    }
}
```

### UI Tests

```kotlin
class UserListScreenTest {
    @Test
    fun `should display user list`() {
        composeTestRule.setContent {
            UserListScreen(onAddUserClick = {}, onEditUserClick = {}, onLogout = {})
        }

        composeTestRule.onNodeWithText("John Doe").assertExists()
    }
}
```

## 🎯 Best Practices Demonstrated

1. ✅ **Clean Architecture** - Clear separation of concerns
2. ✅ **Single Source of Truth** - Repository pattern
3. ✅ **Unidirectional Data Flow** - State flows down, events flow up
4. ✅ **Immutable State** - Data classes with copy
5. ✅ **Type-Safe Navigation** - Voyager screens
6. ✅ **Proper Validation** - In use cases
7. ✅ **Error Handling** - Result type pattern
8. ✅ **Loading States** - User feedback
9. ✅ **Accessibility** - Proper labels and content descriptions
10. ✅ **Responsive Design** - Adapts to screen sizes

## 🚀 Next Steps

1. **Enable the sample pages** by using `SampleApp()`
2. **Explore the code** to understand the patterns
3. **Customize** the forms and validation
4. **Integrate with your backend** API
5. **Add more features** following the same patterns
6. **Write tests** for your features

The sample pages provide a complete reference implementation that you can learn from and build upon!
