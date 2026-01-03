package com.timothymarias.cookingapp.shared.presentation.recipe.dialogs

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientAction
import com.timothymarias.cookingapp.shared.presentation.ingredient.IngredientState
import com.timothymarias.cookingapp.shared.presentation.ingredient.components.IngredientSearchField
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeAction
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeState

@Composable
fun AssignIngredientsDialog(
    recipeId: String,
    ingredientState: IngredientState,
    recipeState: RecipeState,
    onIngredientAction: (IngredientAction) -> Unit,
    onRecipeAction: (RecipeAction) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Ingredients") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search field - fixed at top
                IngredientSearchField(
                    query = ingredientState.query,
                    onQueryChange = { onIngredientAction(IngredientAction.QueryChanged(it)) }
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
                                                onRecipeAction(RecipeAction.AssignIngredient(recipeId, ing.localId))
                                            } else {
                                                onRecipeAction(RecipeAction.RemoveIngredient(recipeId, ing.localId))
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
                        onIngredientAction(IngredientAction.Create(name))
                        onIngredientAction(IngredientAction.QueryChanged(""))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = ingredientState.query.isNotBlank()
                ) {
                    Text(buttonText)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}
