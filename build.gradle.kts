import com.diffplug.spotless.LineEnding.UNIX
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
  extra.apply {
    set("compileSdkVersion", 30)
    set("minSdkVersion", 21)
    set("targetSdkVersion", 30)
  }

  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
  }

  dependencies {
    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs") as org.gradle.accessors.dm.LibrariesForLibs
    val roomMetaDataGeneratorVersion = libs.versions.room.metadataGenerator.get()

    classpath(libs.android.gradle.plugin)
    classpath(libs.firebase.performance.plugin)
    classpath(libs.google.services)
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.sentry.gradle.plugin)
    classpath(files("./buildTooling/room-metadata-generator-${roomMetaDataGeneratorVersion}.jar"))
  }
}

plugins {
  id("com.diffplug.spotless")
  id("com.github.ben-manes.versions")
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
      val ktLintVersion = libs.versions.ktlint.get()

      ktlint(ktLintVersion).userData(mapOf(
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

  // Dependency updates report
  outputFormatter = "html"
}
