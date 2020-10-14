package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap
import android.net.Uri

interface TeleconsultSharePrescriptionUiActions {
  fun setSignatureBitmap(bitmap: Bitmap)
  fun setMedicalRegistrationId(medicalRegistrationId: String)
  fun openHomeScreen()
  fun sharePrescriptionAsImage(imageUri: Uri)
  fun goToPreviousScreen()
  fun showImageSavedToast()
}
