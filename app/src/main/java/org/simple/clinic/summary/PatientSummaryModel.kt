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
    val currentFacility: Facility?,
    val hasCheckedForInvalidPhone: Boolean,
    val linkIdWithPatientViewShown: Boolean
) : Parcelable {

  companion object {
    fun from(openIntention: OpenIntention, patientUuid: UUID): PatientSummaryModel {
      return PatientSummaryModel(
          openIntention = openIntention,
          patientUuid = patientUuid,
          patientSummaryProfile = null,
          currentFacility = null,
          hasCheckedForInvalidPhone = false,
          linkIdWithPatientViewShown = false
      )
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

  fun completedCheckForInvalidPhone(): PatientSummaryModel {
    return copy(hasCheckedForInvalidPhone = true)
  }

  fun shownLinkIdWithPatientView(): PatientSummaryModel {
    return copy(linkIdWithPatientViewShown = true)
  }
}
