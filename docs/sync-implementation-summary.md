# Sync Engine Implementation Summary

## Introduction

This document describes the sync engine implementation for the cooking app - a comprehensive solution for managing data synchronization between the offline-first frontend database (SQLDelight) and the backend server (PostgreSQL).

The sync engine enables users to:
- Work completely offline with full app functionality
- Automatically sync changes when connected
- Resolve conflicts intelligently when the same data is modified in multiple places
- Maintain data consistency across devices (future capability)

This implementation follows an event-tracking pattern with pragmatic conflict resolution, prioritizing user experience and data integrity while avoiding unnecessary complexity.

## Design Rationale: Why It's Built This Way

### 1. **Offline-First, Not Offline-Only**
**Decision**: Frontend database (SQLDelight) is the source of truth, backend syncs opportunistically.

**Why**:
- Users can use the app anywhere without connectivity concerns
- No loading spinners or failed requests during normal usage
- Backend becomes a backup and sharing mechanism, not a dependency
- Aligns with the app's personal tool philosophy

### 2. **Entity-Level Tracking, Not Event Sourcing**
**Decision**: Track entity states and versions rather than individual change events.

**Why**:
- Simpler to implement and debug
- Lower storage overhead (no event accumulation)
- Sufficient for recipe/cooking data (not collaborative real-time editing)
- Easier conflict resolution (compare states, not event sequences)

### 3. **Transparent Sync-Aware Wrappers**
**Decision**: Wrap existing repositories rather than modifying them directly.

**Why**:
- No breaking changes to existing code
- Clear separation of concerns (business logic vs sync logic)
- Easy to disable sync by switching repository implementations
- Testable in isolation

### 4. **"Newest Wins" with User Override**
**Decision**: Default to timestamp-based resolution but allow pinning.

**Why**:
- Works automatically 90% of the time without user intervention
- Respects user intent when they explicitly choose a version
- Simple mental model for users to understand
- Avoids complex merge algorithms that could corrupt recipe data

### 5. **Single Sync Endpoint Strategy**
**Decision**: One POST /api/sync endpoint handles all entity types.

**Why**:
- Reduces network round trips (batch operations)
- Simpler backend implementation
- Easier to maintain consistency across entity types
- Natural transaction boundary for related changes

### 6. **Checksum-Based Integrity**
**Decision**: Generate checksums for all entities to detect changes.

**Why**:
- Quick change detection without deep comparison
- Catches unintended modifications
- Helps identify true conflicts vs false positives
- Lightweight compared to storing full history

### 7. **Sealed Interfaces for Extensibility**
**Decision**: Use sealed interfaces for sync actions and conflict resolutions.

**Why**:
- Type-safe exhaustive when expressions
- Easy to add new sync triggers or resolution strategies
- Compiler helps catch missing cases
- Clean API surface for future features

### 8. **Local-First IDs with Server Mapping**
**Decision**: Use UUID strings locally, map to server IDs during sync.

**Why**:
- Can create entities offline without ID collisions
- No need for complex ID reservation schemes
- Server can maintain its own ID sequences
- Clean separation between local and remote identity

### 1. Database Schema
- **sync_info** table: Tracks sync state for each entity
- **sync_conflicts** table: Stores unresolved conflicts for user resolution
- Migration 8.sqm adds these tables to SQLDelight

### 2. Core Models (`sync/models/SyncModels.kt`)
- **SyncAction**: Sealed interface for sync operations (AutoSync, ManualSync, ResolveConflict)
- **EntityType**: Enum for trackable entities (RECIPE, INGREDIENT, UNIT, QUANTITY, RECIPE_INGREDIENT)
- **SyncStatus**: Entity states (CLEAN, DIRTY, SYNCING, CONFLICT, ERROR)
- **EntitySyncInfo**: Sync metadata for each entity
- **SyncConflict**: Conflict data structure
- **ChecksumGenerator**: Utility for generating entity checksums

### 3. Sync Repository (`sync/repository/SyncRepository.kt`)
Handles all sync-related database operations:
- Initialize sync tracking for new entities
- Mark entities as dirty when modified
- Store and retrieve conflicts
- Prepare entities for sync batches
- Track sync status changes

### 4. Sync Engine (`sync/SyncEngine.kt`)
Orchestrates the sync process:
- `performSync()`: Main sync method
- Conflict detection and resolution
- State management for UI observation
- Default "newest wins" conflict resolver
- Manual conflict resolution support

### 5. Sync-Aware Repository Wrappers
Transparent sync tracking for CRUD operations:
- **SyncAwareRecipeRepository**: Wraps RecipeRepository
- **SyncAwareIngredientRepository**: Wraps IngredientRepository
- **SyncAwareUnitRepository**: Wraps UnitRepository
- **SyncAwareQuantityRepository**: Wraps QuantityRepository

Each wrapper automatically:
- Initializes sync info on create
- Marks entities dirty on update
- Cleans up sync data on delete

### 6. Dependency Injection (`sync/SyncModule.kt`)
Provides configured sync components:
```kotlin
val syncModule = SyncModule.create(
    database,
    recipeRepo,
    ingredientRepo,
    unitRepo,
    quantityRepo
)
```

## How to Use

### Basic Usage

1. **Initialize in your app**:
```kotlin
// In your DI setup
val syncModule = SyncModule.create(...)
val syncComponents = syncModule.getComponents()

// Use sync-aware repositories instead of regular ones
val recipeRepo = syncComponents.recipeRepository
val ingredientRepo = syncComponents.ingredientRepository
```

2. **Create entities with automatic sync tracking**:
```kotlin
// This automatically initializes sync tracking
val recipe = recipeRepo.createRecipe("Pasta Carbonara")
```

3. **Updates are automatically tracked**:
```kotlin
// This marks the entity as dirty for sync
recipeRepo.updateRecipe(recipeId, "Updated Name")
```

4. **Manual sync (when API is ready)**:
```kotlin
// Trigger sync manually
val result = syncComponents.syncEngine.performSync()
println("Synced: ${result.synced}, Conflicts: ${result.conflicts}")
```

5. **Monitor sync state**:
```kotlin
// Observe sync state in UI
syncEngine.syncState.collect { state ->
    when (state) {
        SyncState.IDLE -> // Show normal UI
        SyncState.SYNCING -> // Show progress indicator
        SyncState.HAS_CONFLICTS -> // Show conflict badge
        SyncState.ERROR -> // Show error message
    }
}
```

6. **Handle conflicts**:
```kotlin
// Get unresolved conflicts
val conflicts = syncEngine.getUnresolvedConflicts()

// Resolve a conflict
syncEngine.resolveConflict(
    conflictId = conflict.id,
    resolution = ConflictResolution.AcceptLocal
)
```

## What's Still Needed

### 1. API Integration
- Update the existing API service to include sync endpoints
- Implement the `/api/sync` endpoint in the backend
- Wire up the actual network calls in `SyncEngine.performSync()`

### 2. Background Sync Job
```kotlin
// Example implementation for app startup
class CookingApp : Application() {
    override fun onCreate() {
        super.onCreate()

        lifecycleScope.launch {
            delay(2000) // Let UI load
            if (networkAvailable) {
                syncModule.syncEngine.performSync()
            }
        }
    }
}
```

### 3. Conflict Resolution UI
Create a dialog/screen to show conflicts to users:
```kotlin
@Composable
fun ConflictResolutionDialog(
    conflict: SyncConflict,
    onResolve: (ConflictResolution) -> Unit
) {
    // Show local vs remote versions
    // Let user choose resolution
}
```

### 4. Backend Sync Tables
The backend already has the necessary entities with `local_id` fields. You'll need:
- Sync controller with `/api/sync` endpoint
- Logic to handle sync requests and detect conflicts
- Return appropriate `SyncResponse` objects

## Architecture Benefits

1. **Offline-First**: All operations work offline, sync happens opportunistically
2. **Transparent**: Using sync-aware repositories requires no changes to existing code
3. **Conflict Detection**: Automatic detection with user-friendly resolution
4. **Extensible**: Easy to add new entity types or sync strategies
5. **Testable**: Clean separation of concerns, mockable components

## Testing

To test the sync system:

1. **Unit tests**: Test individual components (SyncRepository, SyncEngine)
2. **Integration tests**: Test full sync flow with mock API
3. **Manual testing**:
   - Create/update entities offline
   - Trigger sync when online
   - Simulate conflicts by editing on "two devices" (two app instances)

## Migration Path

To start using sync in existing code:

1. Replace repository injections:
```kotlin
// Old
val recipeRepo = DbRecipeRepository(database)

// New
val syncModule = SyncModule.create(...)
val recipeRepo = syncModule.syncAwareRecipeRepository
```

2. The rest of your code remains unchanged!

## Summary

The sync engine is ready for:
- Tracking local changes
- Detecting conflicts
- Managing sync state
- Resolving conflicts

Once the API endpoints are implemented and the background job is set up, your app will have full offline-first sync capabilities with intelligent conflict resolution.