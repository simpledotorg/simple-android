package org.simple.clinic.registration.name

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class RegistrationNameUpdate : Update<RegistrationNameModel, RegistrationNameEvent, RegistrationNameEffect> {

  override fun update(model: RegistrationNameModel, event: RegistrationNameEvent): Next<RegistrationNameModel, RegistrationNameEffect> {
    return when (event) {
      is RegistrationFullNameTextChanged -> next(model.nameChanged(event.fullName))
    }
  }
}
