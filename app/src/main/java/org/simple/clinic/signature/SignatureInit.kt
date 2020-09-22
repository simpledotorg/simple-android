package org.simple.clinic.signature

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class SignatureInit : Init<SignatureModel, SignatureEffect> {

  override fun init(model: SignatureModel): First<SignatureModel, SignatureEffect> {
    return first(model, LoadSignatureBitmap)
  }
}
