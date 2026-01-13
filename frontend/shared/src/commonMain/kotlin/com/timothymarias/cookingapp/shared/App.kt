package com.timothymarias.cookingapp.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.timothymarias.cookingapp.shared.presentation.app.AppRoot
import com.timothymarias.cookingapp.shared.presentation.app.AppState
import com.timothymarias.cookingapp.shared.presentation.app.Screen
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientStore
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeStore
import com.timothymarias.cookingapp.shared.presentation.unit.UnitStore

@Composable
fun App() {
    val recipeRepo = remember { ServiceLocator.recipeRepository }
    val ingredientRepo = remember { ServiceLocator.ingredientRepository }
    val unitRepo = remember { ServiceLocator.unitRepository }
    val quantityRepo = remember { ServiceLocator.quantityRepository }

    val recipeStore = remember { RecipeStore(recipeRepo, quantityRepo) }
    val ingredientStore = remember { IngredientStore(ingredientRepo) }
    val unitStore = remember { UnitStore(unitRepo) }

    var appState by remember { mutableStateOf(AppState()) }

    MaterialTheme {
        AppRoot(
            appState = appState,
            onScreenSelected = { screen ->
                appState = appState.copy(currentScreen = screen)
            },
            recipeStore = recipeStore,
            ingredientStore = ingredientStore,
            unitStore = unitStore
        )
    }
}