plugins {
  kotlin("jvm")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${versions.kotlin}")

  implementation("com.spotify.mobius:mobius-core:${versions.mobius}")
  implementation("com.spotify.mobius:mobius-rx2:${versions.mobius}")
  implementation("com.google.guava:guava:${versions.guava}")

  testImplementation("junit:junit:${versions.junit}")
  testImplementation("com.google.truth:truth:${versions.truth}")
}
