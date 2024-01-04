rootProject.name = "joybot"

include("data")
include("reddit") // Should be unused -- API is broked we keep for reference

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://jitpack.io/")
    }
}