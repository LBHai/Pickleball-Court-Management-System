pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://www.jitpack.io")   // Chú ý dấu `=` cho Kotlin
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://www.jitpack.io")   // Chú ý dấu `=` cho Kotlin
    }
}

rootProject.name = "PickleballCourtManagementSystem"
include(":app")
