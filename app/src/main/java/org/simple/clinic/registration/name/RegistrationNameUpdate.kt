package org.simple.clinic.registration.name

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class RegistrationNameUpdate : Update<RegistrationNameModel, RegistrationNameEvent, RegistrationNameEffect> {

  override fun update(
      model: RegistrationNameModel,
      event: RegistrationNameEvent
  ): Next<RegistrationNameModel, RegistrationNameEffect> {
    return when (event) {
      is RegistrationFullNameTextChanged -> next(model.nameChanged(event.fullName))
      is NameValidated -> nameValidated(model, event)
      is RegistrationFullNameDoneClicked -> dispatch(ValidateEnteredName(model.ongoingRegistrationEntry.fullName!!) as RegistrationNameEffect)
    }
  }

  private fun nameValidated(
      model: RegistrationNameModel,
      event: NameValidated
  ): Next<RegistrationNameModel, RegistrationNameEffect> {
    val updatedModel = model.nameValidated(event.result)

    return if (updatedModel.isEnteredNameValid)
      next(updatedModel, ProceedToPinEntry(model.ongoingRegistrationEntry) as RegistrationNameEffect)
    else
      next(updatedModel)
  }
}
