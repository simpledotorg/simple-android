package org.simple.clinic.registration.phone

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber

interface PhoneNumberValidator {

  sealed class Result : Parcelable {
    @Parcelize
    object ValidNumber : Result()

    @Parcelize
    data class LengthTooShort(val minimumAllowedNumberLength: Int) : Result()

    @Parcelize
    object Blank : Result()
  }

  fun validate(number: String): Result
}

class MinimumLengthBasedNumberValidator(
    private val minimumRequiredLength: Int
) : PhoneNumberValidator {

  override fun validate(number: String): Result {
    return when {
      number.isBlank() -> Blank
      number.length < minimumRequiredLength -> LengthTooShort(minimumRequiredLength)
      else -> ValidNumber
    }
  }
}
