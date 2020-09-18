package org.simple.clinic.signature

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import javax.inject.Inject

class SignatureRepository @Inject constructor(
    appContext: Application
) {

  companion object {
    private const val SIGNATURE_FILE_NAME = "doctor_prescription_signature"
    private const val SIGNATURE_FILE_TYPE = "png"
  }

  private val internalFilesDir = appContext.filesDir

  fun getSignatureBitmap(): Bitmap? {
    val signatureFilePath = File(internalFilesDir, "$SIGNATURE_FILE_NAME.$SIGNATURE_FILE_TYPE")
    val bitmapOptions = BitmapFactory.Options().apply {
      inPreferredConfig = Bitmap.Config.ARGB_8888
    }

    return BitmapFactory.decodeFile(signatureFilePath.path, bitmapOptions)
  }

  fun saveSignatureBitmap(bitmap: Bitmap?) {
    val directory = File(internalFilesDir, "$SIGNATURE_FILE_NAME.$SIGNATURE_FILE_TYPE")
    val outputStream = directory.outputStream()

    outputStream.use {
      bitmap?.compress(Bitmap.CompressFormat.PNG, 70, outputStream)
      it.flush()
    }
  }
}
