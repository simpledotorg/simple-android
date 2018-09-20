package org.simple.clinic.search.results

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IndianPhoneNumberObfuscatorTest {

  @Test
  fun `should obfuscate correctly`() {
    val obfuscator = IndianPhoneNumberObfuscator()

    val validNumber = "9811111365"
    assertThat(obfuscator.obfuscate(validNumber)).isEqualTo("98 • • • • • 365")

    val incompleteNumber = validNumber.substring(1)
    assertThat(obfuscator.obfuscate(incompleteNumber)).isEqualTo("• • • • • • • • • ")

    val extraNumbers = validNumber.repeat(2)
    assertThat(obfuscator.obfuscate(extraNumbers)).isEqualTo("98 • • • • • • • • • • • • • • • 365")
  }
}
