package org.simple.clinic.bloodsugar.history

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.Patient
import java.util.UUID

@Parcelize
data class BloodSugarHistoryScreenModel(
    val patientUuid: UUID,
    val patient: Patient?
) : Parcelable {
  companion object {
    fun create(patientUuid: UUID) = BloodSugarHistoryScreenModel(patientUuid, null)
  }

  val hasLoadedPatient: Boolean
    get() = patient != null

  fun patientLoaded(patient: Patient): BloodSugarHistoryScreenModel =
      copy(patient = patient)
}
