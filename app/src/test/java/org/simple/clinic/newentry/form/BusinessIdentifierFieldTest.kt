package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BusinessIdentifierFieldTest {
  private val businessIdentifierField = BusinessIdentifierField()

  @Test
  fun `it returns an empty list when the field is non-empty`() {
    assertThat(businessIdentifierField.validate("12345678"))
        .isEmpty()
  }

  @Test
  fun `it returns a missing value error when the field is empty`() {
    assertThat(businessIdentifierField.validate(""))
        .containsExactly(ValidationError.MissingValue)
  }

  @Test
  fun `it returns a missing value error when the field is blank`() {
    assertThat(businessIdentifierField.validate("    "))
        .containsExactly(ValidationError.MissingValue)
  }
}
