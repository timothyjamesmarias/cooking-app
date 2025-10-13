plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.library")
}

kotlin {
    androidTarget()
    
    jvm("desktop")
    
    js(IR) {
        browser()
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation("androidx.activity:activity-compose:1.8.2")
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }
    }
}

android {
    namespace = "com.timothymarias.cookingapp.shared"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}