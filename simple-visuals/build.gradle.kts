plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  namespace = "org.simple.clinic.visuals"

  val compileSdkVersion: Int by rootProject.extra
  val minSdkVersion: Int by rootProject.extra
  val targetSdkVersion: Int by rootProject.extra

  compileSdk = compileSdkVersion

  defaultConfig {
    minSdk = minSdkVersion
    targetSdk = targetSdkVersion

    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro"
      )
    }
  }
}

dependencies {
  implementation(libs.kotlin.stdlib)

  implementation(libs.rx.java)

  testImplementation(libs.junit)

  testImplementation(libs.truth)
}
