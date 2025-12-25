package com.timothymarias.cookingapp.shared.presentation.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientStore
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
                onDismissRequest = { recipeStore.dispatch(RecipeAction.EditClose) },
                title = { Text("Assign Ingredients") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (ingredientState.items.isEmpty()) {
                            Text("No ingredients. Create some first.")
                        } else {
                            ingredientState.items.forEach { ing ->
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
                },
                confirmButton = {
                    Button(onClick = { recipeStore.dispatch(RecipeAction.EditClose) }) { Text("Done") }
                }
            )
        }
    }
}