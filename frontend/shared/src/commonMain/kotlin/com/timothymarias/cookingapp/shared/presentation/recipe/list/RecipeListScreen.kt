package com.timothymarias.cookingapp.shared.presentation.recipe.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timothymarias.cookingapp.shared.presentation.components.EmptyState
import com.timothymarias.cookingapp.shared.presentation.components.ErrorState
import com.timothymarias.cookingapp.shared.presentation.components.LoadingIndicator
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeAction
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeState
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeStore

@Composable
fun RecipeListScreen(store: RecipeStore) {
    val state by store.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            RecipeListContent(
                state = state,
                onEvent = { store.dispatch(it) }
            )
        }

        if (showCreateDialog) {
            var name by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create Recipe") },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            store.dispatch(RecipeAction.Create(name))
                            showCreateDialog = false
                        },
                        enabled = name.isNotBlank()
                    ) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                }
            )
        }

        state.editingId?.let { id ->
            var name by remember { mutableStateOf(state.editName) }
            AlertDialog(
                onDismissRequest = { store.dispatch(RecipeAction.EditClose) },
                title = { Text("Edit Recipe") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            store.dispatch(RecipeAction.Rename(id, name))
                            store.dispatch(RecipeAction.EditClose)
                        },
                        enabled = name.isNotBlank()
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { store.dispatch(RecipeAction.EditClose) }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun RecipeListContent(
    state: RecipeState,
    onEvent: (RecipeAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                LoadingIndicator()
            }
            state.error != null -> {
                ErrorState(message = state.error)
            }
            state.items.isEmpty() -> {
                EmptyState(message = "No recipes found")
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.items) { recipe ->
                        RecipeRow(
                            recipe = recipe,
                            onClick = { onEvent(RecipeAction.EditOpen(recipe.localId)) },
                            onDelete = { onEvent(RecipeAction.Delete(recipe.localId)) },
                            onManageIngredients = { onEvent(RecipeAction.ManageIngredientsOpen(recipe.localId)) }
                        )
                    }
                }
            }
        }
    }
}

