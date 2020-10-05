package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap

interface TeleconsultSharePrescriptionUiActions {
  fun setSignatureBitmap(bitmap: Bitmap)
  fun setMedicalRegistrationId(medicalRegistrationId: String)
}
