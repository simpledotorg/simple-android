package org.simple.clinic.registration.phone

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooLong
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.MOBILE

class PhoneNumberValidatorTest {

  private val validator = LengthBasedNumberValidator(
      10,
      10,
      9,
      10
  )

  @Test
  fun `validate phone numbers from India`() {
    val validator = LengthBasedNumberValidator(
        10,
        10,
        6,
        12
    )
    assertThat(validator.validate("", MOBILE)).isEqualTo(Blank)
    assertThat(validator.validate("1234", MOBILE)).isEqualTo(LengthTooShort(10))
    assertThat(validator.validate("1234567890", MOBILE)).isEqualTo(ValidNumber)

    assertThat(validator.validate("", LANDLINE_OR_MOBILE)).isEqualTo(Blank)
    assertThat(validator.validate("918", LANDLINE_OR_MOBILE)).isEqualTo(LengthTooShort(6))
    assertThat(validator.validate("1982322678", LANDLINE_OR_MOBILE)).isEqualTo(ValidNumber)
    assertThat(validator.validate("1293918728", LANDLINE_OR_MOBILE)).isEqualTo(ValidNumber)
    assertThat(validator.validate("9868197269876", LANDLINE_OR_MOBILE)).isEqualTo(LengthTooLong(12))
  }

  @Test
  fun `validate valid phone numbers from Ethiopia`() {
    assertThat(validator.validate("987654321", LANDLINE_OR_MOBILE)).isEqualTo(ValidNumber)
    assertThat(validator.validate("9865327861", LANDLINE_OR_MOBILE)).isEqualTo(ValidNumber)
  }

  @Test
  fun `validate blank phone numbers from Ethiopia`() {
    assertThat(validator.validate("", LANDLINE_OR_MOBILE)).isEqualTo(Blank)
  }

  @Test
  fun `validate too short phone numbers from Ethiopia `() {
    assertThat(validator.validate("123456", LANDLINE_OR_MOBILE)).isEqualTo(LengthTooShort(9))
  }

  @Test
  fun `validate too long phone numbers from Ethiopia`() {
    assertThat(validator.validate("98653278653", LANDLINE_OR_MOBILE)).isEqualTo(LengthTooLong(10))
  }
}
