plugins {
  id("com.android.library")
  kotlin("android")
  kotlin("kapt")
}

android {
  compileSdkVersion(versions.compileSdk)

  defaultConfig {
    minSdkVersion(versions.minSdk)
    targetSdkVersion(versions.compileSdk)
    versionCode = 1
    versionName = "1.0"
  }

  buildTypes {
    getByName("release") {
      minifyEnabled(false)
    }
  }
}

dependencies {
  implementation("androidx.appcompat:appcompat:${versions.supportLib}")
  implementation("com.squareup.flow:flow:${versions.flow}")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}")
  implementation("io.reactivex.rxjava2:rxjava:${versions.rxJava}")
  implementation("com.jakewharton.timber:timber:${versions.timber}")

  testImplementation("junit:junit:${versions.junit}")
  testImplementation("pl.pragmatists:JUnitParams:${versions.junitParams}")
  testImplementation("com.google.truth:truth:${versions.truth}")
}
