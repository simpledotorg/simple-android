package org.simple.clinic.deeplink

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class DeepLinkInit : Init<DeepLinkModel, DeepLinkEffect> {
  override fun init(model: DeepLinkModel): First<DeepLinkModel, DeepLinkEffect> {
    return first(model, FetchUser)
  }
}
