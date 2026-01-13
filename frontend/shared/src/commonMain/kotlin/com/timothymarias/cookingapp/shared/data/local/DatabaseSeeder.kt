package com.timothymarias.cookingapp.shared.data.local

import com.timothymarias.cookingapp.shared.data.repository.ingredient.IngredientRepository
import com.timothymarias.cookingapp.shared.data.repository.recipe.RecipeRepository
import com.timothymarias.cookingapp.shared.data.repository.unit.UnitRepository
import com.timothymarias.cookingapp.shared.domain.model.Ingredient
import com.timothymarias.cookingapp.shared.domain.model.MeasurementType
import com.timothymarias.cookingapp.shared.domain.model.Recipe
import com.timothymarias.cookingapp.shared.domain.model.Unit
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

/**
 * Seeds the database with reference data and test data.
 *
 * **Production Seeding** (always runs):
 * - Common measurement units
 *
 * **Development Seeding** (debug builds only):
 * - Sample recipes
 * - Common ingredients
 *
 * Keeps seed data in code for type safety and version control.
 * See BuildConfig.isDebug for environment detection.
 */
object DatabaseSeeder {

    /**
     * Seeds production data that should exist in all builds.
     * Call this on app initialization regardless of environment.
     */
    suspend fun seedProduction(unitRepository: UnitRepository) {
        seedUnitsIfEmpty(unitRepository)
    }

    /**
     * Seeds development/test data for easier testing during development.
     * Only call this in debug builds (check BuildConfig.isDebug).
     */
    suspend fun seedDevelopment(
        recipeRepository: RecipeRepository,
        ingredientRepository: IngredientRepository,
        unitRepository: UnitRepository
    ) {
        // First, ensure production data is seeded
        seedProduction(unitRepository)

        // Then seed dev-only data
        seedTestIngredientsIfEmpty(ingredientRepository)
        seedTestRecipesIfEmpty(recipeRepository, ingredientRepository)
    }

    /**
     * Seeds common units if the units table is empty.
     * Safe to call multiple times; only inserts if table is empty.
     */
    private suspend fun seedUnitsIfEmpty(unitRepository: UnitRepository): Boolean {
        val existing = unitRepository.getAll()
        if (existing.isNotEmpty()) {
            return false // Already seeded
        }

        for (unit in commonUnits) {
            unitRepository.create(unit)
        }
        return true
    }

    /**
     * Seeds common ingredients for testing if the table is empty.
     */
    private suspend fun seedTestIngredientsIfEmpty(ingredientRepository: IngredientRepository): Boolean {
        val existing = ingredientRepository.getAll()
        if (existing.isNotEmpty()) {
            return false
        }

        for (ingredient in testIngredients) {
            ingredientRepository.create(ingredient)
        }
        return true
    }

    /**
     * Seeds sample recipes for testing if the table is empty.
     * Assigns random ingredients to each recipe.
     */
    private suspend fun seedTestRecipesIfEmpty(
        recipeRepository: RecipeRepository,
        ingredientRepository: IngredientRepository
    ): Boolean {
        // Simple check: if we already have recipes, skip
        val existingRecipes = ingredientRepository.getAll()
        // Note: For better check, we'd use recipeRepository, but getAll() isn't in the interface
        // For now, we'll just attempt to create and rely on unique constraints

        val allIngredients = ingredientRepository.getAll()
        if (allIngredients.isEmpty()) {
            // Can't create meaningful test recipes without ingredients
            return false
        }

        for (recipe in testRecipes) {
            val created = recipeRepository.create(recipe)

            // Assign 2-4 random ingredients to each recipe
            val ingredientsToAssign = allIngredients.shuffled().take((2..4).random())
            for (ingredient in ingredientsToAssign) {
                recipeRepository.assignIngredient(created.localId, ingredient.localId)
            }
        }
        return true
    }

    /**
     * Common units pre-populated in the app.
     * Base units: gram (WEIGHT), milliliter (VOLUME), whole (COUNT)
     *
     * Conversion factors convert TO base unit:
     * - 1 kilogram = 1000 grams → factor = 1000
     * - 1 cup = 236.588 milliliters → factor = 236.588
     */
    private val commonUnits = listOf(
        // WEIGHT units (base: gram)
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "gram",
            symbol = "g",
            measurementType = MeasurementType.WEIGHT,
            baseConversionFactor = 1.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "kilogram",
            symbol = "kg",
            measurementType = MeasurementType.WEIGHT,
            baseConversionFactor = 1000.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "ounce",
            symbol = "oz",
            measurementType = MeasurementType.WEIGHT,
            baseConversionFactor = 28.3495
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "pound",
            symbol = "lb",
            measurementType = MeasurementType.WEIGHT,
            baseConversionFactor = 453.592
        ),

        // VOLUME units (base: milliliter)
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "milliliter",
            symbol = "ml",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 1.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "liter",
            symbol = "L",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 1000.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "teaspoon",
            symbol = "tsp",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 4.92892
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "tablespoon",
            symbol = "tbsp",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 14.7868
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "cup",
            symbol = "c",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 236.588
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "pint",
            symbol = "pt",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 473.176
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "quart",
            symbol = "qt",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 946.353
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "gallon",
            symbol = "gal",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 3785.41
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "fluid ounce",
            symbol = "fl oz",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 29.5735
        ),

        // COUNT units (base: whole/piece)
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "whole",
            symbol = "",
            measurementType = MeasurementType.COUNT,
            baseConversionFactor = 1.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "piece",
            symbol = "pc",
            measurementType = MeasurementType.COUNT,
            baseConversionFactor = 1.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "dozen",
            symbol = "dz",
            measurementType = MeasurementType.COUNT,
            baseConversionFactor = 12.0
        ),
    )

    /**
     * Test ingredients for development builds.
     * Covers common pantry staples across multiple categories.
     */
    private val testIngredients = listOf(
        Ingredient(localId = "", name = "All-Purpose Flour"),
        Ingredient(localId = "", name = "Sugar"),
        Ingredient(localId = "", name = "Salt"),
        Ingredient(localId = "", name = "Butter"),
        Ingredient(localId = "", name = "Eggs"),
        Ingredient(localId = "", name = "Milk"),
        Ingredient(localId = "", name = "Olive Oil"),
        Ingredient(localId = "", name = "Chicken Breast"),
        Ingredient(localId = "", name = "Onion"),
        Ingredient(localId = "", name = "Garlic"),
        Ingredient(localId = "", name = "Tomatoes"),
        Ingredient(localId = "", name = "Basil"),
        Ingredient(localId = "", name = "Black Pepper"),
        Ingredient(localId = "", name = "Parmesan Cheese"),
        Ingredient(localId = "", name = "Pasta"),
        Ingredient(localId = "", name = "Rice"),
        Ingredient(localId = "", name = "Carrots"),
        Ingredient(localId = "", name = "Celery"),
        Ingredient(localId = "", name = "Potatoes"),
        Ingredient(localId = "", name = "Broccoli"),
    )

    /**
     * Test recipes for development builds.
     * Ingredients will be randomly assigned during seeding.
     */
    private val testRecipes = listOf(
        Recipe(localId = "", name = "Classic Chocolate Chip Cookies"),
        Recipe(localId = "", name = "Spaghetti Carbonara"),
        Recipe(localId = "", name = "Chicken Stir Fry"),
        Recipe(localId = "", name = "Caesar Salad"),
        Recipe(localId = "", name = "Vegetable Soup"),
        Recipe(localId = "", name = "Grilled Cheese Sandwich"),
        Recipe(localId = "", name = "Banana Bread"),
        Recipe(localId = "", name = "Tomato Basil Pasta"),
        Recipe(localId = "", name = "Roasted Vegetables"),
        Recipe(localId = "", name = "Scrambled Eggs"),
    )
}
