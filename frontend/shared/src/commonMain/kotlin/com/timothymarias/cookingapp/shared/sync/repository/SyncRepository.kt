package com.timothymarias.cookingapp.shared.sync.repository

import com.timothymarias.cookingapp.shared.db.CookingDatabase
import com.timothymarias.cookingapp.shared.sync.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for managing sync state and conflicts
 */
class SyncRepository(
    private val database: CookingDatabase
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    // ===== SYNC INFO OPERATIONS =====

    /**
     * Initialize sync info for a new entity
     */
    suspend fun initializeSyncInfo(
        entityId: String,
        entityType: EntityType,
        checksum: String
    ) {
        database.sync_infoQueries.initializeSyncInfo(
            entity_id = entityId,
            entity_type = entityType.toStorageString(),
            last_modified = System.currentTimeMillis(),
            checksum = checksum
        )
    }

    /**
     * Update or insert sync info for an entity
     */
    suspend fun upsertSyncInfo(syncInfo: EntitySyncInfo) {
        database.sync_infoQueries.upsertSyncInfo(
            entity_id = syncInfo.entityId,
            entity_type = syncInfo.entityType.toStorageString(),
            server_id = syncInfo.serverId,
            local_version = syncInfo.localVersion.toLong(),
            last_modified = syncInfo.lastModified,
            checksum = syncInfo.checksum,
            sync_status = syncInfo.syncStatus.toStorageString(),
            is_pinned = if (syncInfo.isPinned) 1L else 0L
        )
    }

    /**
     * Get sync info for a specific entity
     */
    suspend fun getSyncInfo(entityId: String): EntitySyncInfo? {
        return database.sync_infoQueries.selectByEntityId(entityId)
            .executeAsOneOrNull()
            ?.toEntitySyncInfo()
    }

    /**
     * Get all dirty entities that need syncing
     */
    suspend fun getDirtyEntities(): List<EntitySyncInfo> {
        return database.sync_infoQueries.selectDirtyEntities()
            .executeAsList()
            .mapNotNull { it.toEntitySyncInfo() }
    }

    /**
     * Get all entities with conflicts
     */
    suspend fun getConflictedEntities(): List<EntitySyncInfo> {
        return database.sync_infoQueries.selectConflictedEntities()
            .executeAsList()
            .mapNotNull { it.toEntitySyncInfo() }
    }

    /**
     * Mark entity as clean after successful sync
     */
    suspend fun markClean(entityId: String, serverId: Long) {
        database.sync_infoQueries.markClean(
            server_id = serverId,
            entity_id = entityId
        )
    }

    /**
     * Mark entity as dirty when modified locally
     */
    suspend fun markDirty(
        entityId: String,
        checksum: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        database.sync_infoQueries.markDirty(
            last_modified = timestamp,
            checksum = checksum,
            entity_id = entityId
        )
    }

    /**
     * Mark entity as having a conflict
     */
    suspend fun markConflicted(entityId: String) {
        database.sync_infoQueries.markConflicted(entityId)
    }

    /**
     * Mark entity as currently syncing
     */
    suspend fun markSyncing(entityId: String) {
        database.sync_infoQueries.markSyncing(entityId)
    }

    /**
     * Mark entity as having an error
     */
    suspend fun markError(entityId: String) {
        database.sync_infoQueries.markError(entityId)
    }

    /**
     * Set pinned status for an entity
     */
    suspend fun setPinned(entityId: String, pinned: Boolean) {
        database.sync_infoQueries.setPinned(
            is_pinned = if (pinned) 1L else 0L,
            entity_id = entityId
        )
    }

    /**
     * Get entities by type and status
     */
    suspend fun getEntitiesByTypeAndStatus(
        entityType: EntityType,
        syncStatus: SyncStatus
    ): List<EntitySyncInfo> {
        return database.sync_infoQueries.selectByTypeAndStatus(
            entity_type = entityType.toStorageString(),
            sync_status = syncStatus.toStorageString()
        ).executeAsList().mapNotNull { it.toEntitySyncInfo() }
    }

    /**
     * Count entities by sync status
     */
    suspend fun countByStatus(): Map<SyncStatus, Int> {
        return database.sync_infoQueries.countByStatus()
            .executeAsList()
            .associate {
                (it.sync_status.toSyncStatus() ?: SyncStatus.ERROR) to it.status_count.toInt()
            }
    }

    /**
     * Delete sync info when entity is deleted
     */
    suspend fun deleteSyncInfo(entityId: String) {
        database.sync_infoQueries.deleteByEntityId(entityId)
    }

    // ===== CONFLICT OPERATIONS =====

    /**
     * Store a sync conflict
     */
    suspend fun storeConflict(conflict: SyncConflict) {
        database.sync_conflictsQueries.insertConflict(
            entity_id = conflict.entityId,
            entity_type = conflict.entityType,
            local_data = conflict.localData,
            remote_data = conflict.remoteData,
            local_timestamp = conflict.localTimestamp,
            remote_timestamp = conflict.remoteTimestamp,
            created_at = conflict.createdAt
        )
    }

    /**
     * Get all unresolved conflicts
     */
    suspend fun getAllConflicts(): List<SyncConflict> {
        return database.sync_conflictsQueries.selectAll()
            .executeAsList()
            .map { it.toSyncConflict() }
    }

    /**
     * Get conflict by ID
     */
    suspend fun getConflictById(id: Long): SyncConflict? {
        return database.sync_conflictsQueries.selectById(id)
            .executeAsOneOrNull()
            ?.toSyncConflict()
    }

    /**
     * Get conflicts for a specific entity
     */
    suspend fun getConflictsForEntity(entityId: String): List<SyncConflict> {
        return database.sync_conflictsQueries.selectByEntityId(entityId)
            .executeAsList()
            .map { it.toSyncConflict() }
    }

    /**
     * Delete a conflict after resolution
     */
    suspend fun deleteConflict(id: Long) {
        database.sync_conflictsQueries.deleteById(id)
    }

    /**
     * Delete all conflicts for an entity
     */
    suspend fun deleteConflictsForEntity(entityId: String) {
        database.sync_conflictsQueries.deleteByEntityId(entityId)
    }

    /**
     * Check if entity has conflicts
     */
    suspend fun hasConflicts(entityId: String): Boolean {
        return database.sync_conflictsQueries.hasConflicts(entityId)
            .executeAsOne()
    }

    /**
     * Count unresolved conflicts
     */
    suspend fun countConflicts(): Int {
        return database.sync_conflictsQueries.countConflicts()
            .executeAsOne()
            .toInt()
    }

    // ===== HELPER FUNCTIONS =====

    /**
     * Prepare entities for sync
     */
    suspend fun prepareSyncBatch(): List<SyncEntity> {
        val dirtyEntities = getDirtyEntities()
        return dirtyEntities.map { syncInfo ->
            // Get the actual entity data based on type
            val entityData = when (syncInfo.entityType) {
                EntityType.RECIPE -> getRecipeData(syncInfo.entityId)
                EntityType.INGREDIENT -> getIngredientData(syncInfo.entityId)
                EntityType.UNIT -> getUnitData(syncInfo.entityId)
                EntityType.QUANTITY -> getQuantityData(syncInfo.entityId)
                EntityType.RECIPE_INGREDIENT -> getRecipeIngredientData(syncInfo.entityId)
            }

            SyncEntity(
                localId = syncInfo.entityId,
                serverId = syncInfo.serverId,
                type = syncInfo.entityType.toStorageString(),
                data = entityData,
                version = syncInfo.localVersion,
                timestamp = syncInfo.lastModified,
                checksum = syncInfo.checksum
            )
        }
    }

    private suspend fun getRecipeData(entityId: String): String {
        val recipe = database.recipesQueries.selectById(entityId).executeAsOneOrNull()
        return json.encodeToString(
            mapOf(
                "local_id" to recipe?.local_id,
                "name" to recipe?.name
            )
        )
    }

    private suspend fun getIngredientData(entityId: String): String {
        val ingredient = database.ingredientsQueries.selectById(entityId).executeAsOneOrNull()
        return json.encodeToString(
            mapOf(
                "local_id" to ingredient?.local_id,
                "name" to ingredient?.name
            )
        )
    }

    private suspend fun getUnitData(entityId: String): String {
        val unit = database.unitsQueries.selectById(entityId).executeAsOneOrNull()
        return json.encodeToString(
            mapOf(
                "local_id" to unit?.local_id,
                "name" to unit?.name,
                "symbol" to unit?.symbol,
                "measurement_type" to unit?.measurement_type,
                "base_conversion_factor" to unit?.base_conversion_factor
            )
        )
    }

    private suspend fun getQuantityData(entityId: String): String {
        val quantity = database.quantitiesQueries.selectById(entityId).executeAsOneOrNull()
        return json.encodeToString(
            mapOf(
                "local_id" to quantity?.local_id,
                "amount" to quantity?.amount,
                "unit_id" to quantity?.unit_id
            )
        )
    }

    private suspend fun getRecipeIngredientData(entityId: String): String {
        // entityId format: "recipeId:ingredientId"
        val parts = entityId.split(":")
        if (parts.size != 2) return "{}"

        val (recipeId, ingredientId) = parts
        // Query would need to be added to recipes.sq
        return json.encodeToString(
            mapOf(
                "recipe_id" to recipeId,
                "ingredient_id" to ingredientId,
                "quantity_id" to null // Would need actual query
            )
        )
    }
}

// Extension functions to convert database types to domain models

private fun com.timothymarias.cookingapp.shared.db.migrations.Sync_info.toEntitySyncInfo(): EntitySyncInfo? {
    val entityType = entity_type.toEntityType() ?: return null
    val syncStatus = sync_status.toSyncStatus() ?: return null

    return EntitySyncInfo(
        entityId = entity_id,
        entityType = entityType,
        serverId = server_id,
        localVersion = local_version.toInt(),
        lastModified = last_modified,
        checksum = checksum,
        syncStatus = syncStatus,
        isPinned = is_pinned == 1L
    )
}

private fun com.timothymarias.cookingapp.shared.db.migrations.Sync_conflicts.toSyncConflict(): SyncConflict {
    return SyncConflict(
        id = id,
        entityId = entity_id,
        entityType = entity_type,
        localData = local_data,
        remoteData = remote_data,
        localTimestamp = local_timestamp,
        remoteTimestamp = remote_timestamp,
        createdAt = created_at
    )
}