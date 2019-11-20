package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.MissingValue

class PatientNameFieldTest {
  @Test
  fun `it returns an empty list when the field is non-empty`() {
    val validationErrors = PatientNameField().validate("Ajay")
    assertThat(validationErrors)
        .isEmpty()
  }

  @Test
  fun `it returns an error when the field is empty`() {
    val validationErrors = PatientNameField().validate("")
    assertThat(validationErrors)
        .containsExactly(MissingValue)
        .inOrder()
  }

  @Test
  fun `it returns an error when the field is blank`() {
    val validationErrors = PatientNameField().validate("    ")
    assertThat(validationErrors)
        .containsExactly(MissingValue)
        .inOrder()
  }
}
