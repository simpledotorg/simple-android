package org.simple.clinic.security

import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.RxErrorsRule

class BCryptPasswordHasherTest {

  @get:Rule
  val rules: org.junit.rules.RuleChain = org.simple.clinic.util.Rules.global()

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
