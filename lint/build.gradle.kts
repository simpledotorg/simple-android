plugins {
  kotlin("jvm")
  id("com.android.lint")
}

dependencies {
  compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${versions.kotlin}")

  // Lint
  compileOnly("com.android.tools.lint:lint-api:${versions.lint}")
  compileOnly("com.android.tools.lint:lint-checks:${versions.lint}")

  // Testing
  testImplementation("com.android.tools.lint:lint:${versions.lint}")
  testImplementation("com.android.tools.lint:lint-tests:${versions.lint}")
  testImplementation("junit:junit:${versions.junit}")
}
