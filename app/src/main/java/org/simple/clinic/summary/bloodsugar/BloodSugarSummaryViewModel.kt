package org.simple.clinic.summary.bloodsugar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.facility.Facility
import java.util.UUID

@Parcelize
data class BloodSugarSummaryViewModel(
    val patientUuid: UUID,
    val measurements: List<BloodSugarMeasurement>?,
    val totalRecordedBloodSugarCount: Int?,
    val currentFacility: Facility?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID): BloodSugarSummaryViewModel {
      return BloodSugarSummaryViewModel(patientUuid, null, null, null)
    }
  }

  fun summaryFetched(measurements: List<BloodSugarMeasurement>): BloodSugarSummaryViewModel {
    return copy(measurements = measurements)
  }

  fun countFetched(count: Int): BloodSugarSummaryViewModel =
      copy(totalRecordedBloodSugarCount = count)
}
