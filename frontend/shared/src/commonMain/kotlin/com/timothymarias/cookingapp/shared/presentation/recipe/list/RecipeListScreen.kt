package com.timothymarias.cookingapp.shared.presentation.recipe.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeAction
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeState
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeStore

@Composable
fun RecipeListScreen(store: RecipeStore) {
    val state by store.state.collectAsState()
    RecipeListContent(
        state = state,
        onEvent = store::dispatch
    )
}

@Composable
fun RecipeListContent(state: RecipeState, onEvent: KFunction1<RecipeAction, Unit>) {
    TODO("Not yet implemented")
}

