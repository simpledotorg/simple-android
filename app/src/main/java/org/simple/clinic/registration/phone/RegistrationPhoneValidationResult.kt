package org.simple.clinic.registration.phone

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class RegistrationPhoneValidationResult : Parcelable {

  @Parcelize
  data object Valid : RegistrationPhoneValidationResult()

  sealed class Invalid : RegistrationPhoneValidationResult() {

    @Parcelize
    data object TooShort : Invalid()

    @Parcelize
    data object Blank : Invalid()
  }
}
