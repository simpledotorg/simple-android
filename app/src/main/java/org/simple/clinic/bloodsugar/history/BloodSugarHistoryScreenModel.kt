package org.simple.clinic.bloodsugar.history

import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.patient.Patient
import java.util.UUID

data class BloodSugarHistoryScreenModel(
    val patientUuid: UUID,
    val patient: Patient?,
    val bloodSugars: List<BloodSugarMeasurement>?
) {
  companion object {
    fun create(patientUuid: UUID) = BloodSugarHistoryScreenModel(patientUuid, null, null)
  }

  val hasLoadedPatient: Boolean
    get() = patient != null

  fun patientLoaded(patient: Patient): BloodSugarHistoryScreenModel =
      copy(patient = patient)

  fun bloodSugarsLoaded(bloodSugars: List<BloodSugarMeasurement>): BloodSugarHistoryScreenModel =
      copy(bloodSugars = bloodSugars)
}
