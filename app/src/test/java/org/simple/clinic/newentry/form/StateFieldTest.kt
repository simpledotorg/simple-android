package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.MissingValue

class StateFieldTest {
  private val stateField = StateField(_labelResId = 0)

  @Test
  fun `it returns a missing value error when state is empty`() {
    assertThat(stateField.validate(""))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns a missing value error when state is blank`() {
    assertThat(stateField.validate("      "))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns an empty list when state is not empty`() {
    assertThat(stateField.validate("Punjab"))
        .isEmpty()
  }
}
