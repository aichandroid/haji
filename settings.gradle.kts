pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Haji2027"

// Core Modules
include(":app")
include(":core:designsystem")
include(":core:common")
include(":core:database")
include(":core:model")

// Feature Modules
include(":feature:guide")
include(":feature:places")
include(":feature:tracking")
include(":feature:settings")
