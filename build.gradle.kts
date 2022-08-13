buildscript {
  extra.apply {
    set("compileSdkVersion", 32)
    set("minSdkVersion", 21)
    set("targetSdkVersion", 32)
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

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}
