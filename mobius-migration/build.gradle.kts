plugins {
  kotlin("jvm")
}

dependencies {
  implementation(libs.kotlin.stdlib)

  implementation(libs.mobius.core)
  implementation(libs.mobius.rx2)

  testImplementation(libs.junit)

  testImplementation(libs.truth)
}
