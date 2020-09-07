package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordInfo
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Audio
import java.util.UUID

data class TeleconsultRecordModel(
    val teleconsultRecordId: UUID,
    val teleconsultationType: TeleconsultationType,
    val patientTookMedicines: Answer,
    val patientConsented: Answer
) {

  companion object {

    fun create(teleconsultRecordId: UUID) = TeleconsultRecordModel(
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
}
