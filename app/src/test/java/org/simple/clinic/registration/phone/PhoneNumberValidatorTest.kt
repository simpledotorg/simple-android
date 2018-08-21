package org.simple.clinic.registration.phone

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PhoneNumberValidatorTest {

  @Test
  fun `validate Indian phone numbers`() {
    val validator = IndianPhoneNumberValidator()
    assertThat(validator.isValid("123456789")).isFalse()
    assertThat(validator.isValid("1234567890")).isTrue()
  }
}
