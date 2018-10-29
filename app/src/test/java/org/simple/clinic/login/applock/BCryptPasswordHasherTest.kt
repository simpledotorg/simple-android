package org.simple.clinic.login.applock

import org.junit.Test

class BCryptPasswordHasherTest {

  @Test
  fun `comparison test`() {
    val bcryptHasher = BCryptPasswordHasher()

    val password = "12341234"

    bcryptHasher.hash(password)
        .flatMap { bcryptHasher.compare(it, password) }
        .test()
        .assertValue { it == ComparisonResult.SAME }
  }
}
