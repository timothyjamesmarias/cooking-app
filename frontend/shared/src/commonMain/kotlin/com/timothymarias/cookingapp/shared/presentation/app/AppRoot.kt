package com.timothymarias.cookingapp.shared.presentation.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientAction
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientStore
import com.timothymarias.cookingapp.shared.presentation.ingredient.list.IngredientListScreen
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeAction
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeStore
import com.timothymarias.cookingapp.shared.presentation.recipe.dialogs.AssignIngredientsDialog
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
            AssignIngredientsDialog(
                recipeId = recipeId,
                ingredientState = ingredientState,
                recipeState = recipeState,
                onIngredientAction = { action ->
                    ingredientStore.dispatch(action)
                },
                onRecipeAction = { action ->
                    recipeStore.dispatch(action)
                },
                onDismiss = {
                    ingredientStore.dispatch(IngredientAction.QueryChanged(""))
                    recipeStore.dispatch(RecipeAction.EditClose)
                }
            )
        }
    }
}