package com.timothymarias.cookingapp.shared.presentation.recipe

import com.timothymarias.cookingapp.shared.domain.model.Recipe

data class RecipeState(
    val items: List<Recipe> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    // UI flags to survive recompositions/navigation
    val editingId: String? = null,
    val editName: String = "",
)