package com.timothymarias.cookingapp.shared.presentation.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientAction
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientStore
import com.timothymarias.cookingapp.shared.presentation.ingredient.components.IngredientSearchField
import com.timothymarias.cookingapp.shared.presentation.ingredient.list.IngredientListScreen
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeAction
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeStore
import com.timothymarias.cookingapp.shared.presentation.recipe.list.RecipeListScreen

@Composable
fun AppRoot(
    appState: AppState,
    onScreenSelected: (Screen) -> Unit,
    recipeStore: RecipeStore,
    ingredientStore: IngredientStore
) {
    val recipeState by recipeStore.state.collectAsState()
    val ingredientState by ingredientStore.state.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = appState.currentScreen == Screen.RecipeList,
                    onClick = { onScreenSelected(Screen.RecipeList) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Recipes") },
                    label = { Text("Recipes") }
                )
                NavigationBarItem(
                    selected = appState.currentScreen == Screen.IngredientList,
                    onClick = { onScreenSelected(Screen.IngredientList) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Ingredients") },
                    label = { Text("Ingredients") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (appState.currentScreen) {
                Screen.RecipeList -> RecipeListScreen(recipeStore)
                Screen.IngredientList -> IngredientListScreen(ingredientStore)
            }
        }

        recipeState.managingIngredientsId?.let { recipeId ->
            AlertDialog(
                onDismissRequest = {
                    ingredientStore.dispatch(IngredientAction.QueryChanged(""))
                    recipeStore.dispatch(RecipeAction.EditClose)
                },
                title = { Text("Assign Ingredients") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Search field - fixed at top
                        IngredientSearchField(
                            query = ingredientState.query,
                            onQueryChange = { ingredientStore.dispatch(IngredientAction.QueryChanged(it)) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Ingredient list area - flexible height that adapts to available space
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // Takes available space
                                .heightIn(min = 200.dp, max = 400.dp), // Flexible but bounded
                            contentAlignment = Alignment.Center
                        ) {
                            if (ingredientState.items.isEmpty()) {
                                val message = if (ingredientState.query.isNotEmpty()) {
                                    "No ingredients found for \"${ingredientState.query}\""
                                } else {
                                    "No ingredients yet."
                                }
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                // Only the list scrolls, not the whole column
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(
                                        items = ingredientState.items,
                                        key = { it.localId }
                                    ) { ing ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(ing.name)
                                            Checkbox(
                                                checked = recipeState.assignedIngredientIds.contains(ing.localId),
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        recipeStore.dispatch(RecipeAction.AssignIngredient(recipeId, ing.localId))
                                                    } else {
                                                        recipeStore.dispatch(RecipeAction.RemoveIngredient(recipeId, ing.localId))
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Create button - fixed at bottom, always visible
                        val buttonText = if (ingredientState.query.isNotEmpty()) {
                            "Create \"${ingredientState.query}\""
                        } else {
                            "Create New Ingredient"
                        }

                        OutlinedButton(
                            onClick = {
                                val name = ingredientState.query.ifBlank { return@OutlinedButton }
                                ingredientStore.dispatch(IngredientAction.Create(name))
                                ingredientStore.dispatch(IngredientAction.QueryChanged(""))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = ingredientState.query.isNotBlank()
                        ) {
                            Text(buttonText)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        ingredientStore.dispatch(IngredientAction.QueryChanged(""))
                        recipeStore.dispatch(RecipeAction.EditClose)
                    }) { Text("Done") }
                }
            )
        }
    }
}