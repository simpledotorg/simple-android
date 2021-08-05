plugins {
  id("com.android.library")
  kotlin("android")
}

android {
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

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
  }
}

dependencies {
  api(libs.timber)

  implementation(libs.kotlin.stdlib)

  coreLibraryDesugaring(libs.android.desugaring)
}
