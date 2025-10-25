package com.timothymarias.cookingapp.shared.tests.recipe

import kotlin.test.Ignore
import kotlin.test.Test

/**
 * Skeleton tests for the Presentation layer (RecipeStore) to document expected
 * state transitions for CRUD actions. These are @Ignore for now until the
 * store and CRUD are implemented.
 */
class RecipeStoreContractTest {

    @Test
    @Ignore
    fun `creating a recipe shows optimistic item then commits`() {
        // Expectation:
        // - Dispatch Create action
        // - State emits: isSaving = true, list includes temp item
        // - After repo.create succeeds: isSaving = false, temp replaced by real item
    }

    @Test
    @Ignore
    fun `updating recipe name reflects in list and detail flows`() {
        // Expectation:
        // - Dispatch Rename action
        // - State emits updated name for the item
    }

    @Test
    @Ignore
    fun `deleting a recipe removes it from state`() {
        // Expectation:
        // - Dispatch Delete action
        // - State emits list without the deleted item
    }
}
