package com.timothymarias.cookingapp.shared.presentation.recipe

sealed interface RecipeAction {
    data object Load : RecipeAction
    data class Create(val name: String) : RecipeAction
    data class Rename(val id: String, val name: String): RecipeAction
    data class Delete(val id: String): RecipeAction
    data class QueryChanged(val name: String): RecipeAction
    data class EditOpen(val id: String): RecipeAction
    data object EditClose : RecipeAction
}