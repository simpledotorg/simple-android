@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
  }
}

include(
    ":app",
    ":mobius-migration",
    ":mobius-base",
    ":simple-platform",
    ":simple-visuals",
    ":lint",
    ":sharedTestCode",
    ":common-ui"
)
