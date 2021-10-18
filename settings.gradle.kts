enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
  plugins {
    id("com.diffplug.spotless") version "5.6.1"
    id("com.github.ben-manes.versions") version "0.38.0"
  }
}

include(
    ":app",
    ":mobius-migration",
    ":mobius-base",
    ":simple-platform",
    ":simple-visuals",
    ":lint"
)
