package org.simple.clinic.registration.phone

import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooLong
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.MOBILE

interface PhoneNumberValidator {
  enum class Type {
    LANDLINE_OR_MOBILE,
    MOBILE
  }

  sealed class Result {
    object ValidNumber : Result()
    data class LengthTooShort(val minimumAllowedNumberLength: Int) : Result()
    data class LengthTooLong(val maximumRequiredNumberLength: Int) : Result()
    object Blank : Result()
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
          number.isBlank() -> Blank
          number.length < minimumRequiredLengthMobile -> LengthTooShort(minimumRequiredLengthMobile)
          number.length > maximumAllowedLengthMobile -> LengthTooLong(maximumAllowedLengthMobile)
          else -> ValidNumber
        }
      }
      LANDLINE_OR_MOBILE -> {
        when {
          number.isBlank() -> Blank
          number.length < minimumRequiredLengthLandlinesOrMobile -> LengthTooShort(minimumRequiredLengthLandlinesOrMobile)
          number.length > maximumAllowedLengthLandlinesOrMobile -> LengthTooLong(maximumAllowedLengthLandlinesOrMobile)
          else -> ValidNumber
        }
      }
    }
  }
}
