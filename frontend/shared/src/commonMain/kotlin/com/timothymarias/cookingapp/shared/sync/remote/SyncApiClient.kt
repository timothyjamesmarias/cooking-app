package com.timothymarias.cookingapp.shared.sync.remote

import com.timothymarias.cookingapp.shared.data.remote.ApiService
import com.timothymarias.cookingapp.shared.data.remote.dto.*
import com.timothymarias.cookingapp.shared.sync.models.*
import com.timothymarias.cookingapp.shared.sync.SyncConflictInfo
import kotlinx.serialization.json.JsonPrimitive

/**
 * Client for sync operations with the backend API.
 * Handles conversion between sync models and DTOs.
 */
class SyncApiClient(
    private val apiService: ApiService = ApiService()
) {
    /**
     * Performs a sync operation with the backend
     */
    suspend fun performSync(entities: List<EntitySyncInfo>): Result<List<SyncResult>> {
        // Convert EntitySyncInfo to SyncEntityDto
        val syncEntities = entities.map { entity ->
            SyncEntityDto(
                localId = entity.localId,
                serverId = entity.serverId,
                type = entity.entityType.name,
                data = entity.data.mapValues { it.value.toString() },
                version = entity.version,
                timestamp = entity.lastModified,
                checksum = entity.checksum
            )
        }

        val request = SyncRequestDto(entities = syncEntities)

        // Make the API call
        return apiService.sync(request).map { response ->
            // Convert SyncResultDto to SyncResult
            response.results.map { result ->
                SyncResult(
                    localId = result.localId,
                    serverId = result.serverId,
                    accepted = result.accepted,
                    hasConflict = result.hasConflict,
                    conflictInfo = if (result.hasConflict) {
                        val entity = entities.find { it.localId == result.localId }
                        SyncConflictInfo(
                            entityId = result.localId,
                            entityType = entity?.entityType ?: EntityType.RECIPE,
                            localTimestamp = entity?.lastModified ?: 0L,
                            remoteTimestamp = result.remoteTimestamp ?: 0L,
                            remoteData = result.remoteData?.let {
                                kotlinx.serialization.json.Json.encodeToString(
                                    kotlinx.serialization.json.JsonObject(
                                        it.mapValues { entry -> JsonPrimitive(entry.value) }
                                    )
                                )
                            },
                            isPinned = entity?.isPinned ?: false
                        )
                    } else null,
                    errorMessage = result.errorMessage
                )
            }
        }
    }

    /**
     * Checks if the sync service is available
     */
    suspend fun isSyncServiceAvailable(): Boolean {
        return apiService.checkSyncHealth()
    }

    /**
     * Checks general backend connectivity
     */
    suspend fun isBackendAvailable(): Boolean {
        return apiService.isBackendAvailable()
    }

    /**
     * Closes the underlying HTTP client
     */
    fun close() {
        apiService.close()
    }
}

/**
 * Result of a sync operation for an individual entity
 */
data class SyncResult(
    val localId: String,
    val serverId: Long? = null,
    val accepted: Boolean,
    val hasConflict: Boolean = false,
    val conflictInfo: SyncConflictInfo? = null,
    val errorMessage: String? = null
)