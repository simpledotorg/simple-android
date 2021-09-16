package org.simple.clinic.registration.phone

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber

class MinimumLengthBasedNumberValidatorTest {

  private val validator = PhoneNumberValidator(minimumRequiredLength = 6)

  @Test
  fun `validate phone numbers`() {
    assertThat(validator.validate("")).isEqualTo(Blank)
    assertThat(validator.validate("1234")).isEqualTo(LengthTooShort(minimumAllowedNumberLength = 6))
    assertThat(validator.validate("1234567890")).isEqualTo(ValidNumber)
    assertThat(validator.validate("234567")).isEqualTo(ValidNumber)
  }
}
