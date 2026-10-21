# LikeABite - Restaurant Discovery Demo

**LikeABite** is a modern Android application for discovering local restaurants, viewing menus, and toggling favorite restaurants. Built using Clean Architecture and a modular feature-based structure with Jetpack Compose, Navigation 3, Koin dependency injection, Room Database, and Coroutines/Flow.

---

## 🛠 Prerequisites

- **JDK**: Version 17 or higher
- **Android Studio**: Ladybug (2024.2.1+) or newer
- **Android SDK**:
  - `compileSdk`: 37
  - `minSdk`: 28 (Android 9.0)
  - `targetSdk`: 37
- **Device / Emulator**: Android 9.0 (API level 28) or higher

---

## 🏗 Modular Architecture

The project is structured into clear feature and layer modules:

```
├── app                            # Application entry point, Koin DI assembly, MainActivity
├── build-logic                    # Custom Gradle convention plugins (Compose, Room, Build Environments)
└── discovery
    ├── core:primitives            # Shared primitive models (PagedResult, Pagination, EitherResult), Logging
    ├── data                       # Persistence (Room DB), Data sources (Local & Remote Mocks), Repositories
    ├── domain                     # Core business models (Restaurant, MenuItem) & Use Cases
    ├── feature                    # UI Screens (Browse & Details), ViewModels, Navigation Keys
    ├── ui                         # Shared Compose components (MrDRetryContent, Loaders, Buttons), Theme
    └── test                       # Unit test helpers & utilities
```

---

## 🚀 How to Build and Run Locally

### Product Flavors & Build Variants

The app includes product flavors configured for different environments:
- **`dev`** — Application ID: `com.mrd.restaurant.discovery.demo.dev`
- **`staging`** — Application ID: `com.mrd.restaurant.discovery.demo.staging`
- **`prod`** — Application ID: `com.mrd.restaurant.discovery.demo`

Each flavor supports `debug` and `release` build types (e.g., `devDebug`, `stagingDebug`, `prodDebug`).

### Using Android Studio

1. Open Android Studio and select **Open** -> choose the project root folder.
2. Allow Gradle to sync dependencies.
3. Open the **Build Variants** tool window (`View` > `Tool Windows` > `Build Variants`).
4. Select the **`devDebug`** variant for the `:app` module.
5. Connect an Android device or launch an emulator (API 28+).
6. Click **Run** (`Shift + F10`) or the green **Play** button.

### Using Command Line (Gradle)

- **Build Debug APK (`devDebug`)**:
  ```bash
  ./gradlew :app:assembleDevDebug
  ```

- **Install and Run on Connected Device/Emulator (`devDebug`)**:
  ```bash
  ./gradlew :app:installDevDebug
  ```

- **Clean and Build Everything**:
  ```bash
  ./gradlew clean build
  ```

---

## 🧪 Running Tests

### Unit Tests

- **Run unit tests for all modules**:
  ```bash
  ./gradlew test
  ```

- **Run unit tests for specific modules**:
  ```bash
  # Data layer tests
  ./gradlew :discovery:data:testDevDebugUnitTest

  # Feature layer tests (ViewModels & Delegates)
  ./gradlew :discovery:feature:testDevDebugUnitTest

  # Domain layer tests
  ./gradlew :discovery:domain:test

  # App module tests
  ./gradlew :app:testDevDebugUnitTest
  ```

---

## 🔗 Deep Linking

The app supports deep linking directly into the **Restaurant Details** screen for a given restaurant ID, constructing a back-stack so that pressing **Back** returns to the **Restaurant Discovery** screen.

### Deep Link Format

- **Strict URL Pattern**: `https://staging.mrd.likeabite.com/restaurant/details/{restaurantId}` or `http://staging.mrd.likeabite.com/restaurant/details/{restaurantId}`
- **Example Restaurant ID**: `cpt-cbd-003` (Clarke's Bar & Dining Room)

### Testing Deep Links via ADB

Ensure the app is installed on your running emulator or connected device, then run the ADB command matching your installed build flavor:

#### 1. Dev Flavor (`devDebug` - Default):
```bash
adb shell am start -W -a android.intent.action.VIEW -d "https://staging.mrd.likeabite.com/restaurant/details/cpt-cbd-003" com.mrd.restaurant.discovery.demo.dev
```

#### 2. Staging Flavor (`stagingDebug`):
```bash
adb shell am start -W -a android.intent.action.VIEW -d "https://staging.mrd.likeabite.com/restaurant/details/cpt-cbd-003" com.mrd.restaurant.discovery.demo.staging
```

#### 3. Production Flavor (`prodDebug`):
```bash
adb shell am start -W -a android.intent.action.VIEW -d "https://staging.mrd.likeabite.com/restaurant/details/cpt-cbd-003" com.mrd.restaurant.discovery.demo
```

#### 4. Generic Intent (System Picker):
```bash
adb shell am start -a android.intent.action.VIEW -d "https://staging.mrd.likeabite.com/restaurant/details/cpt-cbd-003"
```

### Expected Behavior
Executing any of the above commands will launch the app directly into the **Restaurant Details** screen for restaurant `cpt-cbd-003`. Tapping the back button will return directly to the **Restaurant Discovery** screen.

---

## 🔑 Key Features Implemented

- **Restaurant Discovery**: Search by query, filter by "Open Now", pagination, lazy loading, and shared element transitions.
- **Restaurant Details**: View restaurant details, full categorized menu items, error and retry handling.
- **Favorites Sync**: Reactive state updates when toggling favorites on either discovery or details screens, persisted in Room DB.
- **Error & Retry Handling**: Unified retry UI (`MrDRetryContent`) across screens for graceful error recovery.
