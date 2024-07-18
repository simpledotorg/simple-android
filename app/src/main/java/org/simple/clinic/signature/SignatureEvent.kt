package org.simple.clinic.signature

import android.graphics.Bitmap
import org.simple.clinic.widgets.UiEvent

sealed class SignatureEvent : UiEvent

data object UndoClicked : SignatureEvent() {
  override val analyticsName: String = "Add Signature Dialog:Undo Clicked"
}

data class AcceptClicked(val bitmap: Bitmap?) : SignatureEvent() {
  override val analyticsName: String = "Add Signature Dialog:Accept Signature Clicked"
}

data object SignatureAccepted : SignatureEvent()

data class SignatureBitmapLoaded(val bitmap: Bitmap?) : SignatureEvent()
