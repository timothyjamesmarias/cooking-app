package com.timothymarias.cookingapp.shared.sync.models

import com.timothymarias.cookingapp.shared.util.currentTimeMillis
import kotlinx.serialization.Serializable

/**
 * Core sync models for tracking entity synchronization state
 */

/**
 * Sync action types that can be performed
 */
sealed interface SyncAction {
    data class AutoSync(val trigger: SyncTrigger) : SyncAction
    object ManualSync : SyncAction
    data class ResolveConflict(
        val conflictId: Long,
        val resolution: ConflictResolution
    ) : SyncAction
    object RetryFailed : SyncAction
}

/**
 * Triggers that can initiate auto-sync
 */
enum class SyncTrigger {
    APP_LAUNCH,     // MVP: Background job on boot
    APP_RESUME,     // Future: When returning to app
    TIMER,          // Future: Periodic sync
    NETWORK_CHANGE  // Future: When connectivity restored
}

/**
 * Ways to resolve a sync conflict
 */
sealed interface ConflictResolution {
    object AcceptLocal : ConflictResolution
    object AcceptRemote : ConflictResolution
    object AcceptNewest : ConflictResolution // Default
}

/**
 * Entity types that can be synced
 */
enum class EntityType {
    RECIPE,
    INGREDIENT,
    UNIT,
    QUANTITY,
    RECIPE_INGREDIENT
}

/**
 * Current sync status of an entity
 */
enum class SyncStatus {
    CLEAN,      // No local changes, synced with server
    DIRTY,      // Has local changes that need syncing
    SYNCING,    // Currently being synced
    CONFLICT,   // Has a conflict that needs resolution
    ERROR       // Sync failed, needs retry
}

/**
 * Sync information for an entity
 */
data class EntitySyncInfo(
    val localId: String,  // Renamed from entityId for consistency
    val entityType: EntityType,
    val serverId: Long? = null,
    val data: Map<String, kotlinx.serialization.json.JsonElement>,  // Entity data to sync
    val version: Int = 1,
    val lastModified: Long,  // Unix timestamp
    val checksum: String,
    val syncStatus: SyncStatus = SyncStatus.DIRTY,
    val isPinned: Boolean = false  // If user explicitly chose this version
)

// Legacy alias for backward compatibility
@Deprecated("Use localId instead", ReplaceWith("localId"))
val EntitySyncInfo.entityId: String
    get() = localId

/**
 * A sync conflict that needs resolution
 */
@Serializable
data class SyncConflict(
    val id: Long = 0,  // Will be set by database
    val entityId: String,
    val entityType: String,
    val localData: String,  // JSON representation
    val remoteData: String, // JSON representation
    val localTimestamp: Long,
    val remoteTimestamp: Long,
    val createdAt: Long = currentTimeMillis()
)

/**
 * Result of a sync operation
 */
data class SyncResult(
    val synced: Int,
    val conflicts: Int,
    val errors: List<String> = emptyList(),
    val timestamp: Long = currentTimeMillis()
)

/**
 * Data to be synced with the server
 */
@Serializable
data class SyncEntity(
    val localId: String,
    val serverId: Long? = null,
    val type: String,
    val data: String,  // JSON representation
    val version: Int,
    val timestamp: Long,
    val checksum: String
)

/**
 * Response from server after sync attempt
 */
@Serializable
data class SyncResponse(
    val results: List<SyncResultItem>
)

/**
 * Individual result for each synced entity
 */
@Serializable
data class SyncResultItem(
    val localId: String,
    val serverId: Long? = null,
    val accepted: Boolean,
    val hasConflict: Boolean = false,
    val remoteData: String? = null,  // JSON if conflict
    val remoteTimestamp: Long? = null,
    val errorMessage: String? = null
)

/**
 * Helper class to generate checksums for entities
 */
object ChecksumGenerator {
    fun generate(data: String): String {
        // Simple checksum for MVP - in production, use proper hashing
        return data.hashCode().toString()
    }

    fun generateForRecipe(id: String, name: String): String {
        return generate("$id:$name")
    }

    fun generateForIngredient(id: String, name: String): String {
        return generate("$id:$name")
    }

    fun generateForUnit(id: String, name: String, symbol: String, type: String, factor: Double): String {
        return generate("$id:$name:$symbol:$type:$factor")
    }

    fun generateForQuantity(id: String, amount: Double, unitId: String): String {
        return generate("$id:$amount:$unitId")
    }

    fun generateForRecipeIngredient(recipeId: String, ingredientId: String, quantityId: String?): String {
        return generate("$recipeId:$ingredientId:${quantityId ?: "null"}")
    }
}

/**
 * Extension functions for EntityType
 */
fun EntityType.toStorageString(): String = this.name

fun String.toEntityType(): EntityType? = try {
    EntityType.valueOf(this)
} catch (e: IllegalArgumentException) {
    null
}

/**
 * Extension functions for SyncStatus
 */
fun SyncStatus.toStorageString(): String = this.name

fun String.toSyncStatus(): SyncStatus? = try {
    SyncStatus.valueOf(this)
} catch (e: IllegalArgumentException) {
    null
}