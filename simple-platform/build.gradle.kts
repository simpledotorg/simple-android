plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  namespace = "org.simple.clinic.platform"

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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    isCoreLibraryDesugaringEnabled = true
  }

  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
}

dependencies {
  api(libs.timber)

  implementation(libs.kotlin.stdlib)

  coreLibraryDesugaring(libs.android.desugaring)
}
