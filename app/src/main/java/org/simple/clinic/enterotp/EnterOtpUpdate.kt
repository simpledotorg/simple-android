package org.simple.clinic.enterotp

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class EnterOtpUpdate : Update<EnterOtpModel, EnterOtpEvent, EnterOtpEffect> {

  override fun update(model: EnterOtpModel, event: EnterOtpEvent): Next<EnterOtpModel, EnterOtpEffect> {
    return noChange()
  }
}
