package org.simple.clinic.editpatient.deletepatient

import java.util.UUID

data class DeletePatientModel(
    val patientUuid: UUID,
    val patientName: String? = null
) {

  val hasPatientName: Boolean
    get() = patientName.isNullOrBlank().not()

  fun patientNameLoaded(patientName: String): DeletePatientModel {
    return copy(patientName = patientName)
  }

  companion object {

    fun default(patientUuid: UUID) = DeletePatientModel(patientUuid = patientUuid, patientName = null)
  }
}
