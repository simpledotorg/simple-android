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

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}")
  api("com.jakewharton.timber:timber:${versions.timber}")

  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${versions.desugarJdk}")
}
