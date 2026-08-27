pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(
        RepositoriesMode.FAIL_ON_PROJECT_REPOS
    )

    repositories {
        google()
        mavenCentral()

        maven {
            url = uri(
                "https://maven.scijava.org/content/repositories/public/"
            )
        }
    }
}

rootProject.name = "LifeAlarm"

include(":app")
