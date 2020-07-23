package org.simple.clinic.enterotp

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class EnterOtpUpdate : Update<EnterOtpModel, EnterOtpEvent, EnterOtpEffect> {

  override fun update(model: EnterOtpModel, event: EnterOtpEvent): Next<EnterOtpModel, EnterOtpEffect> {
    return when (event) {
      is UserLoaded -> next(model.userLoaded(event.user))
    }
  }
}
