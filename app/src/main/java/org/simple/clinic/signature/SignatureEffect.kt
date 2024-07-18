package org.simple.clinic.signature

import android.graphics.Bitmap

sealed class SignatureEffect

data object ClearSignature : SignatureEffect()

data class AcceptSignature(val bitmap: Bitmap?) : SignatureEffect()

data object CloseScreen : SignatureEffect()

data object LoadSignatureBitmap : SignatureEffect()

data class SetSignatureBitmap(val bitmap: Bitmap) : SignatureEffect()
