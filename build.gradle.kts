plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.lint) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.parcelize) apply false
  alias(libs.plugins.kotlin.compose.compiler) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.sentry) apply false
  alias(libs.plugins.datadog) apply false
}

buildscript {
  extra.apply {
    set("compileSdkVersion", 35)
    set("minSdkVersion", 21)
    set("targetSdkVersion", 35)
  }

  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
  }

  dependencies {
    val roomMetaDataGeneratorVersion = libs.versions.room.metadataGenerator.get()
    classpath(files("./buildTooling/room-metadata-generator-${roomMetaDataGeneratorVersion}.jar"))
  }
}
