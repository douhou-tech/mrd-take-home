# SOLUTION.md — Architectural Evolution & Scalability Roadmap

This document outlines the architectural choices behind the **LikeABite** Restaurant Discovery solution and details how this project is designed to scale for a multi-squad, multi-feature enterprise Android application, including future readiness for **Kotlin Multiplatform (KMP)**.

---

## 1. Modularization & Multi-Squad Organization

For a large-scale codebase maintained by multiple engineering squads, a monolithic feature or layer architecture creates build bottlenecks, merge conflicts, and unclear domain ownership. 

### Module Boundaries
The project is structured into highly decoupled, feature- and layer-based modules:

```
├── app                                 # Application assemble entry point, Koin DI graph, MainActivity
├── build-logic                         # Shared Gradle convention plugins
└── discovery
    ├── core:primitives                 # Pure Kotlin domain primitives & logging interfaces
    ├── data                            # Room DB, DataSources (Mock/Ktor), Mappers, Repositories
    ├── domain                          # Business logic, Models (Restaurant, MenuItem), Use Cases
    ├── feature                         # Self-contained UI flows (Browse, Details), ViewModels, NavKeys
    ├── ui                              # Design system tokens, reusable Compose components, Theme
    └── test                            # Shared testing helpers & test dispatchers
```

### Self-Contained Feature Flows
Each user flow (e.g., `:discovery:feature`, and future modules like `:cart:feature`, `:checkout:feature`, `:profile:feature`) is built as a **self-contained feature module**:
- Features encapsulate their own UI Composables, ViewModels, UI state definitions, and Navigation keys (`NavKey`).
- Features expose only public navigation contracts (e.g., `FeatureNavKey`) and a Koin DI module (`restaurantDiscoveryFeatureModule`).
- Internal implementation details (Composables, ViewModels, Delegates) remain `internal` or scoped within the feature module, preventing unwanted cross-feature dependencies.

### Component & Primitive Sharing
- **`:discovery:ui`**: Houses shared design system components (`MrDRetryContent`, `MrDScreenLoader`, `FavouriteIconButton`, `RemoteImage`), typography, colors, and layout dimens. Teams can build new screens rapidly using established design tokens.
- **`:discovery:core:primitives`**: Contains zero-dependency shared data types (`PagedResult`, `Pagination`, `EitherResult`) and logging contracts.
- **`build-logic`**: Centralized Gradle convention plugins encapsulate build configuration (Android SDKs, Compose compiler, Room KSP setup, Build flavors). Squads do not write boilerplate Gradle scripts; they simply apply convention plugins (`alias(libs.plugins.mrd.android.compose)`, etc.).

---

## 2. Navigation, State, and Data Boundaries

### ViewModel-Driven Navigation for Pure Testability
In this application, navigation decisions are driven from the **ViewModel layer** via an injected navigation abstraction (`FeatureNavigator` / `AppNavigator`):
```kotlin
// ViewModel triggers navigation declaratively
private fun onNavigateToDetails(restaurant: Restaurant) {
    val state = _uiState.value
    val isFavourite = if (state is RestaurantDiscoveryUiState.Success) {
        state.favourites.contains(restaurant.id)
    } else {
        restaurant.favourite
    }
    val updatedRestaurant = restaurant.copy(favourite = isFavourite)
    navigator.navigateTo(FeatureNavKey.RestaurantDetails.ByData(updatedRestaurant))
}
```
**Benefits:**
- **Easy Unit Testing**: Navigation logic—such as conditional navigation, deep link redirection, or argument passing—can be unit tested directly in JVM tests without running Compose UI or Android framework tests.
- **Decoupled Composables**: UI Composables remain pure presentation functions that only emit intents (`onIntent(...)`) and receive UI states.

### Unidirectional Data Flow (UDF) & Immutable State
- ViewModels expose read-only `StateFlow<UiState>` instances containing immutable data classes (`RestaurantDiscoveryUiState`, `RestaurantDetailsUiState`).
- User actions are sent to ViewModels as sealed Intent interfaces (`RestaurantDiscoveryIntent`, `RestaurantDetailsIntent`).
- Reactive data streams (such as Room DAO `Flow<Set<String>>` for favorite updates) update the ViewModel state atomically, ensuring the UI always reflects the single source of truth.

### Strict Data Layer & Domain Boundaries
- **Clear Separation of Concerns**: The Domain layer (`:discovery:domain`) is completely decoupled from the Data layer (`:discovery:data`). Domain defines pure repository interfaces (`RestaurantsRepository`), domain models (`Restaurant`, `MenuItem`), and UseCases with zero framework or networking/database dependencies.
- **Model Mapping Pipeline**:
  - Remote Data Models (`RestaurantRemoteModel`) -> Local Database Entities (`RestaurantEntity`) -> Pure Domain Models (`Restaurant`).
- **Plug-and-Play Data Layer & Easy Testing**:
  - Because feature modules depend **only** on Domain interfaces and Use Cases, the entire Data implementation can be swapped out—such as replacing Ktor with Retrofit, Apollo GraphQL, or gRPC, or replacing Room with SQLDelight or DataStore—without touching or affecting a single line of core business rules or UI feature code.
  - Unit testing business logic and ViewModels is straightforward, fast, and deterministic because repositories can be easily stubbed or mocked without spinning up HTTP engines or database instances.

---

## 3. Kotlin Multiplatform (KMP) Readiness & Library Selection

The architecture and library choices were intentionally selected to make future porting to **Kotlin Multiplatform (KMP)** seamless.

### KMP-Friendly Tech Stack
All core dependencies were selected because they have official KMP support or mature KMP equivalents:
- **Ktor Client (`io.ktor:ktor-client-*`)**: Modern, asynchronous, multiplatform HTTP client replacing Retrofit.
- **Koin (`io.insert-koin:koin-*`)**: Kotlin-first Dependency Injection with KMP support for sharing DI definitions across Android, iOS, and Desktop.
- **Room (`androidx.room`)**: Room 2.7+ / 2.8+ natively supports SQLite on KMP (`commonMain` / iOS target support).
- **Kotlinx Serialization (`kotlinx.serialization`)**: Multiplatform JSON parsing.
- **Kotlinx Coroutines & Flow**: Pure Kotlin asynchronous concurrency.
- **Coil 3 (`io.coil-kt.coil3`)**: Compose Multiplatform image loading library.
- **Navigation 3 (`androidx.navigation3`)**: Modern Compose-first navigation model suitable for multiplatform Compose targets.

### Migration Path to KMP
Because `:discovery:domain`, `:discovery:core:primitives`, and `:discovery:data` contain pure Kotlin code (with KMP-ready Room and Ktor abstractions), porting the app to KMP requires minimal effort:
1. Move domain models, use cases, mappers, repositories, and Ktor client setups into `commonMain`.
2. Replace Android-specific context calls in local database builders with platform-agnostic Room drivers / expect-actual declarations.
3. Share Compose UI screens in `commonMain` across Android, iOS, and Web/Desktop targets by simply adjusting package imports.

---

## 4. Networking, Mock Data & TTL Caching Strategy

### Mock JSON Assets vs. Production Ktor Client
- **Take-Home Alignment**: For this demo project, mock JSON files (`restaurants.json`, `restaurant_details.json`) were bundled in assets. This guarantees deterministic, instant, and offline-capable testing without reliance on external backend availability.
- **Ktor as Production Preferred Client**: `Ktor` (`ktor-client-okhttp`, `ktor-client-content-negotiation`) is fully configured in dependencies as the preferred HTTP client for production backend integrations.

### Time-To-Live (TTL) Caching Architecture
While basic offline database persistence is implemented in Room, production-grade network call caching would be structured as follows:
1. **Ktor Layer HTTP Caching**: Utilizing Ktor’s `HttpCache` plugin with standard HTTP `Cache-Control` headers (`max-age`, `ETag`, `If-None-Match`) to prevent redundant network fetches.
2. **Repository TTL Cache Strategy**: In `RestaurantsRepositoryImpl`, attaching timestamp metadata to database entities (`lastUpdatedMillis`). When fetching restaurant data:
   - If `System.currentTimeMillis() - entity.lastUpdatedMillis < TTL_DURATION` (e.g. 5 minutes), serve directly from Room database.
   - Otherwise, execute a background Ktor fetch, refresh Room database, and emit the updated state via `Flow`.

---

## 5. Testing Strategy for Key Features

A comprehensive testing approach is established across all layers:

### 1. ViewModel & Navigation Unit Tests
Tests verify UI state transitions, user intent handling, and navigation routing using fake repositories and `TestDispatcher`:
```kotlin
@Test
fun `when NavigateToDetails intent then navigator receives correct updated restaurant`() = runTest {
    viewModel.onIntent(RestaurantDiscoveryIntent.NavigateToDetails(restaurant))
    
    // Verify navigation target and parameters in pure unit test
    verify { navigator.navigateTo(FeatureNavKey.RestaurantDetails.ByData(expected)) }
}
```

### 2. Domain & Use Case Unit Tests
Pure Kotlin unit tests validating filtering, pagination boundaries, favorite toggling, and business rules (`FetchRestaurantByIdUseCaseTest`, `FilterRestaurantsUseCaseTest`).

### 3. Repository & Data Layer Tests
Mocking Ktor / local DAO responses to verify mapping accuracy and data merging (`upsertRestaurants` maintaining favorite state when updating remote entity details).

### 4. UI & Visual Regression Testing
- **Compose Preview Tests / Paparazzi**: Screenshot tests verifying light/dark theme rendering, empty states, closed restaurant overlays, and favorite icon states.
- **Compose Test Rule**: UI tests simulating user scrolling, clicking favorite buttons, and verifying list behavior.

---

## 6. Keeping UI Smooth at Large Scale (60/120 FPS)

When scaling to thousands of restaurant entries, smooth UI performance is maintained through key Compose & architecture practices:

1. **LazyColumn Keying**:
   Every item in `LazyColumn` uses a stable unique identifier key: `items(restaurants.size, key = { restaurants[it].id })`. This allows Compose to reuse and reorder item nodes efficiently without re-instantiating item views.
2. **Optimized Scroll Calculations with `derivedStateOf`**:
   Filter bar visibility and scroll threshold triggers use `derivedStateOf` to prevent recomposing the parent layout on every single scroll pixel delta.
3. **Off-Thread Mapping & Database Queries**:
   All database operations in `LocalRestaurantsDataSourceImpl` and mapping in `RestaurantsMapper` run on `Dispatchers.IO`, keeping the main thread free for frame rendering.
4. **Pagination Windowing**:
   Data is fetched and paginated in chunks (`pageSize = 20`) via `Pagination` parameters, preventing memory bloat from inflating thousands of items simultaneously.
5. **Image Loading Optimization**:
   Coil 3 manages disk/memory caching, asynchronous cross-fading, and downscaling for restaurant thumbnail images.

---

## 7. Incorporating AI-Assisted Development Productively

To integrate AI development tools safely and productively across multiple engineering squads:

1. **Standardized Prompt Engineering & Rules**:
   Context guidelines and skill definitions enforce consistent MVI/UDF patterns, Koin DI usage, and Compose best practices across generated code.
2. **Automated Architectural Guardrails**:
   Automated linters (Detekt, ktlint) and custom Room/Compose checks run on pull requests to ensure AI-generated code conforms strictly to project conventions.
3. **Human-in-the-Loop Code Review & Verification**:
   AI tools generate code and unit tests, but human engineers review data boundaries, security implications, and edge cases before merging.
4. **Living Architectural Documentation**:
   Maintaining `SOLUTION.md` and module README files ensures AI assistants have full context when proposing refactors or new features.
