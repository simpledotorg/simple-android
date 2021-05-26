package org.simple.clinic.registration.phone

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
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

  sealed class Result : Parcelable {
    @Parcelize
    object ValidNumber : Result()

    @Parcelize
    data class LengthTooShort(val minimumAllowedNumberLength: Int) : Result()

    @Parcelize
    data class LengthTooLong(val maximumRequiredNumberLength: Int) : Result()

    @Parcelize
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
        validateMobile(number, minimumRequiredLengthMobile, maximumAllowedLengthMobile)
      }
      LANDLINE_OR_MOBILE -> {
        validateLandlinesOrMobile(number, minimumRequiredLengthLandlinesOrMobile, maximumAllowedLengthLandlinesOrMobile)
      }
    }
  }

  fun validateLandlinesOrMobile(
      number: String,
      minimumRequiredLength: Int,
      maximumAllowedLength: Int
  ): Result {
    return when {
      number.isBlank() -> Blank
      number.length < minimumRequiredLength -> LengthTooShort(minimumRequiredLength)
      number.length > maximumAllowedLength -> LengthTooLong(maximumAllowedLength)
      else -> ValidNumber
    }
  }

  private fun validateMobile(
      number: String,
      minimumRequiredLength: Int,
      maximumAllowedLength: Int
  ): Result {
    return when {
      number.isBlank() -> Blank
      number.length < minimumRequiredLength -> LengthTooShort(minimumRequiredLength)
      number.length > maximumAllowedLength -> LengthTooLong(maximumAllowedLength)
      else -> ValidNumber
    }
  }
}
