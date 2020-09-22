package org.simple.clinic.signature

import android.graphics.Bitmap

sealed class SignatureEffect

object ClearSignature : SignatureEffect()

data class AcceptSignature(val bitmap: Bitmap?) : SignatureEffect()

object CloseScreen : SignatureEffect()

object LoadSignatureBitmap : SignatureEffect()
