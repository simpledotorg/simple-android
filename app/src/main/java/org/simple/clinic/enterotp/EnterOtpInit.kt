package org.simple.clinic.enterotp

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class EnterOtpInit : Init<EnterOtpModel, EnterOtpEffect> {

  override fun init(model: EnterOtpModel): First<EnterOtpModel, EnterOtpEffect> {
    val effects = mutableSetOf<EnterOtpEffect>(ListenForUserBackgroundVerification)

    if (!model.hasLoadedUser) {
      effects.add(LoadUser)
    }

    return first(model, effects)
  }
}
