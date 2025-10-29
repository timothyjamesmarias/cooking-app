package com.timothymarias.cookingapp.shared.presentation.recipe

import com.timothymarias.cookingapp.shared.domain.model.Recipe

sealed interface RecipeAction {
    data object Load : RecipeAction
    data class Created(val recipe: Recipe) : RecipeAction
//    data class Failed(val error: UiError) : RecipeAction
}