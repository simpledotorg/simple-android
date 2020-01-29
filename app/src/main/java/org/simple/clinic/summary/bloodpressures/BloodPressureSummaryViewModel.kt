package org.simple.clinic.summary.bloodpressures

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bp.BloodPressureMeasurement
import java.util.UUID

@Parcelize
data class BloodPressureSummaryViewModel(
    val patientUuid: UUID,
    val latestBloodPressuresToDisplay: List<BloodPressureMeasurement>?,
    val totalRecordedBloodPressureCount: Int?
) : Parcelable {
  companion object {
    fun create(patientUuid: UUID) = BloodPressureSummaryViewModel(patientUuid, null, null)
  }

  fun bloodPressuresLoaded(bloodPressures: List<BloodPressureMeasurement>): BloodPressureSummaryViewModel =
      copy(latestBloodPressuresToDisplay = bloodPressures)

  fun bloodPressuresCountLoaded(bloodPressuresCount: Int): BloodPressureSummaryViewModel =
      copy(totalRecordedBloodPressureCount = bloodPressuresCount)
}
