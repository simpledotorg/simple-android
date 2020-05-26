package org.simple.clinic.editpatient.deletepatient

import java.util.UUID

data class DeletePatientModel(
    val patientUuid: UUID,
    val patientName: String? = null
) {

  fun patientNameLoaded(patientName: String): DeletePatientModel {
    return copy(patientName = patientName)
  }

  companion object {

    fun default(patientUuid: UUID) = DeletePatientModel(patientUuid = patientUuid, patientName = null)
  }
}
