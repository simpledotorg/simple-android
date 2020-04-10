package org.simple.clinic.bp.history

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.patient.Patient
import java.util.UUID

@Parcelize
data class BloodPressureHistoryScreenModel(
    val patientUuid: UUID,
    val patient: Patient?,
    val bloodPressures: List<BloodPressureMeasurement>?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID) = BloodPressureHistoryScreenModel(patientUuid, null, null)
  }

  val hasBloodPressures: Boolean
    get() = bloodPressures != null

  val hasPatient: Boolean
    get() = patient != null

  fun patientLoaded(patient: Patient): BloodPressureHistoryScreenModel =
      copy(patient = patient)
}
