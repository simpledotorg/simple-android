import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
  extra.apply {
    set("compileSdkVersion", 31)
    set("minSdkVersion", 21)
    set("targetSdkVersion", 31)
  }

  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
  }

  dependencies {
    val roomMetaDataGeneratorVersion = libs.versions.room.metadataGenerator.get()

    classpath(libs.android.gradle.plugin)
    classpath(libs.google.services)
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.sentry.gradle.plugin)
    classpath(files("./buildTooling/room-metadata-generator-${roomMetaDataGeneratorVersion}.jar"))
    classpath(libs.datadog.gradle.plugin)
    classpath(libs.benchmark.gradle.plugin)
  }
}

// https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.dependencyUpdates)
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}

tasks.named<Delete>("clean") {
  delete(rootProject.buildDir)
}

fun String.isNonStable(): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { this.toUpperCase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(this)
  return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
  // Reject all non-stable versions
  rejectVersionIf { candidate.version.isNonStable() }

  // Dependency updates report
  outputFormatter = "html"
}
