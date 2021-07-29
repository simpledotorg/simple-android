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
  implementation("io.reactivex.rxjava2:rxjava:${versions.rxJava}")

  testImplementation("junit:junit:${versions.junit}")
  testImplementation("com.google.truth:truth:${versions.truth}")
}
