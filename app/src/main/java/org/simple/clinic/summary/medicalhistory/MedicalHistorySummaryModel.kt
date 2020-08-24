package org.simple.clinic.summary.medicalhistory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.summary.PatientSummaryChildModel
import java.util.UUID

@Parcelize
data class MedicalHistorySummaryModel(
    val patientUuid: UUID,
    val medicalHistory: MedicalHistory? = null,
    val currentFacility: Facility? = null
) : Parcelable, PatientSummaryChildModel {

  companion object {
    fun create(patientUuid: UUID): MedicalHistorySummaryModel = MedicalHistorySummaryModel(
        patientUuid = patientUuid
    )
  }

  val hasLoadedMedicalHistory: Boolean
    get() = medicalHistory != null

  val hasLoadedCurrentFacility: Boolean
    get() = currentFacility != null

  override fun readyToRender(): Boolean {
    return hasLoadedMedicalHistory && hasLoadedCurrentFacility
  }

  fun medicalHistoryLoaded(medicalHistory: MedicalHistory): MedicalHistorySummaryModel {
    return copy(medicalHistory = medicalHistory)
  }

  fun currentFacilityLoaded(facility: Facility): MedicalHistorySummaryModel {
    return copy(currentFacility = facility)
  }
}
