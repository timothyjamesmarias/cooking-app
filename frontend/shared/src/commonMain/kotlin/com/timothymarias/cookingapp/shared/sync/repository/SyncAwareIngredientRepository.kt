package com.timothymarias.cookingapp.shared.sync.repository

import com.timothymarias.cookingapp.shared.data.repository.ingredient.IngredientRepository
import com.timothymarias.cookingapp.shared.domain.model.Ingredient
import com.timothymarias.cookingapp.shared.sync.models.ChecksumGenerator
import com.timothymarias.cookingapp.shared.sync.models.EntityType
import com.timothymarias.cookingapp.shared.util.randomUUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Sync-aware wrapper for IngredientRepository that tracks changes
 */
class SyncAwareIngredientRepository(
    private val ingredientRepository: IngredientRepository,
    private val syncRepository: SyncRepository
) {
    /**
     * Create a new ingredient with sync tracking
     */
    suspend fun createIngredient(name: String): Ingredient {
        val localId = randomUUID()
        val ingredient = Ingredient(localId = localId, name = name)

        // Create the ingredient
        val createdIngredient = ingredientRepository.create(ingredient)

        // Initialize sync tracking
        val checksum = ChecksumGenerator.generateForIngredient(localId, name)
        syncRepository.initializeSyncInfo(
            entityId = localId,
            entityType = EntityType.INGREDIENT,
            checksum = checksum
        )

        return createdIngredient
    }

    /**
     * Update an ingredient and mark it as dirty for sync
     */
    suspend fun updateIngredient(localId: String, name: String): Ingredient {
        // Update the ingredient
        val updatedIngredient = ingredientRepository.updateName(localId, name)

        // Mark as dirty for sync
        val checksum = ChecksumGenerator.generateForIngredient(localId, name)
        syncRepository.markDirty(
            entityId = localId,
            checksum = checksum
        )

        return updatedIngredient
    }

    /**
     * Delete an ingredient and clean up sync tracking
     */
    suspend fun deleteIngredient(localId: String) {
        // Delete the ingredient
        ingredientRepository.delete(localId)

        // Clean up sync tracking
        syncRepository.deleteSyncInfo(localId)
        syncRepository.deleteConflictsForEntity(localId)
    }

    /**
     * Watch all ingredients (pass-through to original repository)
     */
    fun watchAll(): Flow<List<Ingredient>> {
        return ingredientRepository.watchAll()
    }

    /**
     * Watch an ingredient by ID (pass-through to original repository)
     */
    fun watchById(localId: String): Flow<Ingredient?> {
        return ingredientRepository.watchById(localId)
    }

    /**
     * Get all ingredients (pass-through to original repository)
     */
    suspend fun getAll(): List<Ingredient> {
        return ingredientRepository.getAll()
    }

    /**
     * Watch ingredients by query (pass-through to original repository)
     */
    fun watchByQuery(query: String): Flow<List<Ingredient>> {
        return ingredientRepository.watchByQuery(query)
    }

    /**
     * Check if an ingredient has sync conflicts
     */
    suspend fun hasConflicts(ingredientId: String): Boolean {
        return syncRepository.hasConflicts(ingredientId)
    }

    /**
     * Get sync info for an ingredient
     */
    suspend fun getSyncInfo(ingredientId: String) = syncRepository.getSyncInfo(ingredientId)
}