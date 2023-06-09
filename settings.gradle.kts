pluginManagement {
    repositories {
        google()
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

rootProject.name = "AutomatedTaskHelper"
include(":app")
include(":library")
include(":library:base")
include(":library:operation")
include(":library:graphic")
include(":library:dispatcher")