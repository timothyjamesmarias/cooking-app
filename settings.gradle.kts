pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "cooking-app"

include(":backend")
include(":frontend:shared")
include(":frontend:android")
include(":frontend:ios")
include(":frontend:desktop")
include(":frontend:web")
