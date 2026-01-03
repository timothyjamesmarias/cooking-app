package com.timothymarias.cookingapp.shared.presentation.ingredient.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.timothymarias.cookingapp.shared.presentation.components.EmptyState
import com.timothymarias.cookingapp.shared.presentation.components.ErrorState
import com.timothymarias.cookingapp.shared.presentation.components.LoadingIndicator
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientAction
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientState
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientStore

@Composable
fun IngredientListScreen(store: IngredientStore) {
    val state by store.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            IngredientListContent(
                state = state,
                onEvent = { store.dispatch(it) }
            )
        }

        if (showCreateDialog) {
            var name by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create Ingredient") },
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
                            store.dispatch(IngredientAction.Create(name))
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
                onDismissRequest = { store.dispatch(IngredientAction.EditClose) },
                title = { Text("Edit Ingredient") },
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
                            store.dispatch(IngredientAction.Rename(id, name))
                            store.dispatch(IngredientAction.EditClose)
                        },
                        enabled = name.isNotBlank()
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { store.dispatch(IngredientAction.EditClose) }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun IngredientListContent(
    state: IngredientState,
    onEvent: (IngredientAction) -> Unit
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
                EmptyState(message = "No ingredients found")
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.items) { ingredient ->
                        IngredientRow(
                            ingredient = ingredient,
                            onClick = { onEvent(IngredientAction.EditOpen(ingredient.localId)) },
                            onDelete = { onEvent(IngredientAction.Delete(ingredient.localId)) }
                        )
                    }
                }
            }
        }
    }
}