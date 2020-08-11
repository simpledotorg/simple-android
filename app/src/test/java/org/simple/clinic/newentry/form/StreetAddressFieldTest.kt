package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.MissingValue

class StreetAddressFieldTest {
  private val streetAddressField = StreetAddressField(_labelResId = 0)

  @Test
  fun `it returns a missing value error when street address is empty`() {
    assertThat(streetAddressField.validate(""))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns a missing value error when street address is blank`() {
    assertThat(streetAddressField.validate("      "))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns an empty list when street address is not empty`() {
    assertThat(streetAddressField.validate("17, Some Street"))
        .isEmpty()
  }
}
