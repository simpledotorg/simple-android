package org.simple.clinic.editpatient

import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.router.screen.FullScreenKey
import java.util.UUID

sealed class PatientEditScreenKey(open val patientUuid: UUID) : FullScreenKey {
  companion object {
    @Deprecated("Use `fromPatientData` instead.")
    fun fromPatientUuid(uuid: UUID): PatientEditScreenKey =
        PatientEditScreenKeyWithUuid(uuid)

    fun fromPatientData(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?
    ): PatientEditScreenKey {
      return PatientEditScreenKeyWithData(patient, address, phoneNumber)
    }
  }

  @IgnoredOnParcel
  override val analyticsName = "Edit Patient"

  override fun layoutRes() = R.layout.screen_patient_edit
}

@Parcelize
data class PatientEditScreenKeyWithUuid(
    override val patientUuid: UUID
) : PatientEditScreenKey(patientUuid)

@Parcelize
data class PatientEditScreenKeyWithData(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?
) : PatientEditScreenKey(patient.uuid)
