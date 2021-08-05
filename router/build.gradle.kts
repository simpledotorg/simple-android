plugins {
  id("com.android.library")
  kotlin("android")
  kotlin("kapt")
}

android {
  val compileSdkVersion: Int by rootProject.extra
  val minSdkVersion: Int by rootProject.extra
  val targetSdkVersion: Int by rootProject.extra

  compileSdk = compileSdkVersion

  defaultConfig {
    minSdk = minSdkVersion
    targetSdk = targetSdkVersion
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
}

dependencies {
  implementation(libs.androidx.appcompat)

  implementation(libs.flow)

  implementation(libs.kotlin.stdlib)

  implementation(libs.rx.java)

  implementation(libs.timber)

  testImplementation(libs.junit)
  testImplementation(libs.junitParams)

  testImplementation(libs.truth)
}
