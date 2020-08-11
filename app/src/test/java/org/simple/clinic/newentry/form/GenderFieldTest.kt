package org.simple.clinic.newentry.form

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.newentry.form.ValidationError.MissingValue
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender

class GenderFieldTest {
  private val genderField = GenderField(_labelResId = 0, allowedGenders = setOf(Male, Female, Transgender))

  @Test
  fun `it returns a missing value error if gender is absent`() {
    assertThat(genderField.validate(null))
        .containsExactly(MissingValue)
  }

  @Test
  fun `it returns an empty list if gender is present`() {
    assertThat(genderField.validate(Female))
        .isEmpty()
  }
}
