# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **personal culinary multitool** built with Kotlin — a native-first cooking assistant combining Compose Multiplatform (frontend) and Spring Boot (backend). The architecture is designed for offline-first functionality with optional backend sync, and integrates with LLM services for contextual cooking assistance.

**Core Philosophy**: Local-first, ergonomic, modular, and educational. Must remain useful offline.

## Project Structure

```
cooking-app/
├── backend/              # Spring Boot REST API (Kotlin)
│   └── src/main/kotlin/com/timothymarias/cookingapp/
│       ├── controller/   # REST endpoints
│       ├── service/      # Business logic
│       ├── repository/   # JPA repositories
│       ├── entity/       # JPA entities
│       ├── dto/          # Data transfer objects
│       └── mapper/       # Entity-DTO converters
├── frontend/
│   ├── shared/           # Kotlin Multiplatform shared code
│   │   └── src/commonMain/kotlin/com/timothymarias/cookingapp/shared/
│   │       ├── data/           # Repositories, data sources
│   │       ├── domain/         # Business models
│   │       ├── presentation/   # UI state management (Stores)
│   │       └── App.kt          # Main Compose app entry
│   ├── android/          # Android app module
│   ├── ios/              # iOS app module
│   ├── desktop/          # Desktop (JVM) app module
│   └── web/              # Web app module (Compose for Web)
└── docs/                 # Design docs (see overview.md)
```

## Technology Stack

- **Language**: Kotlin (end-to-end)
- **Frontend**: Compose Multiplatform (Material 3)
- **Backend**: Spring Boot 3.5.6, Spring Data JPA, Flyway
- **Database**: PostgreSQL (backend), SQLDelight (frontend offline cache)
- **Networking**: Ktor Client (frontend), Spring Web (backend)
- **Build**: Gradle with Kotlin DSL
- **JVM**: Java 21

## Common Commands

### Backend (Spring Boot)

```bash
# Run backend server (starts PostgreSQL via Docker Compose)
./gradlew :backend:bootRun

# Run backend tests
./gradlew :backend:test

# Run specific test class
./gradlew :backend:test --tests "com.timothymarias.cookingapp.service.RecipeServiceTest"

# Build backend JAR
./gradlew :backend:build

# Access actuator endpoints (when running)
# http://localhost:8080/actuator/mappings
# http://localhost:8080/actuator/health
```

### Frontend (Compose Multiplatform)

```bash
# Run desktop app
./gradlew :frontend:desktop:run

# Run Android app (requires emulator/device)
./gradlew :frontend:android:installDebug

# Run all shared module tests
./gradlew :frontend:shared:allTests

# Run desktop tests only
./gradlew :frontend:shared:desktopTest

# Run iOS simulator tests
./gradlew :frontend:shared:iosSimulatorArm64Test

# Build all frontend targets
./gradlew :frontend:shared:build
```

### Database

```bash
# Start PostgreSQL (via Docker Compose)
docker compose up -d

# Stop PostgreSQL
docker compose down

# View Flyway migrations
ls backend/src/main/resources/db/migration/
```

### Full Project

```bash
# Build everything
./gradlew build

# Clean all build artifacts
./gradlew clean

# Run all tests
./gradlew check
```

## Architecture Patterns

### Frontend: Unidirectional Data Flow with Stores

The frontend uses a **Store pattern** for state management:

- **Store**: Holds `StateFlow<State>`, receives actions, updates state via coroutines
- **State**: Immutable data class representing UI state
- **Action**: Sealed class/interface representing user intents
- **Repository**: Abstracts data sources (local SQLDelight + remote API via Ktor)

**Example**: `RecipeStore` (frontend/shared/src/commonMain/kotlin/.../presentation/recipe/RecipeStore.kt)

```kotlin
class RecipeStore(private val repo: RecipeRepository) {
    private val _state = MutableStateFlow(RecipeState(isLoading = true))
    val state: StateFlow<RecipeState> = _state.asStateFlow()

    fun dispatch(action: RecipeAction) { /* ... */ }
}
```

### Backend: Layered Architecture

- **Controller** → **Service** → **Repository** → **Entity**
- DTOs for API contracts, Mappers for entity-DTO conversion
- JPA entities with Hibernate, managed by Flyway migrations

### Offline-First Design

- **SQLDelight** in `frontend/shared` provides local database (`.sq` files in `src/commonMain/sqldelight/`)
- Backend is optional; UI should degrade gracefully when offline
- Sync logic (when implemented) will use optimistic updates and conflict resolution

## Development Guidelines

### Adding a New Feature (Full Stack)

1. **Define shared domain model** in `frontend/shared/src/commonMain/kotlin/.../domain/model/`
2. **Create SQLDelight schema** in `frontend/shared/src/commonMain/sqldelight/.../db/` if offline support is needed
3. **Add Flyway migration** in `backend/src/main/resources/db/migration/` (e.g., `V3__description.sql`)
4. **Create JPA entity** in `backend/.../entity/`
5. **Build backend layer**: Repository → Service → DTO → Mapper → Controller
6. **Build frontend layer**: Repository (local + remote) → Store → UI
7. **Write tests**: Backend JUnit tests in `backend/src/test/`, shared module tests in `frontend/shared/src/commonTest/`

### Working with SQLDelight

- Schema files: `frontend/shared/src/commonMain/sqldelight/com/timothymarias/cookingapp/shared/db/*.sq`
- Database name: `CookingDatabase` (configured in `frontend/shared/build.gradle.kts`)
- After modifying `.sq` files, run `./gradlew :frontend:shared:generateCommonMainCookingDatabaseInterface` to regenerate code
- Access via platform-specific drivers (Android, iOS Native, JVM SQLite)

### Environment Setup

Create `.env` in project root with:

```bash
POSTGRES_DB=cooking_db
POSTGRES_USER=cooking_user
POSTGRES_PASSWORD=your_password
DB_PORT=5432
DB_URL=jdbc:postgresql://localhost:5432/cooking_db
SPRING_PROFILES_ACTIVE=dev
```

(See `compose.yaml` for Docker Compose configuration)

### Testing Philosophy

- **Backend**: Use Spring Boot Test with H2 in-memory DB for integration tests, Mockito-Kotlin for unit tests
- **Frontend**: Use Turbine for testing Flows, `desktopTest` for JVM-based tests with SQLite driver
- Always test repositories with real database interactions (in-memory or test containers)

## Module Dependencies

- **Frontend modules** depend on `:frontend:shared` (common code)
- **Shared module** has no dependency on backend (offline-first)
- Backend and frontend communicate via REST API (when online)

## Key Files

- `docs/overview.md` — Comprehensive feature roadmap and module descriptions
- `HELP.md` — Spring Boot reference docs
- `settings.gradle.kts` — Multi-project build structure
- `frontend/shared/build.gradle.kts` — KMP + Compose + SQLDelight configuration
- `backend/build.gradle.kts` — Spring Boot + JPA configuration
- `backend/src/main/resources/application.properties` — Backend runtime config

## Planned Features (See docs/overview.md)

**MVP Scope**: Recipe Library (CRUD), Pantry Tracker, Substitution Engine, basic theming

**Future**: Shopping List auto-generation, Cooking Assistant (step-by-step), Meal Planner, LLM integration for recipe parsing and suggestions, semantic search with vector embeddings

## Notes

- Git branch `feat-basic-recipe-ingredient-screens` is active (check `git status` for current work)
- Spring Boot DevTools enabled for hot reload during development
- Docker Compose auto-starts PostgreSQL when running `bootRun` (Spring Boot 3.5+ feature)
- Compose Multiplatform version: 1.7.0 (matches Kotlin 2.0.0)
