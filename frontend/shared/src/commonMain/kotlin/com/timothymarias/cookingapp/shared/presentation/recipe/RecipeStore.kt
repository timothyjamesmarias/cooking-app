package com.timothymarias.cookingapp.shared.presentation.recipe

import com.timothymarias.cookingapp.shared.data.repository.recipe.RecipeRepository
import com.timothymarias.cookingapp.shared.domain.model.Recipe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeStore(
    private val repo: RecipeRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(RecipeState(isLoading = true))
    val state: StateFlow<RecipeState> = _state.asStateFlow()

    init {
        scope.launch(ioDispatcher) {
            repo.watchAll().collect { list ->
                _state.update { it.copy(items = list, isLoading = false) }
            }
        }
    }

    fun dispatch(action: RecipeAction) {
        when (action) {
            is RecipeAction.Create -> scope.launch(ioDispatcher) {
                _state.update { it.copy(isSaving = true, error = null) }
                runCatching { repo.create(Recipe(localId = "", name = action.name.trim())) }
                    .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
                    .onSuccess { _state.update { it.copy(isSaving = false) } }
            }
            is RecipeAction.Rename -> scope.launch(ioDispatcher) {
                runCatching { repo.updateName(action.id, action.name.trim()) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is RecipeAction.Delete -> scope.launch(ioDispatcher) {
                runCatching { repo.delete(action.id) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is RecipeAction.EditOpen -> _state.update { s ->
                val current = s.items.firstOrNull { it.localId == action.id }
                s.copy(editingId = action.id, editName = current?.name ?: "")
            }
            RecipeAction.EditClose -> _state.update { it.copy(editingId = null, editName = "", managingIngredientsId = null) }
            is RecipeAction.QueryChanged -> _state.update { it.copy(query = action.name) }
            RecipeAction.Load -> { /* already handled by init collector */ }
            is RecipeAction.AssignIngredient -> scope.launch(ioDispatcher) {
                runCatching { repo.assignIngredient(action.recipeId, action.ingredientId) }
                    .onSuccess {
                        _state.update { s -> s.copy(assignedIngredientIds = s.assignedIngredientIds + action.ingredientId) }
                    }
            }
            is RecipeAction.RemoveIngredient -> scope.launch(ioDispatcher) {
                runCatching { repo.removeIngredient(action.recipeId, action.ingredientId) }
                    .onSuccess {
                        _state.update { s -> s.copy(assignedIngredientIds = s.assignedIngredientIds - action.ingredientId) }
                    }
            }
            is RecipeAction.ManageIngredientsOpen -> scope.launch(ioDispatcher) {
                val assigned = repo.getIngredients(action.id)
                _state.update { it.copy(managingIngredientsId = action.id, assignedIngredientIds = assigned.map { it.localId }.toSet()) }
            }
        }
    }

    fun close() { scope.cancel() }
}