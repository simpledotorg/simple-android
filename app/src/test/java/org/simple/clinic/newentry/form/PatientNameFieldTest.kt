package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.MissingValue

class PatientNameFieldTest {
  private val patientNameField = PatientNameField(_labelResId = 0)

  @Test
  fun `it returns an empty list when the field is non-empty`() {
    assertThat(patientNameField.validate("Ajay"))
        .isEmpty()
  }

  @Test
  fun `it returns a missing value error when the field is empty`() {
    assertThat(patientNameField.validate(""))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns a missing value error when the field is blank`() {
    assertThat(patientNameField.validate("    "))
        .containsExactly(MissingValue)
  }
}
