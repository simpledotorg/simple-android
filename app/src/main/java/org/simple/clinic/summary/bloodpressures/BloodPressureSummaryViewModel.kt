package org.simple.clinic.summary.bloodpressures

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.summary.PatientSummaryChildModel
import java.util.UUID

@Parcelize
data class BloodPressureSummaryViewModel(
    val patientUuid: UUID,
    val latestBloodPressuresToDisplay: List<BloodPressureMeasurement>?,
    val totalRecordedBloodPressureCount: Int?,
    val facility: Facility?,
    val diagnosedWithDiabetes: Answer?,
) : Parcelable, PatientSummaryChildModel {
  companion object {
    fun create(patientUuid: UUID) =
        BloodPressureSummaryViewModel(
            patientUuid,
            null,
            null,
            null,
            null
        )
  }

  val hasLoadedCountOfBloodSugars: Boolean
    get() = totalRecordedBloodPressureCount != null

  val hasLoadedFacility: Boolean
    get() = facility != null

  val isDiabetesManagementEnabled: Boolean
    get() = facility!!.config.diabetesManagementEnabled

  override fun readyToRender(): Boolean {
    return hasLoadedCountOfBloodSugars && latestBloodPressuresToDisplay != null && diagnosedWithDiabetes != null
  }

  fun bloodPressuresLoaded(bloodPressures: List<BloodPressureMeasurement>): BloodPressureSummaryViewModel =
      copy(latestBloodPressuresToDisplay = bloodPressures)

  fun bloodPressuresCountLoaded(bloodPressuresCount: Int): BloodPressureSummaryViewModel =
      copy(totalRecordedBloodPressureCount = bloodPressuresCount)

  fun currentFacilityLoaded(facility: Facility): BloodPressureSummaryViewModel =
      copy(facility = facility)

  fun medicalHistoryLoaded(medicalHistory: MedicalHistory): BloodPressureSummaryViewModel =
      copy(diagnosedWithDiabetes = medicalHistory.diagnosedWithDiabetes)

}
