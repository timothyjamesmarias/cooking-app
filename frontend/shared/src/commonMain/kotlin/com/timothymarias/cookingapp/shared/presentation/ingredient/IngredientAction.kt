package com.timothymarias.cookingapp.shared.presentation.ingredient

sealed interface IngredientAction {
    data object Load : IngredientAction
    data class Create(val name: String) : IngredientAction
    data class Rename(val id: String, val name: String): IngredientAction
    data class Delete(val id: String): IngredientAction
    data class QueryChanged(val name: String): IngredientAction
    data class EditOpen(val id: String): IngredientAction
    data object EditClose : IngredientAction
}