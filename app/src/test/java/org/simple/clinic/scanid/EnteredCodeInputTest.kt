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
  fun `when entered code length is not equal to 7 then validation should fail`(input: String) {
    //given
    val enteredCodeInput = EnteredCodeInput(enteredCodeText = input)

    //when
    val result = enteredCodeInput.validate()

    //then
    assertThat(result)
        .isEqualTo(NotEqualToRequiredLength)
  }

  @Test
  fun `when entered code length is equal to 7 then validation should succeed`() {
    //given
    val enteredCodeInput = EnteredCodeInput(enteredCodeText = "3456789")

    //when
    val result = enteredCodeInput.validate()

    //then
    assertThat(result)
        .isEqualTo(Success)
  }

  @Test
  fun `when entered code is empty, then validation should fail with empty`() {
    // when
    val result = EnteredCodeInput("").validate()

    // then
    assertThat(result)
        .isEqualTo(Empty)
  }

  @Test
  fun `when entered code length is equal to 14 then validation should succeed`() {
    //given
    val enteredCodeInput = EnteredCodeInput(enteredCodeText = "34567899876543")

    //when
    val result = enteredCodeInput.validate()

    //then
    assertThat(result)
        .isEqualTo(Success)
  }

  @Parameters(value = [
    "3",
    "34",
    "345",
    "3456",
    "34567",
    "345678",
    "34567890",
    "345678900",
    "3456789001",
    "34567890012",
    "345678900123",
    "3456789001234",
    "345678900123435",
  ])
  @Test
  fun `when entered code length is more than 14 then validation should fail`(input: String) {
    //given
    val enteredCodeInput = EnteredCodeInput(enteredCodeText = input)

    //when
    val result = enteredCodeInput.validate()

    //then
    assertThat(result)
        .isEqualTo(NotEqualToRequiredLength)
  }
}
