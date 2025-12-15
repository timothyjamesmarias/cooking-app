package com.timothymarias.cookingapp.shared.presentation.recipe

import com.timothymarias.cookingapp.shared.data.repository.recipe.RecipeRepository
import com.timothymarias.cookingapp.shared.domain.model.Recipe
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
    private val dispatchers: Dispatchers = Dispatchers,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.Default)
    private val _state = MutableStateFlow(RecipeState(isLoading = true))
    val state: StateFlow<RecipeState> = _state.asStateFlow()

    init {
        scope.launch(dispatchers.IO) {
            repo.watchAll().collect { list ->
                _state.update { it.copy(items = list, isLoading = false) }
            }
        }
    }

    fun dispatch(action: RecipeAction) {
        when (action) {
            is RecipeAction.Create -> scope.launch(dispatchers.IO) {
                _state.update { it.copy(isSaving = true, error = null) }
                runCatching { repo.create(Recipe(localId = "", name = action.name.trim())) }
                    .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
                    .onSuccess { _state.update { it.copy(isSaving = false) } }
            }
            is RecipeAction.Rename -> scope.launch(dispatchers.IO) {
                runCatching { repo.updateName(action.id, action.name.trim()) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is RecipeAction.Delete -> scope.launch(dispatchers.IO) {
                runCatching { repo.delete(action.id) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is RecipeAction.EditOpen -> _state.update { s ->
                val current = s.items.firstOrNull { it.localId == action.id }
                s.copy(editingId = action.id, editName = current?.name ?: "")
            }
            RecipeAction.EditClose -> _state.update { it.copy(editingId = null, editName = "") }
            is RecipeAction.QueryChanged -> _state.update { it.copy(query = action.name) }
            RecipeAction.Load -> { /* already handled by init collector */ }
        }
    }

    fun close() { scope.cancel() }
}