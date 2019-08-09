package org.simple.clinic.scanid

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class ShortCodeInputTest {

  @Parameters(value = [
    "3",
    "34",
    "345",
    "3456",
    "34567",
    "345678",
    "34567890"
  ])
  @Test
  fun `when short code length is not equal to 7 then validation should fail`(input: String) {
    //given
    val shortCodeInput = ShortCodeInput(shortCodeText = input)

    //when
    val isValid = shortCodeInput.isValid()

    //then
    assertThat(isValid).isFalse()
  }

  @Test
  fun `when short code length is equal to 7 then validation should succeed`() {
    //given
    val shortCodeInput = ShortCodeInput(shortCodeText = "3456789")

    //when
    val isValid = shortCodeInput.isValid()

    //then
    assertThat(isValid).isTrue()
  }
}
