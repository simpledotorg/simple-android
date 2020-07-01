package org.simple.clinic.registration.register

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class RegistrationLoadingUpdate : Update<RegistrationLoadingModel, RegistrationLoadingEvent, RegistrationLoadingEffect> {

  override fun update(model: RegistrationLoadingModel, event: RegistrationLoadingEvent): Next<RegistrationLoadingModel, RegistrationLoadingEffect> {
    return when(event) {
      is RegistrationDetailsLoaded -> noChange()
    }
  }
}
