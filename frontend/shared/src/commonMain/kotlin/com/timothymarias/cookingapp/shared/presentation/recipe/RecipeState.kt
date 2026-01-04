package com.timothymarias.cookingapp.shared.presentation.recipe

import com.timothymarias.cookingapp.shared.domain.model.Recipe
import com.timothymarias.cookingapp.shared.domain.model.Ingredient

data class RecipeState(
    val items: List<Recipe> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    // UI flags to survive recompositions/navigation
    val managingIngredientsId: String? = null,
    val assignedIngredientIds: Set<String> = emptySet(),
    val selectedRecipeId: String? = null, // For recipe detail screen
    val isEditMode: Boolean = false, // For recipe detail screen edit mode
)