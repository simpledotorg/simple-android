package org.simple.clinic.signature

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class SignatureUpdate : Update<SignatureModel, SignatureEvent, SignatureEffect> {
  override fun update(
      model: SignatureModel,
      event: SignatureEvent
  ): Next<SignatureModel, SignatureEffect> {
    return noChange()
  }
}
