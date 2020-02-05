package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import java.util.UUID

@Parcelize
data class PatientSummaryModel(
    val openIntention: OpenIntention,
    val patientUuid: UUID,
    val patientSummaryProfile: PatientSummaryProfile?,
    val currentFacility: Facility?
) : Parcelable {

  companion object {
    fun from(openIntention: OpenIntention, patientUuid: UUID): PatientSummaryModel {
      return PatientSummaryModel(openIntention, patientUuid, null, null)
    }
  }

  val hasLoadedPatientSummaryProfile: Boolean
    get() = patientSummaryProfile != null

  val hasLoadedCurrentFacility: Boolean
    get() = currentFacility != null

  val isDiabetesManagementEnabled: Boolean
    get() = currentFacility!!.config.diabetesManagementEnabled

  fun patientSummaryProfileLoaded(patientSummaryProfile: PatientSummaryProfile): PatientSummaryModel {
    return copy(patientSummaryProfile = patientSummaryProfile)
  }

  fun currentFacilityLoaded(facility: Facility): PatientSummaryModel {
    return copy(currentFacility = facility)
  }
}
