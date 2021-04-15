package org.simple.clinic.editpatient

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.router.screen.FullScreenKey

@Parcelize
data class EditPatientScreenKey(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?,
    val bangladeshNationalId: BusinessId?
) : FullScreenKey {
  companion object {
    fun fromPatientData(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?,
        bangladeshNationalId: BusinessId?
    ): EditPatientScreenKey {
      return EditPatientScreenKey(patient, address, phoneNumber, bangladeshNationalId)
    }
  }

  @IgnoredOnParcel
  override val analyticsName = "Edit Patient"

  override fun layoutRes() = R.layout.screen_edit_patient
}
