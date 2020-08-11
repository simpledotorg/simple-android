package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DistrictFieldTest {
  private val districtField = DistrictField(_labelResId = 0)

  @Test
  fun `it returns a missing value error when district is empty`() {
    assertThat(districtField.validate(""))
        .containsExactly(ValidationError.MissingValue)
  }

  @Test
  fun `it returns a missing value error when district is blank`() {
    assertThat(districtField.validate("      "))
        .containsExactly(ValidationError.MissingValue)
  }

  @Test
  fun `it returns an empty list when district is not empty`() {
    assertThat(districtField.validate("Bagta"))
        .isEmpty()
  }
}
