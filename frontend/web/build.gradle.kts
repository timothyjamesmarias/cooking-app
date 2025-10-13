plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "cookingapp.js"
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        jsMain.dependencies {
            implementation(project(":frontend:shared"))
            implementation(compose.html.core)
        }
    }
}