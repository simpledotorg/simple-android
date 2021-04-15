package org.simple.clinic.registration.phone

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class RegistrationPhoneValidationResult : Parcelable {

  @Parcelize
  object Valid : RegistrationPhoneValidationResult()

  sealed class Invalid : RegistrationPhoneValidationResult() {

    @Parcelize
    object TooShort : Invalid()

    @Parcelize
    object TooLong : Invalid()

    @Parcelize
    object Blank : Invalid()
  }
}
