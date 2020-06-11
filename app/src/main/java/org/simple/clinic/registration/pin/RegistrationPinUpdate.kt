package org.simple.clinic.registration.pin

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class RegistrationPinUpdate(
    private val requiredPinLength: Int
) : Update<RegistrationPinModel, RegistrationPinEvent, RegistrationPinEffect> {

  override fun update(model: RegistrationPinModel, event: RegistrationPinEvent): Next<RegistrationPinModel, RegistrationPinEffect> {
    return when(event) {
      is CurrentOngoingEntrySaved -> dispatch(ProceedToConfirmPin)
      is RegistrationPinTextChanged -> next(model.pinChanged(event.pin))
    }
  }
}
