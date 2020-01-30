package org.simple.clinic.bloodsugar.history

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.patient.Patient
import java.util.UUID

@Parcelize
data class BloodSugarHistoryScreenModel(
    val patientUuid: UUID,
    val patient: Patient?,
    val bloodSugars: List<BloodSugarMeasurement>?
) : Parcelable {
  companion object {
    fun create(patientUuid: UUID) = BloodSugarHistoryScreenModel(patientUuid, null, null)
  }

  val hasLoadedPatient: Boolean
    get() = patient != null

  val hasLoadedBloodSugars: Boolean
    get() = bloodSugars != null

  fun patientLoaded(patient: Patient): BloodSugarHistoryScreenModel =
      copy(patient = patient)

  fun bloodSugarsLoaded(bloodSugars: List<BloodSugarMeasurement>): BloodSugarHistoryScreenModel =
      copy(bloodSugars = bloodSugars)
}
