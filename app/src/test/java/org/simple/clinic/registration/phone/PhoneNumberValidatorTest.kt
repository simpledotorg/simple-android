package org.simple.clinic.registration.phone

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.BLANK
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_LONG
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.MOBILE

class PhoneNumberValidatorTest {

  @Test
  fun `validate phone numbers from India`() {
    val validator = IndianPhoneNumberValidator()
    assertThat(validator.validate("", MOBILE)).isEqualTo(BLANK)
    assertThat(validator.validate("123456789", MOBILE)).isEqualTo(LENGTH_TOO_SHORT)
    assertThat(validator.validate("1234567890", MOBILE)).isEqualTo(VALID)

    assertThat(validator.validate("", LANDLINE_OR_MOBILE)).isEqualTo(BLANK)
    assertThat(validator.validate("91823", LANDLINE_OR_MOBILE)).isEqualTo(LENGTH_TOO_SHORT)
    assertThat(validator.validate("1982322", LANDLINE_OR_MOBILE)).isEqualTo(VALID)
    assertThat(validator.validate("129391872", LANDLINE_OR_MOBILE)).isEqualTo(VALID)
    assertThat(validator.validate("98681972638734", LANDLINE_OR_MOBILE)).isEqualTo(LENGTH_TOO_LONG)
  }
}
