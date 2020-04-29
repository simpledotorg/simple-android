package org.simple.clinic.bp.entry

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.bp.BloodPressureReading
import org.simple.clinic.bp.ValidationResult

class BpValidationTest {

  @Test
  fun `when systolic is less than diastolic, return error`() {
    val systolic = 90
    val diastolic = 140
    val result = BloodPressureReading(systolic, diastolic).validate()

    assertThat(result).isEqualTo(ValidationResult.ErrorSystolicLessThanDiastolic)
  }

  @Test
  fun `when systolic is less than minimum possible, return error`() {
    val systolic = 55
    val diastolic = 55
    val result = BloodPressureReading(systolic, diastolic).validate()

    assertThat(result).isEqualTo(ValidationResult.ErrorSystolicTooLow)
  }

  @Test
  fun `when systolic is more than maximum possible, return error`() {
    val systolic = 333
    val diastolic = 88
    val result = BloodPressureReading(systolic, diastolic).validate()

    assertThat(result).isEqualTo(ValidationResult.ErrorSystolicTooHigh)
  }

  @Test
  fun `when diastolic is less than minimum possible, return error`() {
    val systolic = 110
    val diastolic = 33
    val result = BloodPressureReading(systolic, diastolic).validate()

    assertThat(result).isEqualTo(ValidationResult.ErrorDiastolicTooLow)
  }

  @Test
  fun `when diastolic is more than maximum possible, return error`() {
    val systolic = 233
    val diastolic = 190
    val result = BloodPressureReading(systolic, diastolic).validate()

    assertThat(result).isEqualTo(ValidationResult.ErrorDiastolicTooHigh)
  }
}
