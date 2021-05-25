package org.simple.clinic.summary.addphone

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber

class AddPhoneNumberUpdate : Update<AddPhoneNumberModel, AddPhoneNumberEvent, AddPhoneNumberEffect> {

  override fun update(
      model: AddPhoneNumberModel,
      event: AddPhoneNumberEvent
  ): Next<AddPhoneNumberModel, AddPhoneNumberEffect> {
    return when (event) {
      PhoneNumberAdded -> dispatch(CloseDialog)
      is PhoneNumberValidated -> phoneNumberValidated(model, event)
      is AddPhoneNumberSaveClicked -> dispatch(ValidatePhoneNumber(event.number))
    }
  }

  private fun phoneNumberValidated(
      model: AddPhoneNumberModel,
      event: PhoneNumberValidated
  ): Next<AddPhoneNumberModel, AddPhoneNumberEffect> {
    val updatedModel = model.validatedPhoneNumber(event.validationResult)

    return if (event.validationResult == ValidNumber) {
      next(updatedModel, AddPhoneNumber(model.patientUuid, event.newNumber))
    } else {
      next(updatedModel)
    }
  }
}
