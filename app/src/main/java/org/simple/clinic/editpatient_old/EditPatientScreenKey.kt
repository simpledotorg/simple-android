package org.simple.clinic.editpatient_old

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class EditPatientScreenKey(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?
) : FullScreenKey {
  companion object {
    fun fromPatientData(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?
    ): EditPatientScreenKey {
      return EditPatientScreenKey(patient, address, phoneNumber)
    }
  }

  @IgnoredOnParcel
  override val analyticsName = "Edit Patient"

  override fun layoutRes() = R.layout.screen_patient_edit_old
}
