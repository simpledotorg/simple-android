plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.android.lint)
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  compileOnly(libs.kotlin.stdlib)

  // Lint
  compileOnly(libs.bundles.lint)

  // Testing
  testImplementation(libs.junit)

  testImplementation(libs.lint.lint)
  testImplementation(libs.lint.tests)
}
