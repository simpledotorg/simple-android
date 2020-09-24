package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.graphics.Bitmap

interface TeleconsultDoctorInfoUiActions {
  fun setMedicalRegistrationId(medicalRegistrationId: String)
  fun setSignatureBitmap(bitmap: Bitmap)
  fun showAddSignatureDialog()
  fun showAddSignatureButton()
}
