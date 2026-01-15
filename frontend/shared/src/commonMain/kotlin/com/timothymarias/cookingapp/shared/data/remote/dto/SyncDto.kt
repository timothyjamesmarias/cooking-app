package com.timothymarias.cookingapp.shared.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request DTO for sync operations - matches backend SyncRequestDto
 */
@Serializable
data class SyncRequestDto(
    val entities: List<SyncEntityDto>
)

/**
 * Individual entity to be synced - matches backend SyncEntityDto
 */
@Serializable
data class SyncEntityDto(
    val localId: String,
    val serverId: Long? = null,
    val type: String, // RECIPE, INGREDIENT, UNIT, QUANTITY, RECIPE_INGREDIENT
    val data: Map<String, String>, // Simplified to String for serialization
    val version: Int,
    val timestamp: Long,
    val checksum: String
)

/**
 * Response DTO for sync operations - matches backend SyncResponseDto
 */
@Serializable
data class SyncResponseDto(
    val results: List<SyncResultDto>
)

/**
 * Result for each synced entity - matches backend SyncResultDto
 */
@Serializable
data class SyncResultDto(
    val localId: String,
    val serverId: Long? = null,
    val accepted: Boolean,
    val hasConflict: Boolean = false,
    val remoteData: Map<String, String>? = null, // Simplified to String for serialization
    val remoteTimestamp: Long? = null,
    val errorMessage: String? = null
)