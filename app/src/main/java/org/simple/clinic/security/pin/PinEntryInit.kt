package org.simple.clinic.security.pin

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class PinEntryInit : Init<PinEntryModel, PinEntryEffect> {

  override fun init(model: PinEntryModel): First<PinEntryModel, PinEntryEffect> {
    return first(model)
  }
}
