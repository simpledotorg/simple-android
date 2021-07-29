plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  compileSdk = versions.compileSdk

  defaultConfig {
    minSdk = versions.minSdk
    targetSdk = versions.compileSdk

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
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}")
  api("com.jakewharton.timber:timber:${versions.timber}")
}
