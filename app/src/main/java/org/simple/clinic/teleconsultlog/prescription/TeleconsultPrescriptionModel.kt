package org.simple.clinic.teleconsultlog.prescription

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.Patient
import java.util.UUID

@Parcelize
data class TeleconsultPrescriptionModel(
    val teleconsultRecordId: UUID,
    val patientUuid: UUID,
    val patient: Patient?
) : Parcelable {

  companion object {

    fun create(teleconsultRecordId: UUID, patientUuid: UUID) = TeleconsultPrescriptionModel(
        teleconsultRecordId = teleconsultRecordId,
        patientUuid = patientUuid,
        patient = null
    )
  }

  val hasPatient: Boolean
    get() = patient != null

  fun patientLoaded(patient: Patient): TeleconsultPrescriptionModel {
    return copy(patient = patient)
  }
}
