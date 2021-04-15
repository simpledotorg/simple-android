package org.simple.clinic.summary.bloodsugar

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.summary.PatientSummaryChildModel
import java.util.UUID

@Parcelize
data class BloodSugarSummaryViewModel(
    val patientUuid: UUID,
    val measurements: List<BloodSugarMeasurement>?,
    val totalRecordedBloodSugarCount: Int?
) : Parcelable, PatientSummaryChildModel {

  companion object {
    fun create(patientUuid: UUID): BloodSugarSummaryViewModel {
      return BloodSugarSummaryViewModel(patientUuid, null, null)
    }
  }

  override fun readyToRender(): Boolean {
    return totalRecordedBloodSugarCount != null && measurements != null
  }

  fun summaryFetched(measurements: List<BloodSugarMeasurement>): BloodSugarSummaryViewModel {
    return copy(measurements = measurements)
  }

  fun countFetched(count: Int): BloodSugarSummaryViewModel =
      copy(totalRecordedBloodSugarCount = count)

}
