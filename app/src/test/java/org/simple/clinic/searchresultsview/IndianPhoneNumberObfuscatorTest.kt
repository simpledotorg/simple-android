package org.simple.clinic.searchresultsview

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.util.Unicode.bullet
import org.simple.clinic.util.Unicode.hairSpace

private const val mask = "$bullet$hairSpace"

@RunWith(JUnitParamsRunner::class)
class IndianPhoneNumberObfuscatorTest {

  private val obfuscator = IndianPhoneNumberObfuscator()

  @Test
  @Parameters(value = [
    "|",
    "4|4",
    "45|45",
    "365|365",
    "12345|$mask${mask}345",
    "112345|$mask$mask${mask}345",
    "1112345|$mask$mask$mask${mask}345",
    "11112345|$mask$mask$mask$mask${mask}345",
    "811111365|8$hairSpace$mask$mask$mask$mask${mask}365",
    "9811111365|98$hairSpace$mask$mask$mask$mask${mask}365",
    "98111113659811111365|981111136598$hairSpace$mask$mask$mask$mask${mask}365"
  ])
  fun `should obfuscate correctly`(
      inputNumber: String,
      expected: String
  ) {
    assertThat(obfuscator.obfuscate(inputNumber)).isEqualTo(expected)
  }
}
