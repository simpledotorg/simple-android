package org.simple.clinic.registration.name

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class RegistrationNameUpdate: Update<RegistrationNameModel, RegistrationNameEvent, RegistrationNameEffect> {

  override fun update(model: RegistrationNameModel, event: RegistrationNameEvent): Next<RegistrationNameModel, RegistrationNameEffect> {
    return noChange()
  }
}
