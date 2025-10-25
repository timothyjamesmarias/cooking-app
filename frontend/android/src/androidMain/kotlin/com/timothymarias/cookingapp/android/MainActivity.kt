package com.timothymarias.cookingapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.timothymarias.cookingapp.shared.App
import com.timothymarias.cookingapp.shared.ServiceLocator
import com.timothymarias.cookingapp.shared.data.local.DriverConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize shared SQLDelight database and repositories for Android
        ServiceLocator.init(DriverConfig(androidContext = applicationContext))
        setContent {
            App()
        }
    }
}