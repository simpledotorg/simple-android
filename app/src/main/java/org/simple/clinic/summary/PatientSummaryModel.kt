package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.summary.teleconsultation.api.TeleconsultInfo
import java.util.UUID

@Parcelize
data class PatientSummaryModel(
    val openIntention: OpenIntention,
    val patientUuid: UUID,
    val patientSummaryProfile: PatientSummaryProfile?,
    val currentFacility: Facility?,
    val hasCheckedForInvalidPhone: Boolean,
    val linkIdWithPatientViewShown: Boolean,
    val teleconsultInfo: TeleconsultInfo?
) : Parcelable {

  companion object {
    fun from(openIntention: OpenIntention, patientUuid: UUID): PatientSummaryModel {
      return PatientSummaryModel(
          openIntention = openIntention,
          patientUuid = patientUuid,
          patientSummaryProfile = null,
          currentFacility = null,
          hasCheckedForInvalidPhone = false,
          linkIdWithPatientViewShown = false,
          teleconsultInfo = null
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

  fun fetchedTeleconsultationInfo(teleconsultInfo: TeleconsultInfo): PatientSummaryModel {
    return copy(teleconsultInfo = teleconsultInfo)
  }

  fun fetchingTeleconsultationInfo(): PatientSummaryModel {
    return copy(teleconsultInfo = TeleconsultInfo.Fetching)
  }

  fun failedToFetchTeleconsultationInfo(): PatientSummaryModel {
    return copy(teleconsultInfo = TeleconsultInfo.NetworkError)
  }
}
