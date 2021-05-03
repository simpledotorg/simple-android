package org.simple.clinic.scanid

import com.google.common.truth.Truth.assertThat
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.Empty
import org.simple.clinic.scanid.EnteredCodeValidationResult.Failure.NotEqualToRequiredLength
import org.simple.clinic.scanid.EnteredCodeValidationResult.Success

@RunWith(JUnitParamsRunner::class)
class EnteredCodeInputTest {

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
    val shortCodeInput = EnteredCodeInput(shortCodeText = input)

    //when
    val result = shortCodeInput.validate()

    //then
    assertThat(result)
        .isEqualTo(NotEqualToRequiredLength)
  }

  @Test
  fun `when short code length is equal to 7 then validation should succeed`() {
    //given
    val shortCodeInput = EnteredCodeInput(shortCodeText = "3456789")

    //when
    val result = shortCodeInput.validate()

    //then
    assertThat(result)
        .isEqualTo(Success)
  }

  @Test
  fun `when short code is empty, then validation should fail with empty`() {
    // when
    val result = EnteredCodeInput("").validate()

    // then
    assertThat(result)
        .isEqualTo(Empty)
  }

  @Test
  fun `when entered code length is equal to 14 then validation should succeed`() {
    //given
    val enteredCodeInput = ShortCodeInput(shortCodeText = "34567899876543")

    //when
    val result = enteredCodeInput.validate()

    //then
    assertThat(result)
        .isEqualTo(Success)
  }

}
