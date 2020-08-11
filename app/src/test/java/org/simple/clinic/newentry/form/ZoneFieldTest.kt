package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.MissingValue

class ZoneFieldTest {
  private val zoneField = ZoneField(_labelResId = 0)

  @Test
  fun `it returns a missing value error when village or colony is empty`() {
    assertThat(zoneField.validate(""))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns a missing value error when village or colony is blank`() {
    assertThat(zoneField.validate("      "))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns an empty list when village or colony is not empty`() {
    assertThat(zoneField.validate("Zone"))
        .isEmpty()
  }
}
