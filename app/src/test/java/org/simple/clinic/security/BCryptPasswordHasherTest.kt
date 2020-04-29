package org.simple.clinic.security

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.security.ComparisonResult.DIFFERENT
import org.simple.clinic.security.ComparisonResult.SAME
import org.simple.clinic.util.RxErrorsRule

class BCryptPasswordHasherTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val passwordHasher = BCryptPasswordHasher()

  @Test
  fun `it should return SAME if compare a hash from the same password`() {
    val password = "12341234"

    val hashed = passwordHasher.hash(password)
    val result = passwordHasher.compare(hashed, password)

    assertThat(result).isEqualTo(SAME)
  }

  @Test
  fun `it should return DIFFERENT if compare a hash from a different password`() {
    val password = "12341234"

    val hashed = passwordHasher.hash(password)
    val result = passwordHasher.compare(hashed, "hacker")

    assertThat(result).isEqualTo(DIFFERENT)
  }
}
