package com.timothymarias.cookingapp.shared

import app.cash.sqldelight.db.SqlDriver
import com.timothymarias.cookingapp.shared.data.local.BuildConfig
import com.timothymarias.cookingapp.shared.data.local.DatabaseDriverFactory
import com.timothymarias.cookingapp.shared.data.local.DatabaseSeeder
import com.timothymarias.cookingapp.shared.data.local.DriverConfig
import com.timothymarias.cookingapp.shared.data.local.createDatabase
import com.timothymarias.cookingapp.shared.data.repository.ingredient.DbIngredientRepository
import com.timothymarias.cookingapp.shared.data.repository.ingredient.IngredientRepository
import com.timothymarias.cookingapp.shared.data.repository.quantity.DbQuantityRepository
import com.timothymarias.cookingapp.shared.data.repository.quantity.QuantityRepository
import com.timothymarias.cookingapp.shared.data.repository.recipe.DbRecipeRepository
import com.timothymarias.cookingapp.shared.data.repository.recipe.RecipeRepository
import com.timothymarias.cookingapp.shared.data.repository.unit.DbUnitRepository
import com.timothymarias.cookingapp.shared.data.repository.unit.UnitRepository
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Very small, manual service locator to initialize shared singletons.
 * This keeps platform code minimal while avoiding a hard DI dependency.
 */
object ServiceLocator {
    private var initialized: Boolean = false
    private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var driver: SqlDriver
    private lateinit var database: CookingDatabase

    // Public repositories exposed to UI/business layers
    lateinit var recipeRepository: RecipeRepository
        private set
    lateinit var ingredientRepository: IngredientRepository
        private set
    lateinit var unitRepository: UnitRepository
        private set
    lateinit var quantityRepository: QuantityRepository
        private set

    /**
     * Initialize the SQLDelight database and repositories.
     * Safe to call multiple times; subsequent calls are no-ops.
     */
    fun init(config: DriverConfig = DriverConfig()) {
        if (initialized) return
        val driverFactory = DatabaseDriverFactory(config)
        driver = driverFactory.createDriver()
        database = createDatabase(driver)

        // Wire repositories
        recipeRepository = DbRecipeRepository(database)
        ingredientRepository = DbIngredientRepository(database)
        unitRepository = DbUnitRepository(database)
        quantityRepository = DbQuantityRepository(database)

        // Seed reference data asynchronously
        initScope.launch {
            if (BuildConfig.isDebug) {
                // Development build: seed test data
                println("Development build detected - seeding test data")
                DatabaseSeeder.seedDevelopment(
                    recipeRepository = recipeRepository,
                    ingredientRepository = ingredientRepository,
                    unitRepository = unitRepository
                )
            } else {
                // Production build: only seed essential reference data
                println("Production build - seeding reference data only")
                DatabaseSeeder.seedProduction(unitRepository)
            }
        }

        initialized = true
    }
}
