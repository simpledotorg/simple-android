package org.simple.clinic.summary.bloodpressures

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bp.BloodPressureMeasurement
import java.util.UUID

@Parcelize
data class NewBloodPressureSummaryViewModel(
    val patientUuid: UUID,
    val latestBloodPressuresToDisplay: List<BloodPressureMeasurement>?,
    val totalRecordedBloodPressureCount: Int?
) : Parcelable {
  companion object {
    fun create(patientUuid: UUID) = NewBloodPressureSummaryViewModel(patientUuid, null, null)
  }

  fun bloodPressuresLoaded(bloodPressures: List<BloodPressureMeasurement>): NewBloodPressureSummaryViewModel =
      copy(latestBloodPressuresToDisplay = bloodPressures)

  fun bloodPressuresCountLoaded(bloodPressuresCount: Int): NewBloodPressureSummaryViewModel =
      copy(totalRecordedBloodPressureCount = bloodPressuresCount)
}
