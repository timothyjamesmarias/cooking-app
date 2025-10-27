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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timothymarias.cookingapp.shared.domain.model.Recipe
import kotlinx.coroutines.launch

@Composable
fun App() {
    val repo = ServiceLocator.recipeRepository
    val recipes by repo.watchAll().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()
    val (name, setName) = remember { mutableStateOf("") }

    // Edit dialog state
    val (showEdit, setShowEdit) = remember { mutableStateOf(false) }
    val (editing, setEditing) = remember { mutableStateOf<Recipe?>(null) }
    val (editName, setEditName) = remember { mutableStateOf("") }

    val isValid = name.isNotBlank()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
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
                        repo.create(Recipe(localId = "", name = name.trim()))
                        setName("")
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create recipe")
            }

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
                            }) {
                                Text("Edit")
                            }
                            TextButton(onClick = {
                                scope.launch { repo.delete(r.localId) }
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }

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
                                repo.updateName(editing!!.localId, editName.trim())
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
    }
}