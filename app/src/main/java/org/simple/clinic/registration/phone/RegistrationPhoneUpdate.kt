package org.simple.clinic.registration.phone

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class RegistrationPhoneUpdate : Update<RegistrationPhoneModel, RegistrationPhoneEvent, RegistrationPhoneEffect> {

  override fun update(
      model: RegistrationPhoneModel,
      event: RegistrationPhoneEvent
  ): Next<RegistrationPhoneModel, RegistrationPhoneEffect> {
    return when(event) {
      is RegistrationPhoneNumberTextChanged -> next(model.phoneNumberChanged(event.phoneNumber))
      is CurrentRegistrationEntryLoaded -> noChange()
    }
  }
}
