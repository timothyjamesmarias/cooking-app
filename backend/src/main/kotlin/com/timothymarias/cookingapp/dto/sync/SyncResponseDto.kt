package com.timothymarias.cookingapp.dto.sync

/**
 * Response DTO for sync operations
 */
data class SyncResponseDto(
    val results: List<SyncResultDto>
)

/**
 * Result for each synced entity
 */
data class SyncResultDto(
    val localId: String,
    val serverId: Long? = null,
    val accepted: Boolean,
    val hasConflict: Boolean = false,
    val remoteData: Map<String, Any?>? = null, // Data from server if conflict
    val remoteTimestamp: Long? = null,
    val errorMessage: String? = null
)