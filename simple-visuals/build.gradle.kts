plugins {
  alias(libs.plugins.android.library)
}

android {
  namespace = "org.simple.clinic.visuals"

  val compileSdkVersion: Int = rootProject.extra["compileSdkVersion"] as Int
  val minSdkVersion: Int = rootProject.extra["minSdkVersion"] as Int

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
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
  }
}

dependencies {
  implementation(libs.kotlin.stdlib)

  implementation(libs.rx.java)

  testImplementation(libs.junit)

  testImplementation(libs.truth)
}
