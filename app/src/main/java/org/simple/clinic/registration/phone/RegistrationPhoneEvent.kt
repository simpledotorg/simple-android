package org.simple.clinic.registration.phone

import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.UiEvent

sealed class RegistrationPhoneEvent : UiEvent

data class RegistrationPhoneNumberTextChanged(val phoneNumber: String) : RegistrationPhoneEvent() {
  override val analyticsName = "Registration:Phone Entry:Phone Number Text Changed"
}

data class CurrentRegistrationEntryLoaded(val entry: Optional<OngoingRegistrationEntry>) : RegistrationPhoneEvent()

data class NewRegistrationEntryCreated(val entry: OngoingRegistrationEntry) : RegistrationPhoneEvent()


data class EnteredNumberValidated(val result: RegistrationPhoneValidationResult) : RegistrationPhoneEvent() {

  companion object {
    // The only reason this is being done here is to avoid coupling the `RegistrationPhoneUpdate`
    // to `PhoneNumberValidator`. The right way to fix this is remove the need for a validator
    // class and introduce a type to encapsulate the entered phone number and its validation.
    // TODO (vs) 04/06/20: https://www.pivotaltracker.com/story/show/173170198
    fun fromValidateNumberResult(result: PhoneNumberValidator.Result): EnteredNumberValidated {
      val registrationPhoneValidationResult = when (result) {
        PhoneNumberValidator.Result.VALID -> RegistrationPhoneValidationResult.Valid
        PhoneNumberValidator.Result.LENGTH_TOO_SHORT -> RegistrationPhoneValidationResult.Invalid.TooShort
        PhoneNumberValidator.Result.LENGTH_TOO_LONG -> RegistrationPhoneValidationResult.Invalid.TooLong
        PhoneNumberValidator.Result.BLANK -> RegistrationPhoneValidationResult.Invalid.Blank
      }

      return EnteredNumberValidated(registrationPhoneValidationResult)
    }
  }
}
