package org.simple.clinic.patient.shortcode

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class DigitFilterTest {

  @Test
  @Parameters(value = [
    "a|false",
    "A|false",
    "1|true",
    "b|false",
    "c|false",
    "2|true",
    "D|false",
    "Z|false",
    "-|false",
    "(|false",
    "3|true",
    ")|false",
    "*|false",
    "$|false",
    "4|true",
    "-|false",
    "#|false",
    "5|true",
    "6|true",
    "7|true",
    "8|true",
    "9|true",
    "0|true"
  ])
  fun `it should filter only digits`(
      char: Char,
      shouldFilterCharacter: Boolean
  ) {
    val digitFilter = DigitFilter()

    val characterFiltered = digitFilter.filter(char)

    assertThat(characterFiltered).isEqualTo(shouldFilterCharacter)
  }
}
