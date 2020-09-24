package org.simple.clinic.signature

import android.graphics.Bitmap

interface SignatureUiActions {
  fun clearSignature()
  fun closeScreen()
  fun setSignatureBitmap(signatureBitmap: Bitmap)
}
