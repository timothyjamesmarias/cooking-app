package com.timothymarias.cookingapp.shared.data.local

import com.timothymarias.cookingapp.shared.BuildConfig as AndroidBuildConfig

actual object BuildConfig {
    actual val isDebug: Boolean = AndroidBuildConfig.DEBUG
}
