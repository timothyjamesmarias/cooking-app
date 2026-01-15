package com.timothymarias.cookingapp.dto.sync

/**
 * Request DTO for sync operations
 */
data class SyncRequestDto(
    val entities: List<SyncEntityDto>
)

/**
 * Individual entity to be synced
 */
data class SyncEntityDto(
    val localId: String,
    val serverId: Long? = null,
    val type: String, // RECIPE, INGREDIENT, UNIT, QUANTITY, RECIPE_INGREDIENT
    val data: Map<String, Any?>, // Flexible data structure for different entity types
    val version: Int,
    val timestamp: Long,
    val checksum: String
)