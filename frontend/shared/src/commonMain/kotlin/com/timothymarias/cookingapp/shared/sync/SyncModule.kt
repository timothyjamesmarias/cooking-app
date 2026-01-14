package com.timothymarias.cookingapp.shared.sync

import com.timothymarias.cookingapp.shared.data.repository.ingredient.IngredientRepository
import com.timothymarias.cookingapp.shared.data.repository.quantity.QuantityRepository
import com.timothymarias.cookingapp.shared.data.repository.recipe.RecipeRepository
import com.timothymarias.cookingapp.shared.data.repository.unit.UnitRepository
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import com.timothymarias.cookingapp.shared.sync.models.ChecksumGenerator
import com.timothymarias.cookingapp.shared.sync.models.EntityType
import com.timothymarias.cookingapp.shared.sync.repository.*
import kotlinx.coroutines.flow.first

/**
 * Module that provides sync-aware components for the application
 */
class SyncModule(
    private val database: CookingDatabase,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val unitRepository: UnitRepository,
    private val quantityRepository: QuantityRepository
) {
    // Core sync components
    val syncRepository: SyncRepository by lazy {
        SyncRepository(database)
    }

    val syncEngine: SyncEngine by lazy {
        SyncEngine(
            syncRepository = syncRepository,
            conflictResolver = DefaultConflictResolver()
        )
    }

    // Sync-aware repositories
    val syncAwareRecipeRepository: SyncAwareRecipeRepository by lazy {
        SyncAwareRecipeRepository(
            recipeRepository = recipeRepository,
            syncRepository = syncRepository
        )
    }

    val syncAwareIngredientRepository: SyncAwareIngredientRepository by lazy {
        SyncAwareIngredientRepository(
            ingredientRepository = ingredientRepository,
            syncRepository = syncRepository
        )
    }

    val syncAwareUnitRepository: SyncAwareUnitRepository by lazy {
        SyncAwareUnitRepository(
            unitRepository = unitRepository,
            syncRepository = syncRepository
        )
    }

    val syncAwareQuantityRepository: SyncAwareQuantityRepository by lazy {
        SyncAwareQuantityRepository(
            quantityRepository = quantityRepository,
            syncRepository = syncRepository
        )
    }

    /**
     * Initialize sync for all existing entities
     * Call this once during app setup if needed
     */
    suspend fun initializeExistingEntities() {
        // Initialize sync info for existing recipes
        val recipes = recipeRepository.watchAll().first()
        recipes.forEach { recipe ->
            val syncInfo = syncRepository.getSyncInfo(recipe.localId)
            if (syncInfo == null) {
                syncRepository.initializeSyncInfo(
                    entityId = recipe.localId,
                    entityType = EntityType.RECIPE,
                    checksum = ChecksumGenerator.generateForRecipe(recipe.localId, recipe.name)
                )
            }
        }

        // Initialize sync info for existing ingredients
        val ingredients = ingredientRepository.getAll()
        ingredients.forEach { ingredient ->
            val syncInfo = syncRepository.getSyncInfo(ingredient.localId)
            if (syncInfo == null) {
                syncRepository.initializeSyncInfo(
                    entityId = ingredient.localId,
                    entityType = EntityType.INGREDIENT,
                    checksum = ChecksumGenerator.generateForIngredient(ingredient.localId, ingredient.name)
                )
            }
        }

        // Initialize sync info for existing units
        unitRepository.getAll().forEach { unit ->
            val syncInfo = syncRepository.getSyncInfo(unit.localId)
            if (syncInfo == null) {
                syncRepository.initializeSyncInfo(
                    entityId = unit.localId,
                    entityType = EntityType.UNIT,
                    checksum = ChecksumGenerator.generateForUnit(
                        unit.localId,
                        unit.name,
                        unit.symbol,
                        unit.measurementType.name,
                        unit.baseConversionFactor
                    )
                )
            }
        }

        // Initialize sync info for existing quantities
        quantityRepository.getAll().forEach { quantity ->
            val syncInfo = syncRepository.getSyncInfo(quantity.localId)
            if (syncInfo == null) {
                syncRepository.initializeSyncInfo(
                    entityId = quantity.localId,
                    entityType = EntityType.QUANTITY,
                    checksum = ChecksumGenerator.generateForQuantity(
                        quantity.localId,
                        quantity.amount,
                        quantity.unitId
                    )
                )
            }
        }
    }

    /**
     * Factory method to create a configured SyncModule
     */
    companion object {
        fun create(
            database: CookingDatabase,
            recipeRepository: RecipeRepository,
            ingredientRepository: IngredientRepository,
            unitRepository: UnitRepository,
            quantityRepository: QuantityRepository
        ): SyncModule {
            return SyncModule(
                database = database,
                recipeRepository = recipeRepository,
                ingredientRepository = ingredientRepository,
                unitRepository = unitRepository,
                quantityRepository = quantityRepository
            )
        }
    }
}

/**
 * Extension to simplify access to sync components from other modules
 */
data class SyncComponents(
    val syncEngine: SyncEngine,
    val syncRepository: SyncRepository,
    val recipeRepository: SyncAwareRecipeRepository,
    val ingredientRepository: SyncAwareIngredientRepository,
    val unitRepository: SyncAwareUnitRepository,
    val quantityRepository: SyncAwareQuantityRepository
)

fun SyncModule.getComponents(): SyncComponents {
    return SyncComponents(
        syncEngine = syncEngine,
        syncRepository = syncRepository,
        recipeRepository = syncAwareRecipeRepository,
        ingredientRepository = syncAwareIngredientRepository,
        unitRepository = syncAwareUnitRepository,
        quantityRepository = syncAwareQuantityRepository
    )
}