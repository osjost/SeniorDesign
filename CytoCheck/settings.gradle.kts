pluginManagement {
    repositories {
        google()
        mavenCentral()
        // Add other repositories here
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        // Add other repositories here
    }
}

rootProject.name = "CytoCheck"
include(":app")
