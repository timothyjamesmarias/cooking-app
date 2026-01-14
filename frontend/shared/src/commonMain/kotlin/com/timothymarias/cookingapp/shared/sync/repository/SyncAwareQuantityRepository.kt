package com.timothymarias.cookingapp.shared.sync.repository

import com.timothymarias.cookingapp.shared.data.repository.quantity.QuantityRepository
import com.timothymarias.cookingapp.shared.domain.model.Quantity
import com.timothymarias.cookingapp.shared.sync.models.ChecksumGenerator
import com.timothymarias.cookingapp.shared.sync.models.EntityType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Sync-aware wrapper for QuantityRepository that tracks changes
 */
class SyncAwareQuantityRepository(
    private val quantityRepository: QuantityRepository,
    private val syncRepository: SyncRepository
) {
    /**
     * Create a new quantity with sync tracking
     */
    suspend fun createQuantity(amount: Double, unitId: String): Quantity {
        val localId = UUID.randomUUID().toString()
        val quantity = Quantity(
            localId = localId,
            amount = amount,
            unitId = unitId
        )

        // Create the quantity
        val createdQuantity = quantityRepository.create(quantity)

        // Initialize sync tracking
        val checksum = ChecksumGenerator.generateForQuantity(localId, amount, unitId)
        syncRepository.initializeSyncInfo(
            entityId = localId,
            entityType = EntityType.QUANTITY,
            checksum = checksum
        )

        return createdQuantity
    }

    /**
     * Update a quantity and mark it as dirty for sync
     */
    suspend fun updateQuantity(quantity: Quantity): Quantity {
        // Update the quantity
        val updatedQuantity = quantityRepository.update(quantity)

        // Mark as dirty for sync
        val checksum = ChecksumGenerator.generateForQuantity(
            quantity.localId,
            quantity.amount,
            quantity.unitId
        )
        syncRepository.markDirty(
            entityId = quantity.localId,
            checksum = checksum
        )

        // Also mark any recipe-ingredient relationships that use this quantity as dirty
        // This ensures the relationship is synced when the quantity changes
        // Note: This would require a query to find all recipe_ingredients using this quantity
        // For now, we'll leave this as a TODO for when the relationship queries are implemented

        return updatedQuantity
    }

    /**
     * Delete a quantity and clean up sync tracking
     */
    suspend fun deleteQuantity(localId: String) {
        // Delete the quantity
        quantityRepository.delete(localId)

        // Clean up sync tracking
        syncRepository.deleteSyncInfo(localId)
        syncRepository.deleteConflictsForEntity(localId)
    }

    // ===== Pass-through methods (no sync tracking needed for reads) =====

    fun watchAll(): Flow<List<Quantity>> = quantityRepository.watchAll()
    fun watchById(localId: String): Flow<Quantity?> = quantityRepository.watchById(localId)

    suspend fun getAll(): List<Quantity> = quantityRepository.getAll()
    suspend fun getById(localId: String): Quantity? = quantityRepository.getById(localId)
    suspend fun getByUnitId(unitId: String): List<Quantity> = quantityRepository.getByUnitId(unitId)

    /**
     * Check if a quantity has sync conflicts
     */
    suspend fun hasConflicts(quantityId: String): Boolean {
        return syncRepository.hasConflicts(quantityId)
    }

    /**
     * Get sync info for a quantity
     */
    suspend fun getSyncInfo(quantityId: String) = syncRepository.getSyncInfo(quantityId)
}