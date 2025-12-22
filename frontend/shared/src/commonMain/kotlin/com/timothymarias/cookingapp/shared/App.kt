package com.timothymarias.cookingapp.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.timothymarias.cookingapp.shared.presentation.app.AppRoot
import com.timothymarias.cookingapp.shared.presentation.app.AppState
import com.timothymarias.cookingapp.shared.presentation.app.Screen
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientStore
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeStore

@Composable
fun App() {
    val recipeRepo = remember { ServiceLocator.recipeRepository }
    val ingredientRepo = remember { ServiceLocator.ingredientRepository }

    val recipeStore = remember { RecipeStore(recipeRepo) }
    val ingredientStore = remember { IngredientStore(ingredientRepo) }

    var appState by remember { mutableStateOf(AppState()) }

    MaterialTheme {
        AppRoot(
            appState = appState,
            onScreenSelected = { screen ->
                appState = appState.copy(currentScreen = screen)
            },
            recipeStore = recipeStore,
            ingredientStore = ingredientStore
        )
    }
}