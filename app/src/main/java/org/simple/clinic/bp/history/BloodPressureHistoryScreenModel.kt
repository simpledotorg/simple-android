package org.simple.clinic.bp.history

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.Patient
import java.util.UUID

@Parcelize
data class BloodPressureHistoryScreenModel(
    val patientUuid: UUID,
    val patient: Patient?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID) = BloodPressureHistoryScreenModel(patientUuid, null)
  }

  val hasPatient: Boolean
    get() = patient != null

  fun patientLoaded(patient: Patient): BloodPressureHistoryScreenModel =
      copy(patient = patient)
}
