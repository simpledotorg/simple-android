plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(libs.kotlin.stdlib)

  implementation(libs.mobius.core)
  implementation(libs.mobius.rx2)

  testImplementation(libs.junit)

  testImplementation(libs.truth)
}
