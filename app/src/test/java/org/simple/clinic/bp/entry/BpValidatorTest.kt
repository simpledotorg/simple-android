package org.simple.clinic.bp.entry

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BpValidatorTest {
  private val bpValidator = BpValidator()

  @Test
  fun `when systolic is less than diastolic, return error`() {
    val systolic = "90"
    val diastolic = "140"
    val result = bpValidator.validate(systolic, diastolic)

    assertThat(result).isEqualTo(BpValidator.Validation.ErrorSystolicLessThanDiastolic)
  }

  @Test
  fun `when systolic is less than minimum possible, return error`() {
    val systolic = "55"
    val diastolic = "55"
    val result = bpValidator.validate(systolic, diastolic)

    assertThat(result).isEqualTo(BpValidator.Validation.ErrorSystolicTooLow)
  }

  @Test
  fun `when systolic is more than maximum possible, return error`() {
    val systolic = "333"
    val diastolic = "88"
    val result = bpValidator.validate(systolic, diastolic)

    assertThat(result).isEqualTo(BpValidator.Validation.ErrorSystolicTooHigh)
  }

  @Test
  fun `when diastolic is less than minimum possible, return error`() {
    val systolic = "110"
    val diastolic = "33"
    val result = bpValidator.validate(systolic, diastolic)

    assertThat(result).isEqualTo(BpValidator.Validation.ErrorDiastolicTooLow)
  }

  @Test
  fun `when diastolic is more than maximum possible, return error`() {
    val systolic = "233"
    val diastolic = "190"
    val result = bpValidator.validate(systolic, diastolic)

    assertThat(result).isEqualTo(BpValidator.Validation.ErrorDiastolicTooHigh)
  }

  @Test
  fun `when systolic is empty, return error`() {
    val systolic = ""
    val diastolic = "190"
    val result = bpValidator.validate(systolic, diastolic)

    assertThat(result).isEqualTo(BpValidator.Validation.ErrorSystolicEmpty)
  }

  @Test
  fun `when diastolic is empty, return error`() {
    val systolic = "120"
    val diastolic = ""
    val result = bpValidator.validate(systolic, diastolic)

    assertThat(result).isEqualTo(BpValidator.Validation.ErrorDiastolicEmpty)
  }
}
