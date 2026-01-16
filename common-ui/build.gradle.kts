plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose.compiler)
}

android {
  val compileSdkVersion: Int by rootProject.extra
  val minSdkVersion: Int by rootProject.extra

  namespace = "org.simple.clinic.common"
  compileSdk = compileSdkVersion

  defaultConfig {
    minSdk = minSdkVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.edittext.pinentry)

  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  api(libs.androidx.compose.material)
  api(libs.androidx.compose.material.iconsExtended)
  implementation(libs.composeThemeAdapter)
  implementation(libs.composeThemeAdapterCore)
  implementation(libs.androidx.compose.ui.tooling.preview)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
