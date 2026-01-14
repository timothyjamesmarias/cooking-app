package com.timothymarias.cookingapp.shared.sync.repository

import com.timothymarias.cookingapp.shared.data.repository.recipe.RecipeRepository
import com.timothymarias.cookingapp.shared.domain.model.Recipe
import com.timothymarias.cookingapp.shared.sync.models.ChecksumGenerator
import com.timothymarias.cookingapp.shared.sync.models.EntityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Sync-aware wrapper for RecipeRepository that tracks changes
 */
class SyncAwareRecipeRepository(
    private val recipeRepository: RecipeRepository,
    private val syncRepository: SyncRepository
) {
    /**
     * Create a new recipe with sync tracking
     */
    suspend fun createRecipe(name: String): Recipe {
        val localId = UUID.randomUUID().toString()
        val recipe = Recipe(localId = localId, name = name)

        // Create the recipe
        val createdRecipe = recipeRepository.create(recipe)

        // Initialize sync tracking
        val checksum = ChecksumGenerator.generateForRecipe(localId, name)
        syncRepository.initializeSyncInfo(
            entityId = localId,
            entityType = EntityType.RECIPE,
            checksum = checksum
        )

        return createdRecipe
    }

    /**
     * Update a recipe and mark it as dirty for sync
     */
    suspend fun updateRecipe(localId: String, name: String): Recipe {
        // Update the recipe
        val updatedRecipe = recipeRepository.updateName(localId, name)

        // Mark as dirty for sync
        val checksum = ChecksumGenerator.generateForRecipe(localId, name)
        syncRepository.markDirty(
            entityId = localId,
            checksum = checksum
        )

        return updatedRecipe
    }

    /**
     * Delete a recipe and clean up sync tracking
     */
    suspend fun deleteRecipe(localId: String) {
        // Delete the recipe
        recipeRepository.delete(localId)

        // Clean up sync tracking
        syncRepository.deleteSyncInfo(localId)
        syncRepository.deleteConflictsForEntity(localId)
    }

    /**
     * Watch all recipes (pass-through to original repository)
     */
    fun watchAll(): Flow<List<Recipe>> {
        return recipeRepository.watchAll()
    }

    /**
     * Watch a recipe by ID (pass-through to original repository)
     */
    fun watchById(localId: String): Flow<Recipe?> {
        return recipeRepository.watchById(localId)
    }

    /**
     * Get all recipes as a snapshot
     */
    suspend fun getAll(): List<Recipe> {
        return watchAll().first()
    }

    /**
     * Get a recipe by ID as a snapshot
     */
    suspend fun getById(localId: String): Recipe? {
        return watchById(localId).first()
    }

    /**
     * Add an ingredient to a recipe with sync tracking
     */
    suspend fun addIngredientToRecipe(
        recipeId: String,
        ingredientId: String,
        quantityId: String? = null
    ) {
        // Check if already assigned
        if (!recipeRepository.isIngredientAssigned(recipeId, ingredientId)) {
            // Add the ingredient
            recipeRepository.assignIngredient(recipeId, ingredientId)

            // If quantity provided, update it
            if (quantityId != null) {
                recipeRepository.updateIngredientQuantity(recipeId, ingredientId, quantityId)
            }

            // Track the relationship for sync
            val relationshipId = "$recipeId:$ingredientId"
            val checksum = ChecksumGenerator.generateForRecipeIngredient(
                recipeId,
                ingredientId,
                quantityId
            )

            // Check if sync info exists, if not initialize it
            val existingSyncInfo = syncRepository.getSyncInfo(relationshipId)
            if (existingSyncInfo == null) {
                syncRepository.initializeSyncInfo(
                    entityId = relationshipId,
                    entityType = EntityType.RECIPE_INGREDIENT,
                    checksum = checksum
                )
            } else {
                syncRepository.markDirty(
                    entityId = relationshipId,
                    checksum = checksum
                )
            }

            // Also mark the recipe as dirty since its relationships changed
            val recipe = getById(recipeId)
            if (recipe != null) {
                val recipeChecksum = ChecksumGenerator.generateForRecipe(recipeId, recipe.name)
                syncRepository.markDirty(
                    entityId = recipeId,
                    checksum = recipeChecksum
                )
            }
        }
    }

    /**
     * Remove an ingredient from a recipe with sync tracking
     */
    suspend fun removeIngredientFromRecipe(recipeId: String, ingredientId: String) {
        // Remove the ingredient
        recipeRepository.removeIngredient(recipeId, ingredientId)

        // Clean up sync tracking for the relationship
        val relationshipId = "$recipeId:$ingredientId"
        syncRepository.deleteSyncInfo(relationshipId)
        syncRepository.deleteConflictsForEntity(relationshipId)

        // Mark the recipe as dirty
        val recipe = getById(recipeId)
        if (recipe != null) {
            val checksum = ChecksumGenerator.generateForRecipe(recipeId, recipe.name)
            syncRepository.markDirty(
                entityId = recipeId,
                checksum = checksum
            )
        }
    }

    /**
     * Update ingredient quantity with sync tracking
     */
    suspend fun updateIngredientQuantity(
        recipeId: String,
        ingredientId: String,
        quantityId: String?
    ) {
        // Update the quantity
        recipeRepository.updateIngredientQuantity(recipeId, ingredientId, quantityId)

        // Mark the relationship as dirty
        val relationshipId = "$recipeId:$ingredientId"
        val checksum = ChecksumGenerator.generateForRecipeIngredient(
            recipeId,
            ingredientId,
            quantityId
        )
        syncRepository.markDirty(
            entityId = relationshipId,
            checksum = checksum
        )
    }

    /**
     * Check if a recipe has sync conflicts
     */
    suspend fun hasConflicts(recipeId: String): Boolean {
        return syncRepository.hasConflicts(recipeId)
    }

    /**
     * Get sync info for a recipe
     */
    suspend fun getSyncInfo(recipeId: String) = syncRepository.getSyncInfo(recipeId)
}