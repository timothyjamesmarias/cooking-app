package com.timothymarias.cookingapp.shared.presentation.recipe

sealed interface RecipeAction {
    data object Load : RecipeAction
    data class Create(val name: String) : RecipeAction
    data class Rename(val id: String, val name: String): RecipeAction
    data class Delete(val id: String): RecipeAction
    data class QueryChanged(val name: String): RecipeAction
    data object EditClose : RecipeAction
    data class AssignIngredient(val recipeId: String, val ingredientId: String): RecipeAction
    data class RemoveIngredient(val recipeId: String, val ingredientId: String): RecipeAction
    data class ManageIngredientsOpen(val id: String): RecipeAction
    data class ViewRecipeDetail(val id: String): RecipeAction
    data object CloseRecipeDetail : RecipeAction
    data object EnterEditMode : RecipeAction
    data object ExitEditMode : RecipeAction
    data class ViewRecipeDetailInEditMode(val id: String): RecipeAction
}