package com.timothymarias.cookingapp.shared.presentation.unit

/**
 * Actions for Unit store.
 * Currently focused on loading reference data.
 * CRUD operations can be added later when users need custom units.
 */
sealed interface UnitAction {
    data object Load : UnitAction
}
