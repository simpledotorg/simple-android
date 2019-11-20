package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.MissingValue

class AgeFieldTest {
  private val ageField = AgeField()

  @Test
  fun `it returns an error when the field is empty`() {
    assertThat(ageField.validate(""))
        .containsExactly(MissingValue)
        .inOrder()
  }

  @Test
  fun `it returns an empty list when the field is not empty`() {
    assertThat(ageField.validate("45"))
        .isEmpty()
  }
}
