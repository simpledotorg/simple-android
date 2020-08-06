package org.simple.clinic.enterotp

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class EnterOtpInit : Init<EnterOtpModel, EnterOtpEffect> {

  override fun init(model: EnterOtpModel): First<EnterOtpModel, EnterOtpEffect> {
    return first(model)
  }
}
