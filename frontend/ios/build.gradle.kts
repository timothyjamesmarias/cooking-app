plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.executable {
            entryPoint = "main"
        }
    }
    
    sourceSets {
        iosMain.dependencies {
            implementation(project(":frontend:shared"))
        }
    }
}