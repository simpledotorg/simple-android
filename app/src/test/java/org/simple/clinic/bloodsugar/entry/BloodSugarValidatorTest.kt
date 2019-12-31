package org.simple.clinic.bloodsugar.entry

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarEmpty
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooHigh
import org.simple.clinic.bloodsugar.entry.BloodSugarValidator.Result.ErrorBloodSugarTooLow

class BloodSugarValidatorTest {
  private val bloodSugarValidator = BloodSugarValidator()

  @Test
  fun `when blood sugar reading is empty, return error`() {
    val bloodSugarReading = ""
    val result = bloodSugarValidator.validate(bloodSugarReading)

    assertThat(result).isEqualTo(ErrorBloodSugarEmpty)
  }

  @Test
  fun `when blood sugar reading is more than maximum possible, return error`() {
    val bloodSugarReading = "1001"
    val result = bloodSugarValidator.validate(bloodSugarReading)

    assertThat(result).isEqualTo(ErrorBloodSugarTooHigh)
  }

  @Test
  fun `when blood sugar reading is less than minimum possible, return error`() {
    val bloodSugarReading = "29"
    val result = bloodSugarValidator.validate(bloodSugarReading)

    assertThat(result).isEqualTo(ErrorBloodSugarTooLow)
  }
}
