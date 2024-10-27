// Archivo: settings.gradle.kts

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) // Preferir los repositorios definidos aquí
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CulturalPhraseApp"
include(":app")
