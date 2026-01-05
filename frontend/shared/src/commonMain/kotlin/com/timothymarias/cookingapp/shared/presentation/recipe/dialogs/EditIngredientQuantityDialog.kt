package com.timothymarias.cookingapp.shared.presentation.recipe.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timothymarias.cookingapp.shared.presentation.recipe.RecipeAction
import com.timothymarias.cookingapp.shared.presentation.unit.UnitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIngredientQuantityDialog(
    recipeId: String,
    ingredientId: String,
    ingredientName: String,
    unitState: UnitState,
    onSave: (amount: Double, unitId: String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedUnitId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // Initialize with first unit if available
    LaunchedEffect(unitState.items) {
        if (selectedUnitId == null && unitState.items.isNotEmpty()) {
            selectedUnitId = unitState.items.first().localId
        }
    }

    val selectedUnit = unitState.items.firstOrNull { it.localId == selectedUnitId }
    val isValid = amountText.toDoubleOrNull() != null &&
                  amountText.toDoubleOrNull()?.let { it > 0 } == true &&
                  selectedUnitId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Quantity for $ingredientName") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    placeholder = { Text("e.g., 2.5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = amountText.isNotEmpty() && amountText.toDoubleOrNull() == null
                )

                // Unit dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedUnit?.let { "${it.name} (${it.symbol})" } ?: "Select unit",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        unitState.items.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text("${unit.name} (${unit.symbol})") },
                                onClick = {
                                    selectedUnitId = unit.localId
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (unitState.items.isEmpty()) {
                    Text(
                        text = "Loading units...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Clear button (removes quantity)
                TextButton(onClick = {
                    onClear()
                    onDismiss()
                }) {
                    Text("Clear")
                }

                // Save button
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        val unitId = selectedUnitId
                        if (amount != null && unitId != null) {
                            onSave(amount, unitId)
                            onDismiss()
                        }
                    },
                    enabled = isValid
                ) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
