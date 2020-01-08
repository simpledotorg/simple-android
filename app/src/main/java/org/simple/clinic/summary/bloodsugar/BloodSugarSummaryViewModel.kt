package org.simple.clinic.summary.bloodsugar

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import java.util.UUID

@Parcelize
data class BloodSugarSummaryViewModel(
    val patientUuid: UUID,
    val measurements: List<BloodSugarMeasurement>?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID): BloodSugarSummaryViewModel {
      return BloodSugarSummaryViewModel(patientUuid, null)
    }
  }

  fun summaryFetched(measurements: List<BloodSugarMeasurement>): BloodSugarSummaryViewModel {
    return copy(measurements = measurements)
  }
}
