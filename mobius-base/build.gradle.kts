plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.androidx.benchmark)
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "org.simple.clinic.mobius"

  val compileSdkVersion: Int by rootProject.extra
  val minSdkVersion: Int by rootProject.extra

  compileSdk = compileSdkVersion

  defaultConfig {
    minSdk = minSdkVersion

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
  }

  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
  }
}

dependencies {
  api(libs.bundles.mobius)

  implementation(libs.kotlin.stdlib)

  implementation(projects.simplePlatform)
  implementation(libs.androidx.viewmodel)
  implementation(libs.androidx.viewmodel.savedstate)
  implementation(libs.androidx.lifecycle.livedata.ktx)
}
