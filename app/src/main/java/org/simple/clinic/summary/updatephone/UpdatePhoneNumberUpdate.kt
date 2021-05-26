package org.simple.clinic.summary.updatephone

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.registration.phone.PhoneNumberValidator

class UpdatePhoneNumberUpdate : Update<UpdatePhoneNumberModel, UpdatePhoneNumberEvent, UpdatePhoneNumberEffect> {

  override fun update(
      model: UpdatePhoneNumberModel,
      event: UpdatePhoneNumberEvent
  ): Next<UpdatePhoneNumberModel, UpdatePhoneNumberEffect> {
    return when (event) {
      is PhoneNumberLoaded -> dispatch(PrefillPhoneNumber(event.phoneNumber))
      is PhoneNumberValidated -> phoneNumberValidated(event, model)
      NewPhoneNumberSaved -> dispatch(CloseDialog)
      is UpdatePhoneNumberSaveClicked -> dispatch(ValidatePhoneNumber(event.number))
      ExistingPhoneNumberSaved -> dispatch(CloseDialog)
      UpdatePhoneNumberCancelClicked -> dispatch(SaveExistingPhoneNumber(model.patientUuid))
    }
  }

  private fun phoneNumberValidated(
      event: PhoneNumberValidated,
      model: UpdatePhoneNumberModel
  ): Next<UpdatePhoneNumberModel, UpdatePhoneNumberEffect> {
    val effect = when (val result = event.result) {
      PhoneNumberValidator.Result.ValidNumber -> SaveNewPhoneNumber(model.patientUuid, event.phoneNumber)
      is PhoneNumberValidator.Result.LengthTooShort -> ShowPhoneNumberTooShortError(result.minimumAllowedNumberLength)
      is PhoneNumberValidator.Result.LengthTooLong -> ShowPhoneNumberTooLongError(result.maximumRequiredNumberLength)
      PhoneNumberValidator.Result.Blank -> ShowBlankPhoneNumberError
    }

    return dispatch(effect)
  }
}
