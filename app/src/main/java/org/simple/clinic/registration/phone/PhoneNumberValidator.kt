package org.simple.clinic.registration.phone

import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_LONG
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.MOBILE

interface PhoneNumberValidator {
  enum class Type {
    LANDLINE_OR_MOBILE,
    MOBILE
  }

  enum class Result {
    VALID,
    LENGTH_TOO_SHORT,
    LENGTH_TOO_LONG,
    BLANK
  }

  fun validate(number: String, type: Type): Result
}

class LengthBasedNumberValidator(
    val minimumRequiredLengthMobile: Int,
    val maximumAllowedLengthMobile: Int,
    val minimumRequiredLengthLandlinesOrMobile: Int,
    val maximumAllowedLengthLandlinesOrMobile: Int
) : PhoneNumberValidator {
  override fun validate(number: String, type: PhoneNumberValidator.Type): Result {
    return when (type) {
      MOBILE -> {
        when {
          number.isBlank() -> Result.BLANK
          number.length < minimumRequiredLengthMobile -> LENGTH_TOO_SHORT
          number.length > maximumAllowedLengthMobile -> LENGTH_TOO_LONG
          else -> VALID
        }
      }
      LANDLINE_OR_MOBILE -> {
        when {
          number.isBlank() -> Result.BLANK
          number.length < minimumRequiredLengthLandlinesOrMobile -> LENGTH_TOO_SHORT
          number.length > maximumAllowedLengthLandlinesOrMobile -> LENGTH_TOO_LONG
          else -> VALID
        }
      }
    }
  }
}
