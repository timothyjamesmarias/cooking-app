# Sync API Integration Plan

## Current State Analysis

### Frontend
- **ApiService**: Basic Ktor client with GET, POST, PUT, DELETE methods
- **API_BASEURL**: http://localhost:8080
- **Sync Engine**: Ready with SyncEntity/SyncResponse models

### Backend
- **Controllers**: RecipeController, IngredientController, recipeDraftsController
- **Repositories**: RecipeRepository, IngredientRepository
- **Missing**: Unit/Quantity repositories and controllers
- **Entities**: All entities have `localId` field for sync mapping

## Implementation Steps

### Phase 1: Backend Infrastructure (Start Here)

#### Step 1.1: Create Missing Repositories
```kotlin
// UnitRepository.kt
interface UnitRepository : JpaRepository<Unit, Long> {
    fun findByLocalId(localId: String): Unit?
    fun existsByLocalId(localId: String): Boolean
}

// QuantityRepository.kt
interface QuantityRepository : JpaRepository<Quantity, Long> {
    fun findByLocalId(localId: String): Quantity?
    fun existsByLocalId(localId: String): Boolean
}
```

#### Step 1.2: Create Sync DTOs
```kotlin
// SyncRequestDto.kt
data class SyncRequestDto(
    val entities: List<SyncEntityDto>
)

data class SyncEntityDto(
    val localId: String,
    val serverId: Long? = null,
    val type: String,
    val data: Map<String, Any?>,
    val version: Int,
    val timestamp: Long,
    val checksum: String
)

// SyncResponseDto.kt
data class SyncResponseDto(
    val results: List<SyncResultDto>
)

data class SyncResultDto(
    val localId: String,
    val serverId: Long? = null,
    val accepted: Boolean,
    val hasConflict: Boolean = false,
    val remoteData: Map<String, Any?>? = null,
    val remoteTimestamp: Long? = null,
    val errorMessage: String? = null
)
```

#### Step 1.3: Create Sync Service
```kotlin
@Service
class SyncService(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val unitRepository: UnitRepository,
    private val quantityRepository: QuantityRepository
) {
    fun processSync(request: SyncRequestDto): SyncResponseDto {
        // Process each entity
        // Check for conflicts
        // Update or create entities
        // Return results
    }
}
```

#### Step 1.4: Create Sync Controller
```kotlin
@RestController
@RequestMapping("/api/sync")
class SyncController(
    private val syncService: SyncService
) {
    @PostMapping
    fun sync(@RequestBody request: SyncRequestDto): SyncResponseDto {
        return syncService.processSync(request)
    }
}
```

### Phase 2: Frontend API Integration

#### Step 2.1: Add Sync API Methods
```kotlin
// In ApiService.kt
suspend fun sync(entities: List<SyncEntity>): SyncResponse {
    val request = SyncRequest(entities)
    return post("/api/sync", request)
}
```

#### Step 2.2: Wire Up SyncEngine
Update `SyncEngine.performSync()` to use actual API:
```kotlin
// Replace simulateLocalSync with:
val syncResponse = apiService.sync(syncBatch)
```

#### Step 2.3: Add Network Check
```kotlin
// Create ConnectivityManager
expect class ConnectivityManager {
    fun isOnline(): Boolean
}

// Desktop implementation
actual class ConnectivityManager {
    actual fun isOnline(): Boolean {
        // Try to reach backend
        return try {
            // Ping endpoint
            true
        } catch (e: Exception) {
            false
        }
    }
}
```

### Phase 3: Backend Query Optimization

#### Step 3.1: Batch Query Methods
Add to repositories:
```kotlin
// RecipeRepository
fun findAllByLocalIdIn(localIds: List<String>): List<Recipe>

// Similar for other repositories
```

#### Step 3.2: Efficient Relationship Loading
```kotlin
// Use JOIN FETCH to avoid N+1 queries
@Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.recipeIngredients WHERE r.localId IN :localIds")
fun findByLocalIdsWithIngredients(localIds: List<String>): List<Recipe>
```

### Phase 4: Conflict Detection Logic

#### Step 4.1: Version Tracking
```kotlin
// Add to entities
@Version
var version: Long = 0

// Check version during sync
fun hasConflict(local: SyncEntityDto, existing: Entity): Boolean {
    return existing.version > local.version &&
           existing.lastModified > local.timestamp
}
```

#### Step 4.2: Conflict Resolution in Service
```kotlin
fun resolveConflict(
    local: SyncEntityDto,
    existing: Entity
): SyncResultDto {
    // Return conflict info for frontend to handle
    return SyncResultDto(
        localId = local.localId,
        serverId = existing.id,
        accepted = false,
        hasConflict = true,
        remoteData = entityToMap(existing),
        remoteTimestamp = existing.lastModified
    )
}
```

### Phase 5: Testing Strategy

#### Step 5.1: Unit Tests
- SyncService logic tests
- Conflict detection tests
- Entity mapping tests

#### Step 5.2: Integration Tests
- Full sync flow with TestRestTemplate
- Conflict scenarios
- Batch operations

#### Step 5.3: End-to-End Tests
- Frontend to backend sync
- Multiple entity types
- Offline/online transitions

## Execution Order

1. **Week 1**: Backend Infrastructure (Phase 1)
   - Create repositories and DTOs
   - Implement basic SyncService
   - Add SyncController endpoint

2. **Week 2**: Frontend Integration (Phase 2)
   - Update ApiService
   - Wire up SyncEngine
   - Add connectivity check

3. **Week 3**: Optimization & Conflicts (Phase 3 & 4)
   - Batch queries
   - Conflict detection
   - Resolution logic

4. **Week 4**: Testing & Polish (Phase 5)
   - Comprehensive tests
   - Bug fixes
   - Performance tuning

## Key Decisions

### Why Single Endpoint?
- Reduces network calls
- Atomic batch operations
- Simpler transaction management
- Easier to maintain consistency

### Why Map<String, Any?> for Data?
- Flexible for different entity types
- Easy to serialize/deserialize
- No need for separate DTOs per entity
- Supports future entity types

### Why Version + Timestamp?
- Version for optimistic locking
- Timestamp for conflict resolution
- Both needed for accurate sync

## Success Criteria

1. All entity types sync successfully
2. Conflicts are detected accurately
3. No data loss during sync
4. Performance: <2s for typical sync
5. Handles offline/online transitions gracefully

## Risks & Mitigations

| Risk | Mitigation |
|------|------------|
| Large sync batches | Implement pagination |
| Network timeouts | Add retry logic with exponential backoff |
| Data corruption | Validate checksums before applying |
| Version drift | Periodic full sync option |
| Concurrent edits | Pessimistic locking for critical paths |

## Next Immediate Action

Start with Step 1.1 - Create the missing repositories in the backend. This is the foundation everything else builds on.