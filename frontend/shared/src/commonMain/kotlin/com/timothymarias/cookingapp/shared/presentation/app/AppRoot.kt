package com.timothymarias.cookingapp.shared.presentation.app

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppRoot(appState: AppState) {
    Scaffold() { padding -> Row(Modifier.padding(padding)) {} }
}