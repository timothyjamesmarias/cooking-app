package com.timothymarias.cookingapp.shared.sync

import com.timothymarias.cookingapp.shared.sync.models.*
import com.timothymarias.cookingapp.shared.sync.remote.SyncApiClient
import com.timothymarias.cookingapp.shared.sync.remote.SyncResult as ApiSyncResult
import com.timothymarias.cookingapp.shared.sync.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Core sync engine that orchestrates synchronization between local and remote databases
 */
class SyncEngine(
    private val syncRepository: SyncRepository,
    private val syncApiClient: SyncApiClient = SyncApiClient(),
    private val conflictResolver: ConflictResolver = DefaultConflictResolver()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    // Sync state for UI observation
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult.asStateFlow()

    /**
     * Main sync method - coordinates the sync process
     */
    suspend fun performSync(): SyncResult {
        _syncState.value = SyncState.SYNCING

        try {
            // 1. Get all dirty entities
            val dirtyEntities = syncRepository.getDirtyEntities()
            if (dirtyEntities.isEmpty()) {
                _syncState.value = SyncState.IDLE
                return SyncResult(synced = 0, conflicts = 0).also {
                    _lastSyncResult.value = it
                }
            }

            // 2. Mark entities as syncing
            dirtyEntities.forEach { entity ->
                syncRepository.markSyncing(entity.localId)
            }

            // 3. Prepare sync batch
            val syncBatch = syncRepository.prepareSyncBatch()

            // 4. Check connectivity before syncing
            if (!syncApiClient.isBackendAvailable()) {
                _syncState.value = SyncState.ERROR
                return SyncResult(
                    synced = 0,
                    conflicts = 0,
                    errors = listOf("Backend is not available")
                ).also {
                    _lastSyncResult.value = it
                }
            }

            // 5. Send to server
            val apiResponse = syncApiClient.performSync(syncBatch)

            // 6. Process the API response
            val syncResponse = apiResponse.fold(
                onSuccess = { results ->
                    convertApiResultsToSyncResponse(results)
                },
                onFailure = { error ->
                    _syncState.value = SyncState.ERROR
                    return SyncResult(
                        synced = 0,
                        conflicts = 0,
                        errors = listOf(error.message ?: "Sync failed")
                    ).also {
                        _lastSyncResult.value = it
                    }
                }
            )

            // 7. Process sync response
            val conflicts = processSyncResponse(syncResponse)

            // 8. Handle conflicts if any
            val unresolvedConflicts = if (conflicts.isNotEmpty()) {
                handleConflicts(conflicts)
            } else {
                0
            }

            // 9. Update sync state
            _syncState.value = if (unresolvedConflicts > 0) {
                SyncState.HAS_CONFLICTS
            } else {
                SyncState.IDLE
            }

            val result = SyncResult(
                synced = syncBatch.size - conflicts.size,
                conflicts = unresolvedConflicts
            )
            _lastSyncResult.value = result
            return result

        } catch (e: Exception) {
            _syncState.value = SyncState.ERROR
            val result = SyncResult(
                synced = 0,
                conflicts = 0,
                errors = listOf(e.message ?: "Unknown error")
            )
            _lastSyncResult.value = result
            return result
        }
    }

    /**
     * Process sync response from server
     */
    private suspend fun processSyncResponse(response: SyncResponse): List<SyncConflictInfo> {
        val conflicts = mutableListOf<SyncConflictInfo>()

        for (item in response.results) {
            when {
                // Successfully synced
                item.accepted && !item.hasConflict -> {
                    item.serverId?.let {
                        syncRepository.markClean(item.localId, it)
                    }
                }

                // Has conflict
                item.hasConflict -> {
                    val syncInfo = syncRepository.getSyncInfo(item.localId)
                    if (syncInfo != null) {
                        conflicts.add(
                            SyncConflictInfo(
                                entityId = item.localId,
                                entityType = syncInfo.entityType,
                                localTimestamp = syncInfo.lastModified,
                                remoteTimestamp = item.remoteTimestamp ?: 0,
                                remoteData = item.remoteData,
                                isPinned = syncInfo.isPinned
                            )
                        )
                        syncRepository.markConflicted(item.localId)
                    }
                }

                // Error
                !item.accepted && !item.hasConflict -> {
                    syncRepository.markError(item.localId)
                }
            }
        }

        return conflicts
    }

    /**
     * Handle sync conflicts
     */
    private suspend fun handleConflicts(conflicts: List<SyncConflictInfo>): Int {
        var unresolvedCount = 0

        for (conflictInfo in conflicts) {
            val resolution = if (conflictInfo.isPinned) {
                // User has pinned this entity, store conflict for manual resolution
                ConflictResolutionResult.RequiresUserInput
            } else {
                // Auto-resolve using configured strategy
                conflictResolver.resolve(conflictInfo)
            }

            when (resolution) {
                is ConflictResolutionResult.Resolved -> {
                    // Apply the resolution
                    applyConflictResolution(conflictInfo.entityId, resolution)
                }

                ConflictResolutionResult.RequiresUserInput -> {
                    // Store conflict for user resolution
                    val localData = getEntityData(conflictInfo.entityId, conflictInfo.entityType)
                    syncRepository.storeConflict(
                        SyncConflict(
                            entityId = conflictInfo.entityId,
                            entityType = conflictInfo.entityType.toStorageString(),
                            localData = localData,
                            remoteData = conflictInfo.remoteData ?: "{}",
                            localTimestamp = conflictInfo.localTimestamp,
                            remoteTimestamp = conflictInfo.remoteTimestamp
                        )
                    )
                    unresolvedCount++
                }
            }
        }

        return unresolvedCount
    }

    /**
     * Apply conflict resolution
     */
    private suspend fun applyConflictResolution(
        entityId: String,
        resolution: ConflictResolutionResult.Resolved
    ) {
        when (resolution.strategy) {
            ResolutionStrategy.KEEP_LOCAL -> {
                // Mark as clean but keep local version
                syncRepository.getSyncInfo(entityId)?.let { info ->
                    info.serverId?.let { serverId ->
                        syncRepository.markClean(entityId, serverId)
                    }
                }
            }

            ResolutionStrategy.KEEP_REMOTE -> {
                // TODO: Apply remote data when API integration is complete
                // For now, just mark as clean
                syncRepository.getSyncInfo(entityId)?.let { info ->
                    info.serverId?.let { serverId ->
                        syncRepository.markClean(entityId, serverId)
                    }
                }
            }

            ResolutionStrategy.MERGE -> {
                // TODO: Implement merge logic based on entity type
                // For now, treat as keep local
                syncRepository.getSyncInfo(entityId)?.let { info ->
                    info.serverId?.let { serverId ->
                        syncRepository.markClean(entityId, serverId)
                    }
                }
            }
        }
    }

    /**
     * Resolve a specific conflict manually
     */
    suspend fun resolveConflict(conflictId: Long, resolution: ConflictResolution) {
        val conflict = syncRepository.getConflictById(conflictId) ?: return

        when (resolution) {
            ConflictResolution.AcceptLocal -> {
                // Keep local version and mark as pinned
                syncRepository.setPinned(conflict.entityId, true)
                syncRepository.getSyncInfo(conflict.entityId)?.let { info ->
                    info.serverId?.let { serverId ->
                        syncRepository.markClean(conflict.entityId, serverId)
                    }
                }
            }

            ConflictResolution.AcceptRemote -> {
                // TODO: Apply remote data when API integration is complete
                syncRepository.getSyncInfo(conflict.entityId)?.let { info ->
                    info.serverId?.let { serverId ->
                        syncRepository.markClean(conflict.entityId, serverId)
                    }
                }
            }

            ConflictResolution.AcceptNewest -> {
                // Choose based on timestamp
                if (conflict.localTimestamp > conflict.remoteTimestamp) {
                    // Local is newer
                    syncRepository.getSyncInfo(conflict.entityId)?.let { info ->
                        info.serverId?.let { serverId ->
                            syncRepository.markClean(conflict.entityId, serverId)
                        }
                    }
                } else {
                    // Remote is newer - apply remote data
                    // TODO: Apply remote data when API integration is complete
                    syncRepository.getSyncInfo(conflict.entityId)?.let { info ->
                        info.serverId?.let { serverId ->
                            syncRepository.markClean(conflict.entityId, serverId)
                        }
                    }
                }
            }
        }

        // Remove the conflict from storage
        syncRepository.deleteConflict(conflictId)

        // Update sync state if no more conflicts
        val remainingConflicts = syncRepository.countConflicts()
        if (remainingConflicts == 0) {
            _syncState.value = SyncState.IDLE
        }
    }

    /**
     * Get current entity data as JSON
     */
    private suspend fun getEntityData(entityId: String, entityType: EntityType): String {
        // This will be replaced with actual entity queries
        return when (entityType) {
            EntityType.RECIPE -> "{\"type\":\"recipe\",\"id\":\"$entityId\"}"
            EntityType.INGREDIENT -> "{\"type\":\"ingredient\",\"id\":\"$entityId\"}"
            EntityType.UNIT -> "{\"type\":\"unit\",\"id\":\"$entityId\"}"
            EntityType.QUANTITY -> "{\"type\":\"quantity\",\"id\":\"$entityId\"}"
            EntityType.RECIPE_INGREDIENT -> "{\"type\":\"recipe_ingredient\",\"id\":\"$entityId\"}"
        }
    }

    /**
     * Convert API sync results to internal sync response format
     */
    private fun convertApiResultsToSyncResponse(results: List<ApiSyncResult>): SyncResponse {
        return SyncResponse(
            results = results.map { result ->
                SyncResultItem(
                    localId = result.localId,
                    serverId = result.serverId,
                    accepted = result.accepted,
                    hasConflict = result.hasConflict,
                    remoteData = result.conflictInfo?.remoteData?.toString(),
                    remoteTimestamp = result.conflictInfo?.remoteTimestamp
                )
            }
        )
    }

    /**
     * Get all unresolved conflicts
     */
    suspend fun getUnresolvedConflicts(): List<SyncConflict> {
        return syncRepository.getAllConflicts()
    }

    /**
     * Clear all conflicts for an entity
     */
    suspend fun clearConflicts(entityId: String) {
        syncRepository.deleteConflictsForEntity(entityId)
    }

    /**
     * Get sync status counts
     */
    suspend fun getSyncStatusCounts(): Map<SyncStatus, Int> {
        return syncRepository.countByStatus()
    }

    /**
     * Check if backend is available for sync
     */
    suspend fun checkConnectivity(): Boolean {
        return syncApiClient.isSyncServiceAvailable()
    }

    /**
     * Force a sync even if no dirty entities exist
     */
    suspend fun forceSyncAll() {
        // Mark all entities as dirty to force a full sync
        syncRepository.markAllDirty()
        performSync()
    }
}

/**
 * Sync state for UI
 */
enum class SyncState {
    IDLE,
    SYNCING,
    HAS_CONFLICTS,
    ERROR
}

/**
 * Conflict info for sync operations
 */
data class SyncConflictInfo(
    val entityId: String,
    val entityType: EntityType,
    val localTimestamp: Long,
    val remoteTimestamp: Long,
    val remoteData: String?,
    val isPinned: Boolean
)

/**
 * Conflict resolver interface
 */
interface ConflictResolver {
    suspend fun resolve(conflict: SyncConflictInfo): ConflictResolutionResult
}

/**
 * Result of conflict resolution
 */
sealed interface ConflictResolutionResult {
    data class Resolved(val strategy: ResolutionStrategy) : ConflictResolutionResult
    object RequiresUserInput : ConflictResolutionResult
}

/**
 * Resolution strategies
 */
enum class ResolutionStrategy {
    KEEP_LOCAL,
    KEEP_REMOTE,
    MERGE
}

/**
 * Default conflict resolver implementation
 */
class DefaultConflictResolver : ConflictResolver {
    override suspend fun resolve(conflict: SyncConflictInfo): ConflictResolutionResult {
        // Default strategy: newest wins
        return if (conflict.localTimestamp > conflict.remoteTimestamp) {
            ConflictResolutionResult.Resolved(ResolutionStrategy.KEEP_LOCAL)
        } else {
            ConflictResolutionResult.Resolved(ResolutionStrategy.KEEP_REMOTE)
        }
    }
}