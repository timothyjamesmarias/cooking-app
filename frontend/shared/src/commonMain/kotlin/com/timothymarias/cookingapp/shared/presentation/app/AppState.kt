package com.timothymarias.cookingapp.shared.presentation.app

enum class Screen {
    RecipeList,
    IngredientList
}

data class AppState(
    val currentScreen: Screen = Screen.RecipeList
)