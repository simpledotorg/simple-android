package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.MissingValue

class AgeFieldTest {
  private val ageField = AgeField(_labelResId = 0)

  @Test
  fun `it returns a missing value error when the field is empty`() {
    assertThat(ageField.validate(""))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns an empty list when the field is not empty`() {
    assertThat(ageField.validate("45"))
        .isEmpty()
  }
}
