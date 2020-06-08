package org.simple.clinic.registration.phone

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.util.Just

class RegistrationPhoneUpdate : Update<RegistrationPhoneModel, RegistrationPhoneEvent, RegistrationPhoneEffect> {

  override fun update(
      model: RegistrationPhoneModel,
      event: RegistrationPhoneEvent
  ): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    return when (event) {
      is RegistrationPhoneNumberTextChanged -> next(model.phoneNumberChanged(event.phoneNumber))
      is CurrentRegistrationEntryLoaded -> currentEntryLoaded(event, model)
      is NewRegistrationEntryCreated -> next(model.withEntry(event.entry))
      is RegistrationPhoneDoneClicked -> dispatch(ValidateEnteredNumber(model.ongoingRegistrationEntry!!.phoneNumber!!))
      is EnteredNumberValidated -> next(model.phoneNumberValidated(event.result))
      is FacilitiesSynced -> noChange()
      is SearchForExistingUserCompleted -> noChange()
      is UserCreatedLocally -> noChange()
      is CurrentRegistrationEntryCleared -> noChange()
      is CurrentUserUnauthorizedStatusLoaded -> showUserLoggedOut(event)
    }
  }

  private fun currentEntryLoaded(
      event: CurrentRegistrationEntryLoaded,
      model: RegistrationPhoneModel
  ): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    val savedEntry = event.entry

    return if (savedEntry is Just)
      next(model.withEntry(savedEntry.value), PrefillFields(savedEntry.value) as RegistrationPhoneEffect)
    else
      dispatch(CreateNewRegistrationEntry as RegistrationPhoneEffect)
  }

  private fun showUserLoggedOut(event: CurrentUserUnauthorizedStatusLoaded): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    return if (event.isUserUnauthorized) {
      dispatch(ShowUserLoggedOutAlert as RegistrationPhoneEffect)
    } else {
      noChange()
    }
  }
}
