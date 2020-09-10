package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.Patient
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordInfo
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Audio
import java.util.UUID

@Parcelize
data class TeleconsultRecordModel(
    val patientUuid: UUID,
    val patient: Patient?,
    val teleconsultRecordId: UUID,
    val teleconsultationType: TeleconsultationType,
    val patientTookMedicines: Answer,
    val patientConsented: Answer
) : Parcelable {

  val hasPatient: Boolean
    get() = patient != null

  companion object {

    fun create(patientUuid: UUID, teleconsultRecordId: UUID) = TeleconsultRecordModel(
        patientUuid = patientUuid,
        patient = null,
        teleconsultRecordId = teleconsultRecordId,
        teleconsultationType = Audio,
        patientTookMedicines = Yes,
        patientConsented = Yes
    )
  }

  fun teleconsultRecordLoaded(teleconsultRecordInfo: TeleconsultRecordInfo): TeleconsultRecordModel {
    return copy(
        teleconsultationType = teleconsultRecordInfo.teleconsultationType,
        patientTookMedicines = teleconsultRecordInfo.patientTookMedicines,
        patientConsented = teleconsultRecordInfo.patientConsented
    )
  }

  fun patientLoaded(patient: Patient): TeleconsultRecordModel {
    return copy(patient = patient)
  }
}
