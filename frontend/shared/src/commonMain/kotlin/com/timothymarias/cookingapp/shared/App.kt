package com.timothymarias.cookingapp.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timothymarias.cookingapp.shared.domain.model.Ingredient
import com.timothymarias.cookingapp.shared.domain.model.Recipe
import kotlinx.coroutines.launch

@Composable
fun App() {
    val recipeRepo = ServiceLocator.recipeRepository
    val ingredientRepo = ServiceLocator.ingredientRepository

    val recipes by recipeRepo.watchAll().collectAsState(initial = emptyList())
    val ingredients by ingredientRepo.watchAll().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    // Recipe create
    val (name, setName) = remember { mutableStateOf("") }

    // Recipe edit dialog state
    val (showEdit, setShowEdit) = remember { mutableStateOf(false) }
    val (editing, setEditing) = remember { mutableStateOf<Recipe?>(null) }
    val (editName, setEditName) = remember { mutableStateOf("") }

    // Ingredient create
    val (ingName, setIngName) = remember { mutableStateOf("") }

    // Ingredient edit dialog state
    val (showEditIng, setShowEditIng) = remember { mutableStateOf(false) }
    val (editingIng, setEditingIng) = remember { mutableStateOf<Ingredient?>(null) }
    val (editIngName, setEditIngName) = remember { mutableStateOf("") }

    // Manage recipe ingredients dialog
    val (showManage, setShowManage) = remember { mutableStateOf(false) }
    val (manageRecipe, setManageRecipe) = remember { mutableStateOf<Recipe?>(null) }
    val selectedIds = remember { mutableStateListOf<String>() }
    val originalSelected = remember { mutableStateListOf<String>() }

    val isValidRecipe = name.isNotBlank()
    val isValidIngredient = ingName.isNotBlank()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Recipes section
            OutlinedTextField(
                value = name,
                onValueChange = setName,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Recipe name") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        recipeRepo.create(Recipe(localId = "", name = name.trim()))
                        setName("")
                    }
                },
                enabled = isValidRecipe,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Create recipe") }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Recipes", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(recipes) { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(r.name)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                setEditing(r)
                                setEditName(r.name)
                                setShowEdit(true)
                            }) { Text("Edit") }
                            TextButton(onClick = {
                                scope.launch { recipeRepo.delete(r.localId) }
                            }) { Text("Delete") }
                            TextButton(onClick = {
                                // Open manage ingredients dialog
                                scope.launch {
                                    val assigned = recipeRepo.getIngredients(r.localId)
                                    originalSelected.clear()
                                    originalSelected.addAll(assigned.map { it.localId })
                                    selectedIds.clear()
                                    selectedIds.addAll(originalSelected)
                                    setManageRecipe(r)
                                    setShowManage(true)
                                }
                            }) { Text("Ingredients") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Ingredients section
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = ingName,
                onValueChange = setIngName,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Ingredient name") }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        ingredientRepo.create(Ingredient(localId = "", name = ingName.trim()))
                        setIngName("")
                    }
                },
                enabled = isValidIngredient,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Create ingredient") }

            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(ingredients) { ing ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ing.name)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                setEditingIng(ing)
                                setEditIngName(ing.name)
                                setShowEditIng(true)
                            }) { Text("Edit") }
                            TextButton(onClick = {
                                scope.launch { ingredientRepo.delete(ing.localId) }
                            }) { Text("Delete") }
                        }
                    }
                }
            }
        }

        // Edit recipe dialog
        if (showEdit && editing != null) {
            AlertDialog(
                onDismissRequest = { setShowEdit(false); setEditing(null) },
                title = { Text("Edit recipe") },
                text = {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = setEditName,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Recipe name") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                recipeRepo.updateName(editing!!.localId, editName.trim())
                                setShowEdit(false)
                                setEditing(null)
                            }
                        },
                        enabled = editName.isNotBlank()
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { setShowEdit(false); setEditing(null) }) { Text("Cancel") }
                }
            )
        }

        // Edit ingredient dialog
        if (showEditIng && editingIng != null) {
            AlertDialog(
                onDismissRequest = { setShowEditIng(false); setEditingIng(null) },
                title = { Text("Edit ingredient") },
                text = {
                    OutlinedTextField(
                        value = editIngName,
                        onValueChange = setEditIngName,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Ingredient name") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                ingredientRepo.updateName(editingIng!!.localId, editIngName.trim())
                                setShowEditIng(false)
                                setEditingIng(null)
                            }
                        },
                        enabled = editIngName.isNotBlank()
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { setShowEditIng(false); setEditingIng(null) }) { Text("Cancel") }
                }
            )
        }

        // Manage recipe ingredients dialog
        if (showManage && manageRecipe != null) {
            AlertDialog(
                onDismissRequest = { setShowManage(false); setManageRecipe(null) },
                title = { Text("Assign ingredients") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (ingredients.isEmpty()) {
                            Text("No ingredients. Create some first.")
                        } else {
                            ingredients.forEach { ing ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ing.name)
                                    Checkbox(
                                        checked = selectedIds.contains(ing.localId),
                                        onCheckedChange = { checked ->
                                            if (checked) selectedIds.add(ing.localId)
                                            else selectedIds.remove(ing.localId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val recipeId = manageRecipe!!.localId
                                val toAdd = selectedIds.toSet() - originalSelected.toSet()
                                val toRemove = originalSelected.toSet() - selectedIds.toSet()
                                toAdd.forEach { ingredientId -> recipeRepo.assignIngredient(recipeId, ingredientId) }
                                toRemove.forEach { ingredientId -> recipeRepo.removeIngredient(recipeId, ingredientId) }
                                setShowManage(false)
                                setManageRecipe(null)
                            }
                        }
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { setShowManage(false); setManageRecipe(null) }) { Text("Cancel") }
                }
            )
        }
    }
}