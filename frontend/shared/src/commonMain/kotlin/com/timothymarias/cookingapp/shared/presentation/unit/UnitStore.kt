package com.timothymarias.cookingapp.shared.presentation.unit

import com.timothymarias.cookingapp.shared.data.repository.unit.UnitRepository
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

/**
 * Store for Unit reference data.
 * Units are typically loaded once at app start and cached.
 * Future: Add CRUD operations when users need custom units.
 */
class UnitStore(
    private val repo: UnitRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(UnitState(isLoading = true))
    val state: StateFlow<UnitState> = _state.asStateFlow()

    init {
        scope.launch(ioDispatcher) {
            repo.watchAll()
                .collect { units ->
                    _state.update { it.copy(items = units, isLoading = false) }
                }
        }
    }

    fun dispatch(action: UnitAction) {
        when (action) {
            UnitAction.Load -> { /* already handled by init collector */ }
        }
    }

    fun close() { scope.cancel() }
}
