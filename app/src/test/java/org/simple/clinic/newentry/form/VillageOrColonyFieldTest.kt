package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class VillageOrColonyFieldTest {
  private val villageOrColonyField = VillageOrColonyField(_labelResId = 0)

  @Test
  fun `it returns a missing value error when village or colony is empty`() {
    assertThat(villageOrColonyField.validate(""))
        .containsExactly(ValidationError.MissingValue)
  }

  @Test
  fun `it returns a missing value error when village or colony is blank`() {
    assertThat(villageOrColonyField.validate("      "))
        .containsExactly(ValidationError.MissingValue)
  }

  @Test
  fun `it returns an empty list when village or colony is not empty`() {
    assertThat(villageOrColonyField.validate("Village"))
        .isEmpty()
  }
}
