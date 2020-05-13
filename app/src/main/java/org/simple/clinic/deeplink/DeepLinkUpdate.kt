package org.simple.clinic.deeplink

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class DeepLinkUpdate : Update<DeepLinkModel, DeepLinkEvent, DeepLinkEffect> {

  override fun update(model: DeepLinkModel, event: DeepLinkEvent): Next<DeepLinkModel, DeepLinkEffect> {
    return dispatch(NavigateToSetupActivity)
  }
}
