import com.diffplug.spotless.LineEnding.UNIX
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
  }

  dependencies {
    classpath("com.android.tools.build:gradle:${versions.agp}")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}")
    classpath("io.sentry:sentry-android-gradle-plugin:${versions.sentryGradlePlugin}")
    classpath("com.google.gms:google-services:${versions.googleServices}")
    classpath("com.google.firebase:perf-plugin:${versions.firebasePerformancePlugin}")
    classpath(files("./buildTooling/room-metadata-generator-${versions.roomMetadataGenerator}.jar"))
  }
}

plugins {
  id("com.diffplug.spotless") version versions.spotless
  id("com.github.ben-manes.versions") version versions.gradleVersions
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "com.diffplug.spotless")

  // https://github.com/diffplug/spotless/tree/master/plugin-gradle
  spotless {
    lineEndings = UNIX

    kotlin {
      ktlint(versions.ktlint).userData(mapOf(
          "indent_style" to "space",
          "indent_size" to "2",
          "continuation_indent_size" to "4"
      ))

      trimTrailingWhitespace()
      endWithNewline()
    }
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
}
