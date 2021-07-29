plugins {
  id("com.android.library")
  kotlin("android")
  kotlin("kapt")
}

android {
  compileSdk = versions.compileSdk

  defaultConfig {
    minSdk = versions.minSdk
    targetSdk = versions.compileSdk
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
}

dependencies {
  implementation("androidx.appcompat:appcompat:${versions.appcompat}")
  implementation("com.squareup.flow:flow:${versions.flow}")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}")
  implementation("io.reactivex.rxjava2:rxjava:${versions.rxJava}")
  implementation("com.jakewharton.timber:timber:${versions.timber}")

  testImplementation("junit:junit:${versions.junit}")
  testImplementation("pl.pragmatists:JUnitParams:${versions.junitParams}")
  testImplementation("com.google.truth:truth:${versions.truth}")
}
