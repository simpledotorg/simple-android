plugins {
  id("com.android.library")
  kotlin("android")
}

android {
  namespace = "org.simple.sharedTestCode"
  
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
    release {
      isMinifyEnabled = false
      proguardFiles(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
  compileOptions {
    isCoreLibraryDesugaringEnabled = true
  }
}

dependencies {
  coreLibraryDesugaring(libs.android.desugaring)

  implementation(projects.app)
  implementation(libs.traceur)
  implementation(libs.junit)
  implementation(libs.rx.java)
  implementation(libs.faker)
  implementation(libs.retrofit.retrofit)
  implementation(libs.okhttp.okhttp)
}
