package org.simple.clinic.signature

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class SignatureUpdate : Update<SignatureModel, SignatureEvent, SignatureEffect> {
  override fun update(
      model: SignatureModel,
      event: SignatureEvent
  ): Next<SignatureModel, SignatureEffect> {
    return when (event) {
      is UndoClicked -> dispatch(ClearSignature)
      is AcceptClicked -> dispatch(AcceptSignature(event.bitmap))
      is SignatureAccepted -> dispatch(CloseScreen)
      is SignatureBitmapLoaded -> signatureBitmapLoaded(event)
    }
  }

  private fun signatureBitmapLoaded(event: SignatureBitmapLoaded): Next<SignatureModel, SignatureEffect> {
    return if (event.bitmap != null) {
      dispatch(SetSignatureBitmap(event.bitmap))
    } else {
      noChange()
    }
  }
}
