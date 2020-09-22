package org.simple.clinic.signature

import android.graphics.Bitmap

sealed class SignatureEvent

object UndoClicked : SignatureEvent()

data class AcceptClicked(
    val bitmap: Bitmap?
) : SignatureEvent()

object SignatureAccepted : SignatureEvent()

data class SignatureBitmapLoaded(val bitmap: Bitmap?) : SignatureEvent()
